package models

import com.mongodb.casbah.{MongoDB, MongoConnection}
import play.api.Play._

object DB {

  // Get a MongoDB connection based on application.conf
  lazy val connection =
    MongoConnection(
      configuration.getString("mongo.host").getOrElse("127.0.0.1"),
      configuration.getInt("mongo.port").getOrElse(27017)
      )(configuration.getString("mongo.dbname").getOrElse("race"))


}
