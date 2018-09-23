package igdb

import java.io.{File, FileWriter}
import java.time.Instant

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

import scala.io.Source
import scala.collection.JavaConverters._
import scala.util.Try

object Key {
   def apply(c : Config) : Key = {
      Key(c.getString("source"), c.getString("platform"), c.getString("key"), c.getString("type"), c.getString("description"), c.getString("status"))
   }
}

case class Key(source : String, platform : String, key : String, keyType : String = "game", description : String = "", status : String = "unknown") {
   def toConfigString() = s"""{ source = "$source", platform = "$platform", key = "$key", type = "$keyType", description = ${ConfigValueFactory.fromAnyRef(description).render()}, status = "$status"}"""
}
case class Download(what : String, where : String) {
   def toConfigString() = s"""{ what = "$what", where = "$where" }"""
}

object HumbleBundleLibraryEntry {
   def apply(c : Config) : HumbleBundleLibraryEntry = {
      HumbleBundleLibraryEntry(c.getString("name"), c.getString("id"), Try(c.getString("type")).getOrElse("game"), c.getBoolean("ignore"), c.getBoolean("processed"), c.getConfigList("keys").asScala.map(Key.apply), Seq())
   }
}

case class HumbleBundleLibraryEntry(name : String, id : String, `type` : String, ignore : Boolean, processed : Boolean, keys : Seq[Key], downloads : Seq[Download]) {
   def toConfigString() = {
      val keyString = keys.map(_.toConfigString).mkString("[",",","]")
      val downloadString = downloads.map(_.toConfigString).mkString("[",",","]")
         s"""{ name = ${ConfigValueFactory.fromAnyRef(name).render()}, id = "$id", ignore = $ignore, processed = $processed, keys = $keyString, downloads = $downloadString}""".stripMargin
   }
}

object HumbleLibrary {
   def main(args: Array[String]): Unit = {
      val entries = Source.fromFile(args(0)).getLines().toList
         .map(HumbleBundleLibraryEntry(_, "", "game",false, false, Seq(), Seq()))
   
      val keys : Map[String, List[Key]] = if (new File(args(1)).exists()) {
         Source.fromFile(args(1)).getLines().toList
            .filterNot(_.startsWith("#"))
            .map(line => try{ ConfigFactory.parseString(line) } catch { case th : Throwable => println(line); throw th})
            .map { c =>c.getString("name") -> Key(c.getConfig("key")) }
            .groupBy(_._1)
            .mapValues(_.map(_._2))
      } else {
         Map()
      }
      
      val entryNames = entries.map(_.name).toSet
      keys.filterNot(t => entryNames.contains(t._1))
         .foreach(t => println(s"'${t._1}' looks to be a bad name for a key"))
   
   
      println(s"Copy new keys in ${args(1)}")
      println(s"# Games generated at ${Instant.now}")
      entries
         .map { entry =>
            val newKeys = keys.getOrElse(entry.name, List())
            entry.copy(keys = (entry.keys ++ newKeys).distinct)
         }
         .map(_.toConfigString)
         .foreach(println)
   }
}
