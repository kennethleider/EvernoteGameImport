package igdb

import java.net.URLEncoder

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

import scala.io.Source
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

case class IGDBRecord(id : Int, name : String, url : String, parent : Option[Int], versionParent : Option[Int])

class IGDBAccessor(key : String) {
   def getGamesByName(name: String) = {
      val url = s"""https://api-endpoint.igdb.com/games/?search=${URLEncoder.encode(name, "utf8")}&fields=id,name,url,game,version_parent"""
      val get = new HttpGet(url)
      get.addHeader("user-key", key)
      get.addHeader("Accept", "application/json")
      val response = HttpClients.createDefault().execute(get)
      val body = Source.fromInputStream(response.getEntity.getContent).mkString
      JSON.parseRaw(body)
         .map { _.asInstanceOf[JSONArray].list }
         .get
         .map {_.asInstanceOf[JSONObject]}
         .map{_.obj}
         .map { map => IGDBRecord(map("id").toString.toDouble.toInt,
            map("name").toString,
            map("url").toString,
            map.get("game").map(_.toString.toDouble.toInt),
            map.get("version_parent").map(_.toString.toDouble.toInt)) }
   }
   
   
   def getGameBySteamId(id : Int) = {
      val url = s"https://api-endpoint.igdb.com/games/?filter[external.steam][eq]=${id}&fields=id,name,url,game,version_parent"
      //println(url)
      val get = new HttpGet(url)
      get.addHeader("user-key", key)
      get.addHeader("Accept", "application/json")
      val response = HttpClients.createDefault().execute(get)
      val body = Source.fromInputStream(response.getEntity.getContent).mkString
      //println(body)
      JSON.parseRaw(body)
         .map { _.asInstanceOf[JSONArray].list }
         .get
         .map {_.asInstanceOf[JSONObject]}
         .map{_.obj}
         .map { map => IGDBRecord(map("id").toString.toDouble.toInt,
            map("name").toString,
            map("url").toString,
            map.get("game").map(_.toString.toDouble.toInt),
            map.get("version_parent").map(_.toString.toDouble.toInt)) }
   }
}
