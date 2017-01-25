package model

import java.io.{FileInputStream, InputStreamReader, FileReader, File}
import java.net.URLEncoder
import java.time.{Instant, LocalDate}

import scala.io.Source
import scala.util.Try
import scala.xml.{Unparsed, NodeSeq, Node, XML}

object DescribedURL {
  def read(seq: NodeSeq): Seq[DescribedURL] = {
    seq.map { node => DescribedURL(node \@ "url", node \@ "description") }
  }

  def apply(url: String, desc: String) = new DescribedURL(url, GameDetails.clean(desc))
}

class DescribedURL(val url: String, val desc: String)


object Note {
  def readParts(file : File) : Seq[Note] = {
    println(s"Reading $file")
    val root = XML.load(new InputStreamReader(new FileInputStream(file), "UTF-16"))
    (root \ "note").map { note : Node =>
      Note(
        title = note \@ "title",
        created = Try(Instant.parse(note \@ "created")).getOrElse(null),
        modified = Instant.parse(note \@ "modified"),
        tags = (note \ "tag").map { _.text },

        sourceUrl = note \@ "source",
        imageURL = note \@ "image",
        review = Try(Review.read(note \ "review")).getOrElse(null),
        releaseDate = Try(LocalDate.parse(note \@ "releaseDate")).getOrElse(null),
        developer = DescribedURL.read(note \ "developer").headOption.getOrElse(null),
        publisher = DescribedURL.read(note \ "publisher").headOption.getOrElse(null),
        description = (note \ "short_description").text,
        steamTags = (note \ "steamTag").flatMap { DescribedURL.read(_) },
        genres= (note \ "genre").flatMap { DescribedURL.read(_) },
        dlc = (note \ "content").flatMap { DescribedURL.read(_) },
        playTime = Try((note \@ "playTime").toDouble).getOrElse(0.0),

        website = note \ "links" \@ "website",
        updateHistory = note \ "links" \@ "updates",
        news = note \ "links" \@ "news",
        discussions = note \ "links" \@ "discussions",
        groupSearch = note \ "links" \@ "groups",
        longDescription = (note \ "long-description" \ "div").toString()
      )
    }
  }

  def read(file: File): Seq[Note] = {
    println(s"Reading $file")
    val root = XML.load(new InputStreamReader(new FileInputStream(file), "UTF-16"))
    (root \ "note").map { note : Node =>
      Note(
        title = note \@ "title",
        created = Instant.parse(note \@ "created"),
          modified = Instant.parse(note \@ "modified"),
        tags = (note \ "tag").map { _.text },

        sourceUrl = note \@ "source",
        imageURL = note \@ "image",
        review = Review.read(note \ "review"),
        releaseDate = LocalDate.parse(note \@ "releaseDate"),
        developer = DescribedURL.read(note \ "developer").head,
        publisher = DescribedURL.read(note \ "publisher").head,
        description = (note \ "short_description").text,
        steamTags = (note \ "steamTag").flatMap { DescribedURL.read(_) },
        genres= (note \ "genre").flatMap { DescribedURL.read(_) },
        dlc = (note \ "content").flatMap { DescribedURL.read(_) },
        playTime = (note \@ "playTime").toDouble,

        website = note \ "links" \@ "website",
        updateHistory = note \ "links" \@ "updates",
        news = note \ "links" \@ "news",
        discussions = note \ "links" \@ "discussions",
        groupSearch = note \ "links" \@ "groups",
        longDescription = (note \ "long-description" \ "div").toString()
      )
    }
  }

  def write(notes : Seq[Note]) : Node = {
    <notes>
      {notes.map { note =>
      <note title={note.title} created={note.created.toString} modified={note.created.toString} source={note.sourceUrl} image={note.imageURL}
            releaseDate={note.releaseDate.toString} playTime={note.playTime.toString}>
        {note.tags.map { tag => <tag>{tag}</tag> }}
        {note.steamTags.map { tag => <steamTag url={tag.url} description={tag.desc}/> }}
        {note.genres.map { genre => <genre url={genre.url} description={genre.desc}/> }}
        {note.dlc.map { content => <content url={content.url} description={content.desc}/> }}
        {Review.write(note.review)}
        <developer url={note.developer.url} description={note.developer.desc}/>
        <publisher url={note.publisher.url} description={note.publisher.desc}/>
        <links website={note.website} updates={note.updateHistory} news={note.news} discussions={note.discussions} groups={note.groupSearch}/>
        <long-description>{Unparsed(note.longDescription)}</long-description>
      </note>
    }}
    </notes>
  }


  def load(file: File) = {
    (XML.loadFile(file) \ "note").map { node =>
      Note(
        title = (node \ "title").text.trim,
        created = Instant.parse((node \ "created").text),
        tags = (node \ "tag").map {
          _.text
        },
        sourceUrl = (node \\ "source-url").text.trim
      )
    }
  }

  def apply(game: Game, source : String): Note = {
    try {
      Note(
        title = game.details.title,
        created = Instant.now(),
        sourceUrl = game.storePage,
        imageURL = game.details.image,
        review = game.details.review,
        releaseDate = game.details.releaseDate,
        steamTags = game.details.tags.take(5),
        playTime = game.playtimeMinutes / 60.0,
        developer = game.details.developer,
        publisher = game.details.publisher,
        genres = game.details.genre,
        dlc = game.details.dlc,
        description = game.details.description,
        longDescription = game.details.longDescription,

        website = game.details.webSite.getOrElse(game.storePage),
        updateHistory = game.updateHistory,
        news = game.news,
        discussions = game.discussions,
        groupSearch = game.details.groupSearch,
        tags = if (game.playtimeMinutes > 180) Seq(source, "played") else Seq(source, "unplayed")
      )
    } catch {
      case e: Throwable => throw new Exception(s"Failed to make note for ${game.storePage}  ", e)
    }
  }
}

case class Note(
                 title: String = "undefined title",
                 created: Instant = Instant.now(),
                 modified: Instant = Instant.now(),
                 tags: Seq[String] = Seq("unplayed", "steam"),

                 sourceUrl: String = "undefined source",
                 imageURL: String = "undefined image",
                 review: Review = Review(),
                 releaseDate: LocalDate = LocalDate.now(),
                 developer: DescribedURL = DescribedURL("unknown", "unknown"),
                 publisher: DescribedURL = DescribedURL("unknown", "unknown"),
                 description: String = "undefined description",
                 steamTags: Seq[DescribedURL] = Seq(),
                 genres: Seq[DescribedURL] = Seq(),
                 dlc: Seq[DescribedURL] = Seq(),
                 playTime: Double = 60,

                 website: String = "undefined",
                 updateHistory: String = "undefined",
                 news: String = "undefined",
                 discussions: String = "undefined",
                 groupSearch: String = "undefined",
                 longDescription: String = "undefined"
                 )
