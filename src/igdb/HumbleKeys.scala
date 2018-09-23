package igdb

import java.io.File
import java.time.Instant

import com.typesafe.config.ConfigFactory

import scala.io.Source

object HumbleKeys {
   def main(args: Array[String]): Unit = {
      val parsedKeys = new File(args(0)).listFiles().flatMap { file =>
         println(file)
         val platformExpression = ".*<td class=\"platform\".*title=\"(.*)\".*".r
         val nameExpression = ".*<h4 title=.*?>(.*) Key</h4.*".r
         val nameExpression2 = ".*<h4 title=.*?>(.*)</h4.*".r
         val keyExpression = ".*<div class=\"keyfield-value\">(.*)</div>.*".r
   
         val lines = Source.fromFile(file).getLines().toList
         val matches = lines.splitAt(lines.indexWhere(_.contains("<h1>Keys</h1>")))._2
            .collect {
               case platformExpression(platform) => platform.toLowerCase()
               case nameExpression(name) => name
               case nameExpression2(name) => name
               case keyExpression(key) => key
            }
            .grouped(3)
            .map{i => (i(0), i(1), i(2))}
            .toList
         
         if (matches.size != 20) {
            println("WARN: Expected 20 keys in the file")
         }

         matches
      }
   
      val existingKeys = if (new File(args(1)).exists()) {
         Source.fromFile(args(1)).getLines().toList
            .filterNot(_.startsWith("#"))
            .map(ConfigFactory.parseString)
            .map(c => (c.getConfig("key").getString("platform"), c.getString("name"), c.getConfig("key").getString("key")))
      } else {
         List()
      }
   
      val newKeys = parsedKeys
         .filterNot { t => existingKeys.contains(t) }
         .map { t => s"""{ name = "${t._2.replace("\"","\\\"")}", key = ${Key("humble", t._1, t._3).toConfigString()} }""" }
      
      parsedKeys
         .map(_._3)
         .groupBy(key => key)
         .mapValues(_.length)
         .filter(_._2 > 1)
         .keys
         .foreach { key => println(s"WARN : '${key}' exists multiple times")}

      println(s"Copy new keys in ${args(1)}")
      println(s"# Keys generated at ${Instant.now}")
      newKeys.foreach(println)
      
//      val writer = new FileWriter(args(1), true)
//      parsedKeys
//         .foreach(t => writer.write()
//      writer.close()
     
   
   }
}
