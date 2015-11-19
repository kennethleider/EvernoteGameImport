package model

import java.io.{FileInputStream, File, FileOutputStream, StringReader}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate}
import java.util.Properties
import scrape.Scraper._

import org.htmlcleaner.{HtmlCleaner, PrettyXmlSerializer}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.{By, JavascriptExecutor}

import scala.io.Source
import scala.util.Try
import scala.xml.{Node, NodeSeq, XML}

object GameDetails {

  val cacheDir = new File("cache")
  cacheDir.mkdirs()

  val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

  lazy val repairs = {
    val retval = new Properties()
    retval.load(classOf[GameDetails].getResourceAsStream("/steam/repairs.properties"))
    retval
  }


  def load(game: Game) : GameDetails = {
    val elements = getPage(game)
    val review = elements.filter(findAttribute("onclick", "window.location='#app_reviews_hash'")).headOption.map { div =>
      Review(
        tone = (div \ "div" \ "span").head \@ "class" match {
          case "game_review_summary" => "negative"
          case "game_review_summary positive" => "positive"
          case value => throw new IllegalArgumentException(value)
        },
        description = (div \ "div" \ "span").head.text.trim,
        count = (div \ "div" \ "span")(1).text.replaceAll("\\D","").toInt
      )
    }

    GameDetails(
      title = clean(elements.filter(findClass("apphub_AppName")).text.trim),
      releaseDate = (elements \ "span").filter(findClass("date")).headOption.flatMap {
        node => Try(LocalDate.parse(node.text.trim, dateFormatter)).toOption
      }.getOrElse { repairDate(game.appID, "releaseDate")},

      image = elements.filter(findClass("game_header_image_full")) \@ "src",
      icon = (elements.filter(findClass("apphub_AppIcon")) \ "img" \@ "src").trim,

      description = clean(elements.filter(findClass("game_description_snippet")).text.trim),
      longDescription = clean(elements.filter(findAttribute("id", "game_area_description")).toString.replaceFirst("About This Game","")),

      review = review.getOrElse(Review()),
      tags = (elements.filter(findClass("glance_tags popular_tags")) \ "a").map { node =>
        DescribedURL(node \@ "href", node.text)
      },
      dlc = dlc(elements),
      developer = urls(elements, "http://store.steampowered.com/search/?developer=").headOption.getOrElse {repair(game.appID, "developer")},
      publisher = urls(elements, "http://store.steampowered.com/search/?publisher=").headOption.getOrElse {repair(game.appID, "publisher")},
      genre = elements.filter(findClass("details_block")).head.descendant.filter(findAttributeContains("href", "http://store.steampowered.com/genre")).map { node => DescribedURL(node \@ "href", node.text) },
      groupSearch =  elements.filter(findAttributeContains("href", "http://steamcommunity.com/actions/Search?T=ClanAccoun")) \@ "href",
      webSite =  elements.filter(findClass("linkbar")).filter ( _.text.contains("Visit the website") ).headOption.map { _ \@ "href" }
    )
  }

  def repairDate(appID : String, key : String) :LocalDate= {
    val value = repairs.getProperty(s"${appID}.${key}")
    if (value == null) throw new IllegalStateException(s"No repair value for ${appID}.${key}")
    LocalDate.parse(value, dateFormatter)
  }

  def repair(appID : String, key : String) :DescribedURL= {
    val value = repairs.getProperty(s"${appID}.${key}")
    if (value == null) throw new IllegalStateException(s"No repair value for ${appID}.${key}")
    val tokens = value.split(",")
    DescribedURL(tokens(1), tokens(0))
  }

  def urls(elements : NodeSeq, pattern : String)= {
    elements.filter(findAttributeContains("href", pattern)).map { node => DescribedURL(node \@ "href", node.text) }
  }

  def dlc(elements : NodeSeq): Seq[DescribedURL] = {
    val owned = elements.filter(findAttributeContains("id","dlc_row_")).filter { node => node.descendant.exists(findAttributeContains("class","ds_owned_flag")) }
    owned.map { node =>
      new DescribedURL(node \@ "href", clean(node.descendant.filter(findClass("game_area_dlc_name")).text.trim ))
    }
  }

  def clean(in : String): String = {
    in.replaceAll( "[^\\p{Print}]", "")
  }



  val wayback : Map[String, String] = Source.fromInputStream(classOf[Game].getResourceAsStream("/wayback.txt")).getLines().map { line =>
    val pattern = "(.*?):(.*)".r

    line.trim match {
      case pattern(key, value) => (key -> value)
      case _ => "" -> ""
    }
  }.toMap

  def getPage(game : Game) : NodeSeq = {

    val cacheFile = new File(cacheDir, s"${game.appID}.html")
    if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(60, ChronoUnit.DAYS)) != 1) {
      val page = wayback.getOrElse(game.appID, game.storePage)
      driver.get(page)
      if (!driver.getCurrentUrl.equals(page)) throw new IllegalAccessException(s"${page} does not exist")
      val javascript = "return arguments[0].innerHTML";
      val pageSource = driver.asInstanceOf[JavascriptExecutor].executeScript(javascript, driver.findElement(By.tagName("html")))

      val node = cleaner.clean(new StringReader("<html>"+pageSource +"</html>"))
      val writer = new FileOutputStream(cacheFile)
      serializer.writeToStream(node, writer)
      writer.close()
    }


    XML.loadFile(cacheFile).descendant_or_self
  }
}

case class Review(tone : String = "positive", description : String = "No reviews", count : Int = 0)

object Review {
  def write(review: Review) : Node = {
    <review tone={review.tone} description={review.description} count={review.count.toString}/>
  }

  def read(seq: NodeSeq): Review = {
    seq.map { node =>
      Review(node \@ "tone", node \@ "description", (node \@ "count").toInt)
    }.head
  }
}

case class GameDetails (
                         title: String,
                         releaseDate: LocalDate,
                         image: String,
                         icon: String,
                         description: String,
                         longDescription: String,
                         review : Review,
                         tags : Seq[DescribedURL],
                         developer : DescribedURL,
                         publisher : DescribedURL,
                         genre : Seq[DescribedURL],
                         groupSearch : String,
                         webSite : Option[String],
                         dlc : Seq[DescribedURL])