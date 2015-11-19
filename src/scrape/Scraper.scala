package scrape

import java.io.File

import org.htmlcleaner.{PrettyXmlSerializer, HtmlCleaner}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

import scala.xml.Node


object Scraper {
  val cleaner = new HtmlCleaner()
  val serializer = new PrettyXmlSerializer(cleaner.getProperties)
  val cacheDir = new File("cache")
  cacheDir.mkdirs()

  lazy val driver = {
    val profileDir = new File(new File("cache"), "chrome-profile")
    profileDir.mkdirs()
    val options = new ChromeOptions();
    options.addArguments(s"user-data-dir=${profileDir.getAbsoluteFile}")
    val d = new ChromeDriver(options)
    d.get("http://store.steampowered.com/app/6860")
    d.getCurrentUrl
    Thread.sleep(10000)
    d
  }

  def classContains(value : String) = findAttributeContains("class", value) _

  def findClass(value : String) = findAttribute("class", value) _

  def findAttribute(key : String, value: String)(node : Node) : Boolean = {
    val att = node.attribute(key)
    att match {
      case Some(attVal) =>
        attVal.text.equals(value)
      case None => false
    }
  }

  def findAttributeContains(key : String, value: String)(node : Node) : Boolean = {
    val att = node.attribute(key)
    att match {
      case Some(attVal) =>
        attVal.text.contains(value)
      case None => false
    }
  }
}
