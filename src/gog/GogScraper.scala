package gog

import java.io._
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util

import model.{Game, Note}
import org.openqa.selenium.{By, JavascriptExecutor}
import scrape.Scraper._

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML, PrettyPrinter}
import scala.collection.JavaConversions._

object GogScraper {
  def load() : Seq[_] = {
    val gogCache = new File(cacheDir, "gog")

    val lists = gogCache.listFiles().filter { _.getName.startsWith("games") }.map { file =>
      val cacheFile = new File(gogCache, file.getName.replaceFirst("games", "cleaned"))
      if (!cacheFile.exists() || Instant.ofEpochMilli(cacheFile.lastModified()).compareTo(Instant.now().minus(3, ChronoUnit.DAYS)) != 1) {
        driver.get(file.toURI.toString)
        val javascript = "return JSON.stringify(gogData.accountProducts)";
        val accountProducts = driver.asInstanceOf[JavascriptExecutor].executeScript(javascript, driver.findElement(By.tagName("html")))

        val writer = new FileOutputStream(cacheFile))
        writer.write()
        writer.close()
      }

      val writer = new ObjectInputStream(new FileInputStream(cacheFile))
      val list = writer.readObject().asInstanceOf[util.ArrayList[_]]
      writer.close()
      list.toList
    }
    lists
  }


  def main(args: Array[String]) {
    load().foreach { name => println(name) }

  }
}
