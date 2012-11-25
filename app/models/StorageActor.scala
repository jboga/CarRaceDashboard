package models

import akka.actor._
import models.DB._
import com.mongodb.casbah.Imports._
import models.Events._
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current

// This actor receives each event and stores them in MongoDB for later use (statistics)
// It publishes each event in the eventStream for other subscribed listeners
object StorageActor{
	
  val logger=Logger("actor-storage")

  val actor =  Akka.system.actorOf(Props(new Actor {

    def receive = {
      case e:Event => 
        logger.debug("New event : "+e)
        Akka.system.eventStream.publish(e)
        connection("events").insert(  e match {
            case SpeedEvent(car,speed) =>
              MongoDBObject (
                "type" -> "speed",
                "car" -> car,
                "speed" -> speed
              )
            case DistEvent(car,dist) =>
              MongoDBObject (
                "type" -> "dist",
                "car" -> car,
                "dist" -> dist
              )
            case PositionEvent(car,latitude,longitude) =>
              MongoDBObject (
                "type" -> "pos",
                "car" -> car,
                "lat" -> latitude,
                "long" -> longitude
              )
          }
        )
    }

    override def preStart={
      // Clean collection on startup
      connection("events").dropCollection
    }

  }), name = "storage")

}