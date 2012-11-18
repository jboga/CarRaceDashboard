package models

import com.mongodb.casbah.{MongoDB, MongoConnection}
import play.api.Play._

object DB {

  lazy val connection =
    configuration.getString("mongo.user").flatMap{username=>configuration.getString("mongo.password").flatMap{password=>Some(username,password)}}
      .map{credentials=>
        db:MongoDB=>
          db.authenticate(credentials._1,credentials._2)
          db
      }.getOrElse(identity[MongoDB](_)).apply(
        MongoConnection(
          configuration.getString("mongo.host").getOrElse("127.0.0.1"),
          configuration.getInt("mongo.port").getOrElse(27017)
        )(configuration.getString("mongo.dbname").getOrElse("todos"))
      )

}
