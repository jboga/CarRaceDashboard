package models

import akka.actor._
import models.DB._
import com.mongodb.casbah.Imports._
import models.Streams._
import play.Logger
import play.api.libs.concurrent.Akka

object Storage {

  val storeActor = ActorSystem("RaceSystem").actorOf(Props(new Actor {

    def receive = {
      case e: Event =>
        context.system.eventStream.publish(e)
        connection("events").insert(e match {
          case SpeedEvent(car, speed) =>
            MongoDBObject(
              "type" -> "speed",
              "car" -> car,
              "speed" -> speed
            )
          case DistEvent(car, dist) =>
            MongoDBObject(
              "type" -> "dist",
              "car" -> car,
              "dist" -> dist
            )
          case PositionEvent(car, latitude, longitude) =>
            MongoDBObject(
              "type" -> "pos",
              "car" -> car,
              "lat" -> latitude,
              "long" -> longitude
            )
        }
        )
    }
  }), name = "storage"
  )
}