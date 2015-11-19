import java.io.{File, PrintWriter}
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}

import model.{Game, Note}
import templates.ExportTemplate

import scala.util.{Failure, Success, Try}
import scala.xml.XML


object ENEXWriter {

  val instantFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")

  lazy val baselines : Map[String, Note] = {
    val query = new TemporalQuery[Instant] {
      override def queryFrom(temporal: TemporalAccessor): Instant = { Instant.from(temporal)}
    }

    (XML.loadFile(new File("baseline.enex")) \ "note").map { node =>
      val note = Note(
        title = (node \ "title").text.trim,
        created = instantFormat.parse((node \ "created").text.trim).query (query),
        tags = (node \ "tag").map {
          _.text
        },
        sourceUrl = (node \ "note-attributes" \ "source-url").text.trim
      )

      val appID = note.sourceUrl.split("/").reverse.head
      (appID -> note)
    }.toMap
  }

  def applyBaseline(note: Note) : Note = {
    val appID = note.sourceUrl.split("/").reverse.head
    val baseline = baselines.get(appID)

    baseline.map { b =>
      if (note.title != b.title)
        println(s"Titles do not match: ${note.title} != ${b.title}")

      var tags : Set[String] = (b.tags ++ note.tags).toSet

      if (tags.contains("completed")) tags --= Seq("played", "unplayed")
      if (tags.contains("played")) tags -= "unplayed"

      note.copy(
        title = b.title,
        created = b.created,
        tags = tags.toSeq.sorted
      )
    }.getOrElse(note)

  }

  def main(args: Array[String]) {
    val notes = args.map { new File(_).listFiles() }.flatten.map { Note.read(_) }.flatten
    //val baselined = notes.map { applyBaseline(_) }
    val baselined = notes.filter { note =>
      val appID = note.sourceUrl.split("/").reverse.head
      !baselines.contains(appID)
    }

    println (s"Exporting ${baselined.size} notes")
    val writer = new PrintWriter("computer-games.enex","UTF-8")
    writer.write(ExportTemplate.fill(baselined))
    writer.close()
  }
}
