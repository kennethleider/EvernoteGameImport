package gog

import model.{Note, Game}

import scala.io.Source
import scala.util.Try

/**
 * Created by Kenneth on 11/23/2015.
 */
trait Action {
  def apply()
}

object SteamAction {
  val ownedAppID = Source.fromInputStream(classOf[Game].getResourceAsStream("/steam/owned_elsewhere.txt")).getLines().filter { !_.isEmpty }.map { line =>
    val tokens = line.split(":")
    tokens(0)
  }.toSet
}

class SteamAction(val appID : String) extends Action {
  def apply(): Unit = {
    if (!SteamAction.ownedAppID.contains(appID)) {
      println(s"Add to owned_elsewhere.txt : $appID:gog")
    }
  }
}

class ScrapeAction(val url : String) extends Action {
  def apply(): Unit = {
    println(s"create note for $url")
  }
}
