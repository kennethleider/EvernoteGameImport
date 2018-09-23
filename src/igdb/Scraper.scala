package igdb

import model.Game
import steam.SteamAccessor

object Scraper {
   def main(args: Array[String]) {
      val steamUser = args(0)
   
      val steam = new SteamAccessor("45DBD24B26DFFDEB761F6C18F9AEE09A")
      val igdb = new IGDBAccessor("2259e85f479bb30c5a7ae1ce27eaa478")

      val steamGames = steam.getAllGamesForUser(steamUser)
      steamGames.foreach { ownedGame =>
         val matches = igdb.getGameBySteamId(ownedGame.appid)
         if (matches.isEmpty) {
            println(s"${ownedGame.appid}, ${ownedGame.name}, , , , , , ${ownedGame.playtimeForever / 60}")
         } else {
            matches.foreach { record =>
               println(s"${ownedGame.appid}, ${ownedGame.name}, ${record.id}, ${record.name}, ${record.url}, ${record.parent.getOrElse("")}, ${record.versionParent.getOrElse("")}, ${ownedGame.playtimeForever / 60}")
            }
         }
      }
   }
}
