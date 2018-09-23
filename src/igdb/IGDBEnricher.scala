package igdb

import java.io.File
import java.time.Instant

import com.typesafe.config.ConfigFactory

import scala.io.Source

object IGDBEnricher {
   def main(args: Array[String]): Unit = {
   
//      Source.fromFile(args(0)).getLines().toList
//         .filterNot(_.startsWith("#"))
//         .map(line => try{ ConfigFactory.parseString(line) } catch { case th : Throwable => println(line); throw th})
//         .map { c => HumbleBundleLibraryEntry(c) }
//         .filterNot(_.id.isEmpty)
//         .foreach(record => println(s"${record.name} : ${record.keys.map(_.description).filterNot(_.isEmpty)}"))
      
      val entries =
         Source.fromFile(args(0)).getLines().toList
            .filterNot(_.startsWith("#"))
            .map(line => try{ ConfigFactory.parseString(line) } catch { case th : Throwable => println(line); throw th})
            .map { c => HumbleBundleLibraryEntry(c) }
            .filterNot(_.ignore)
            .filter(_.id.isEmpty)
            .filter(_.`type` != "bundle")
            .filter(_.`type` != "dlc")
            .filter(_.`type` != "book")
            .filter(_.`type` != "music")
            .filter(_.`type` != "video")
      
      val accessor = new IGDBAccessor("2259e85f479bb30c5a7ae1ce27eaa478")
      println(s"${entries.head.name} : ${entries.head.keys.map(_.description)}")
      accessor.getGamesByName(entries.head.name).foreach { record =>
         println(s"${record.toString.replaceAll(",",", ")}")
      }
   }
}
