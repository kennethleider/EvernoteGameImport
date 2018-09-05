package steam

import java.io.PrintWriter

import model.{Game, Note}

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.PrettyPrinter


object SteamScraper {


  def main(args: Array[String]) {
    val key = args(0)
    val id = args(1)

    val games = Game.load(key, id)
    
//    val ownedElseWhere = Source.fromInputStream(classOf[Game].getResourceAsStream("/steam/owned_elsewhere.txt")).getLines().filter { !_.isEmpty }.map { line =>
//      val tokens = line.split(":")
//      val id = tokens(0)
//      val tags = tokens(1)
//      Try(Note(Game(id, 0), tags))
//    }.toSeq

    //val steamNotes = Seq[Try[Note]]()
    val steamNotes = Seq(Try(Note(Game("259530", 0), "steam")))
//    val steamNotes = games.map { game => Try(Note(game, "steam")) }
    steamNotes.collect {
      case Failure(th) => th.getMessage
      case Success(note) => note.title
    }

//    ownedElseWhere.map { _.map { _.sourceUrl }}.toSet.intersect(steamNotes.map { _.map { _.sourceUrl }}.toSet).foreach { _.map { url =>
//      println(s"Duplicate between owned elsewhere and steam: $url")
//    }}
//    val notes = (ownedElseWhere ++ steamNotes).collect {
//      case Success(note) => note
//    }
//
//    (ownedElseWhere ++ steamNotes).collect {
//      case Failure(ex) =>
//        println(ex.getMessage + s":${ex.getCause.getMessage}")
//        None
//    }
//
//    notes.foreach { note =>
//      val appID = note.sourceUrl.split("/").reverse.head
//      val writer = new PrintWriter(s"notes/steam/$appID.xml","UTF-16")
//      println (s"Writing notes/steam/$appID.xml")
//      writer.write( new PrettyPrinter(500, 3).format(Note.write(Seq(note))))
//      writer.close()
//    }

  }
}
