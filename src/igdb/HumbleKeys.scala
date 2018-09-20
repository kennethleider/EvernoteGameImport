package igdb

import java.io.{File, FileWriter, PrintWriter}

import scala.io.Source

object HumbleKeys {
   def main(args: Array[String]): Unit = {
      val allKeys = new File(args(0)).listFiles().flatMap { file =>
         println(file)
         val platformExpression = ".*<td class=\"platform\".*title=\"(.*)\".*".r
         val nameExpression = ".*<h4 title=\"(.*) Key\".*".r
         val nameExpression2 = ".*<h4 title=\"(.*)\".*".r
         val keyExpression = ".*<div class=\"keyfield-value\">(.*)</div>.*".r
   
         val lines = Source.fromFile(file).getLines().toList
         val matches = lines.splitAt(lines.indexWhere(_.contains("<h1>Keys</h1>")))._2
            .collect {
               case platformExpression(platform) => platform
               case nameExpression(name) => name
               case nameExpression2(name) => name
               case keyExpression(key) => key
            }
            .grouped(3)
            .map{i => println(i); (i(0), i(1), i(2))}
            .toList
         
         if (matches.size != 20) {
            println("WARN: Expected 20 keys in the file")
         }

         matches
      }
      
      allKeys
         .map(_._3)
         .groupBy(key => key)
         .mapValues(_.length)
         .filter(_._2 > 1)
         .keys
         .foreach { key => println(s"WARN : ${key} exists multiple times")}

      val writer = new FileWriter(args(1), true)
      allKeys
         .foreach(t => writer.write(s""""${t._2}", "${t._1}", "${t._3}"\n"""))
      writer.close()
     
   
   }
}
