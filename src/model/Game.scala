package model

import java.io.{PrintWriter, File}
import java.time.Instant
import java.time.temporal.ChronoUnit

import scala.io.Source
import scala.util.parsing.json._
import scala.xml.XML


object Game {
  val cacheDir = new File("cache")
  cacheDir.mkdirs()
  
  val addOns: Set[String] = Source.fromInputStream(classOf[Game].getResourceAsStream("/add-ons.txt")).getLines().flatMap { line =>
    line.split(":")(1).split(",")
  }.toSet
  
  val skip: Map[String, String] = Source.fromInputStream(classOf[Game].getResourceAsStream("/steam/skip.txt")).getLines().map { line =>
    val pattern = "(.*?):(.*)".r
    
    line.trim match {
      case pattern(key, value) => (key -> value)
      case _ => "" -> ""
    }
  }.toMap
  
  val replace: Map[String, String] = Source.fromInputStream(classOf[Game].getResourceAsStream("/steam/replace.txt")).getLines().map { line =>
    val pattern = "(.*?):(.*)".r
    
    line.trim() match {
      case pattern(key, value) => (key -> value)
    }
  }.toMap
  
  def load(key: String, userID: String) = {
    val cacheFile = new File(cacheDir, s"user-${userID}.json")
    if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(2, ChronoUnit.DAYS)) != 1) {
      val url = s"http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=${key}&steamid=${userID}&format=json&include_appinfo=1&include_played_free_games=1"
      val text = Source.fromURL(url).mkString
      val writer = new PrintWriter(cacheFile)
      writer.write(text)
      writer.close()
    }
    
    val json = JSON.parseRaw(Source.fromFile(cacheFile).mkString).get.asInstanceOf[JSONObject]
    val games = json.obj("response").asInstanceOf[JSONObject].obj("games").asInstanceOf[JSONArray].list
       .map(_.asInstanceOf[JSONObject].obj)
       .map(obj => Game(obj("appid").toString, obj("playtime_forever").toString.toDouble.toInt))
       .filterNot { game => addOns.contains(game.appID) }
       .filterNot { game =>
         val result = skip.contains(game.appID)
         if (result) println(s"Skipping ${game.appID}: ${skip(game.appID)}")
         result
       }
    
    val (replacements, keep) = games.partition(game => replace.contains(game.appID))
    val timeUpdates = replacements.map { game => game.copy(appID = replace(game.appID)) }
    val newGames = timeUpdates.filterNot { game => keep.exists { k => k.appID.equals(game.appID) } }.map { game => game.copy(playtimeMinutes = 0) }.toSet
    (keep ++ newGames).map { game =>
      val extraTime = timeUpdates.filter {
        _.appID.equals(game.appID)
      }.map {
        _.playtimeMinutes
      }.sum
      if (extraTime > 0) println(s"Adding ${extraTime} minutes to ${game.appID}")
      game.copy(playtimeMinutes = game.playtimeMinutes + extraTime)
    }
  }
}


case class Game(appID : String, playtimeMinutes : Int) {
  val storePage = s"http://store.steampowered.com/app/${appID}"
  val updateHistory: String = s"http://steamcommunity.com/games/${appID}/announcements/"
  val news: String = s"http://steamcommunity.com/games/${appID}/allnews/"
  val discussions: String = s"http://steamcommunity.com/games/${appID}/discussions/"

  lazy val details = GameDetails.load(this)
}
