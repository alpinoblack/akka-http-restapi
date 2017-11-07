package courses.restapi.core.storage

import courses.restapi.utils.Config
import org.mongodb.scala.{MongoClient, MongoDatabase}

object MongoStorage {

  val uri: String = Config.load().database.connectionString
  System.setProperty("org.mongodb.async.type", "netty")

  val client: MongoClient = MongoClient (uri)
  val db: MongoDatabase = client.getDatabase("test")
}

 trait MongoStorage {
  val db: MongoDatabase = MongoStorage.db
}