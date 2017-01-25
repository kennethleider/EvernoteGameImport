package gog

import java.io._
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util

import model.{Game, Note}
import org.json4s.JsonAST.JArray
import org.json4s.{JObject, FileInput, JsonInput}
import org.json4s.native.JsonMethods
import org.openqa.selenium.{By, JavascriptExecutor}
import scrape.Scraper._

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML, PrettyPrinter}
import scala.collection.JavaConversions._

//https://www.gog.com/account/getFilteredProducts?hiddenFlag=0&mediaType=1&page=2&sortBy=title

object GogScraper {
  val gogCache = new File(cacheDir, "gog")

  private def getPage(pageNumber : Int)  = {

    val cacheFile = new File(gogCache, s"${pageNumber}.html")
    if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(1, ChronoUnit.DAYS)) != 1) {
      val page = s"https://www.gog.com/account/getFilteredProducts?hiddenFlag=0&mediaType=1&page=${pageNumber}&sortBy=title"
      driver.get(page)
      val source = (XML.loadString(driver.getPageSource) \\ "pre").text
      val writer = new FileOutputStream(cacheFile)
      writer.write(source.getBytes)
      writer.close()
    }
    JsonMethods.parse(new FileInput(cacheFile)).asInstanceOf[JObject]
  }

//  def load() : Seq[_] = {
//
//
//    gogCache.listFiles().filter { _.getName.startsWith("games") }.flatMap { file =>
//      val cacheFile = new File(gogCache, file.getName.replaceFirst("games(.*).html", "cleaned$1.json"))
//      if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(3, ChronoUnit.DAYS)) != 1) {
//        driver.get(file.toURI.toString)
//        val javascript = "return JSON.stringify(gogData.accountProducts)";
//        val accountProducts = driver.asInstanceOf[JavascriptExecutor].executeScript(javascript, driver.findElement(By.tagName("html")))
//
//        val writer = new FileOutputStream(cacheFile)
//        writer.write(accountProducts.toString.getBytes)
//        writer.close()
//      }
//
//        JsonMethods.parse(new FileInput(cacheFile)).children
//    }
//  }


  val actions : Map[String, Action] = Source.fromInputStream(classOf[Game].getResourceAsStream("/gog/actions.txt")).getLines().flatMap { line =>
    val steam = "(.*?):::steam:(.*)".r
    val scrape = "(.*?):::scrape".r

    line.trim match {
      case steam(url, appID) => Some(url -> new SteamAction(appID))
      case scrape(url) => Some(url -> new ScrapeAction(url))
      case _ => None
    }
  }.toMap

  def main(args: Array[String]) {
    //    load().foreach { name => println(name) }

    val partitions = (1 to 2).map { page =>
      val json: JObject = getPage(page)
      val titles = json.values("products") match {
        case array: List[Map[String, Any]] => array.map { values =>
          values("title").toString
        }
      }

      titles.partition(actions.contains(_))
    }

    val found = partitions.flatMap {
      _._1
    }
    val missing = partitions.flatMap {
      _._2
    }

    found.foreach { title =>
      actions.get(title).foreach {
        _.apply()
      }
    }

    missing.foreach { title => println(s"$title:::unknown") }
  }
}
