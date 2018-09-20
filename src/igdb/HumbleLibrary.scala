package igdb

import java.io.{File, FileWriter}

import com.typesafe.config.ConfigFactory

import scala.io.Source


case class Key(platform : String, key : String) {
   def toConfigString() = s"""{ platform : $platform, key : $key }"""
}
case class Download(what : String, where : String) {
   def toConfigString() = s"""{ what : $what, where : $where }"""
}
case class HumbleBundleLibraryEntry(name : String, ignore : Boolean, processed : Boolean, keys : Seq[Key], downloads : Seq[Download]) {
   def toConfigString() = {
      val keyString = keys.map(_.toConfigString).mkString("[",",","]")
      val downloadString = downloads.map(_.toConfigString).mkString("[",",","]")
         s"""{ name : "$name", ignore : $ignore, processed : $processed, keys : $keyString, downloads : $downloadString}""".stripMargin
   }
}

object HumbleLibrary {
   def main(args: Array[String]): Unit = {
      val entries = Source.fromFile(args(0)).getLines().toList
         .map(HumbleBundleLibraryEntry(_, false, false, Seq(), Seq()))


      val keys = Source.fromFile(args(1)).getLines().toList
         .map(_.split("\", \"").toList)
         .map(t => t(0).tail -> Key(t(1), t(2).substring(0, t(2).length - 1)))
         .groupBy(_._1)
         .mapValues(_.map(_._2))

      entries.foreach { entry =>
         println(s"${entry.name} ${keys.get(entry.name)}")
      }

      val entryNames = entries.map(_.name).toSet
      keys.filterNot(t => entryNames.contains(t._1))
         .foreach(println)
//   println(keys)
//      println(entries)
//         .map(_.toConfigString)
//         .mkString("{library = [\n   ", ",\n   ", "\n]}")
//      println(ConfigFactory.parseString(formatted))
   }
}
