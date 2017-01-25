package steam

import java.io.{File, FileOutputStream, StringReader}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate}

import model.{Game, GameDetails}
import org.openqa.selenium.{By, JavascriptExecutor}
import scrape.Scraper._

import scala.util.Try
import scala.xml.{NodeSeq, Text, XML}


object SteamSaleReport {
  val MAX_PRICE = 5.00
  val MIN_DISCOUNT = -50
  val MIN_AGE = 90

  val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
  case class Row(title : String, appID : Int, date : LocalDate, owned : Boolean, price : Double, discount : Int)

  def main(args: Array[String]) {
    var doMore = true
    var page = 1
    while (doMore) {
      doMore = doPage(page)
      page += 1
    }
  }

  def doPage(page : Int) = {
    println(s"Analyzing sales page $page")
    val rows = getPage(page).filter(findAttributeContains("class", "search_result_row")).map { root =>
      val nodes = root.descendant_or_self
      Row(
        title = nodes.find(findClass("title")).map { _.text }.getOrElse("Unknown"),
        appID = (root \@ "href").replaceAll(".*(app|sub)/(.*)/.*","$2").toInt,
        date = Try(LocalDate.parse(nodes.find(findClass("col search_released responsive_secondrow")).map { _.text }.getOrElse("Unknown"), dateFormatter)).getOrElse(LocalDate.now),
        owned = nodes.find(findAttributeContains("class", "ds_owned_flag")).isDefined,
        price = Try(nodes.filter(findAttributeContains("class", " search_price ")).head.child.collect { case t : Text => t}.text.trim.replaceAll("[^\\d.]+", "").toDouble).getOrElse(0),
        discount = Try((nodes.filter(findAttributeContains("class", " search_discount ")).head \ "span").text.trim.replaceAll("[^\\d.-]+", "").toInt).getOrElse(0)
      )
    }

    //rows.slice(0,5).foreach { println(_) }

    val price_filtered = rows.filter { _.price < MAX_PRICE }
    val discount_filtered = price_filtered.filter { _.discount <= MIN_DISCOUNT }
    val date_filtered = discount_filtered.filter (_.date.plusDays(MIN_AGE).compareTo(LocalDate.now) < 0)
    val owned_filtered = date_filtered.filter { _.owned == false}
    val done = owned_filtered.filter { row =>
      val elements = GameDetails.getPage(Game(row.appID.toString, 0))
      try {
        val notInterested = (elements.filter(classContains("queue_btn_ignore")).head.descendant.filter(classContains("queue_btn_active")) \@ "style").isEmpty
        val earlyAccess = elements.exists { node => node.label == "h1" && node.text == "Early Access Game"}
        val onWishlist = false
        !notInterested && !earlyAccess
      } catch {
        case e :
          Throwable =>
          println (row.appID)
          Thread.sleep(10)
          e.printStackTrace()
          System.exit(1)
          true
      }
    }

    done.foreach { row =>
      val game = Game(row.appID.toString, 0)

      println(game.storePage)
    }
    !rows.isEmpty
  }

  val cacheDir = new File("cache", "sales")
  cacheDir.mkdirs()

  private def getPage(pageNumber : Int) : NodeSeq = {

    val cacheFile = new File(cacheDir, s"${pageNumber}.html")
    if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(1, ChronoUnit.DAYS)) != 1) {
      val page = s"http://store.steampowered.com/search/?specials=1&os=win#sort_by=&sort_order=0&specials=1&category1=998&os=win&page=${pageNumber}"
      println(page)
      driver.get(page)
      if (!driver.getCurrentUrl.equals(page)) throw new IllegalAccessException(s"${page} does not exist")
      val javascript = "return arguments[0].innerHTML";
      val pageSource = driver.asInstanceOf[JavascriptExecutor].executeScript(javascript, driver.findElement(By.tagName("html")))

      val node = cleaner.clean(new StringReader("<html>"+pageSource +"</html>"))
      val writer = new FileOutputStream(cacheFile)
      serializer.writeToStream(node, writer)
      writer.close()
      Thread.sleep(1000)
    }

    XML.loadFile(cacheFile).descendant_or_self
  }
}
