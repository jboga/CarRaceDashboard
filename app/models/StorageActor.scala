package models

import akka.actor._
import models.DB._
import models.Events._
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers._
import reactivemongo.core.commands._

import scala.concurrent.ExecutionContext.Implicits.global

// This actor receives each event and stores them in MongoDB for later use (statistics)
// It publishes each event in the eventStream for other subscribed listeners
object StorageActor{

  val collection = db("events")
	
  val logger=Logger("actor-storage")

  val actor =  Akka.system.actorOf(Props(new Actor {

    implicit object EventWriter extends BSONWriter[Event] {
      def toBSON(event: Event) = 
        event match {
          case event:SpeedEvent =>
            BSONDocument(
              "type" -> BSONString("speed"),
              "car" -> BSONString(event.car),
              "speed" -> BSONInteger(event.speed)
            )  
          case event:DistEvent =>
            BSONDocument(
              "type" -> BSONString("dist"),
              "car" -> BSONString(event.car),
              "dist" -> BSONDouble(event.dist)
            )
          case event:PositionEvent => 
            BSONDocument(
              "type" -> BSONString("pos"),
              "car" -> BSONString(event.car),
              "lat" -> BSONDouble(event.latitude),
              "long" -> BSONDouble(event.longitude)
            )
          case _ => BSONDocument()
        }
    }

    def receive = {
      case e:Event => 
        logger.debug("New event : "+e)
        Akka.system.eventStream.publish(e)
        collection.insert(e).map{
          case LastError(false,_,_,errorMessage,_) =>
            logger.error("An error occured on insert : %s".format(errorMessage.getOrElse("no error message")))
          case _ =>
        }
    }

    override def preStart={
      // Clean collection on startup
      db("events").drop()
    }

  }), name = "storage")

}