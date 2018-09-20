package steam

import java.io.{File, PrintWriter}
import java.time.Instant
import java.time.temporal.ChronoUnit

import model.Game.cacheDir

import scala.io.Source
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

case class OwnedGame(appid : Int, name : String, playtimeForever : Int)

class SteamAccessor(key : String) {
   def getAllGamesForUser(userID : String) = {
      val cacheFile = new File(cacheDir, s"user-${userID}.json")
      if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(2, ChronoUnit.DAYS)) != 1) {
         val url = s"http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=${key}&steamid=${userID}&format=json&include_appinfo=1&include_played_free_games=1"
         val text = Source.fromURL(url).mkString
         val writer = new PrintWriter(cacheFile)
         writer.write(text)
         writer.close()
      }
      
      val json = JSON.parseRaw(Source.fromFile(cacheFile).mkString).get.asInstanceOf[JSONObject]
      json.obj("response").asInstanceOf[JSONObject].obj("games").asInstanceOf[JSONArray].list
         .map(_.asInstanceOf[JSONObject].obj)
         .map(obj => OwnedGame(obj("appid").toString.toDouble.toInt, obj("name").toString, obj("playtime_forever").toString.toDouble.toInt))
   }
}
