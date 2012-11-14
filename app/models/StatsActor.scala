package models

import akka.actor._
import models.DB._
import com.mongodb.casbah.Imports._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.Logger
import akka.util.duration._

object StatsActor{

  case class SpeedStatEvent(statType:String, value:Double)

  private val logger = Logger("actor-stats")

  val actor =  Akka.system.actorOf(Props(new Actor {
    
    def receive = {

      case "speedMean"=>
        aggregateOne[Double]("mean") {
          MongoDBList(
            MongoDBObject(
              "$group" -> MongoDBObject(
                "_id" -> "$type",
                "mean" -> MongoDBObject("$avg"->"$speed")
              )
            ),
            MongoDBObject(
              "$match" -> MongoDBObject("_id" -> "speed")
            )
          )
        }.map(value=>SpeedStatEvent("mean",value)) match {
          case Some(event) =>
            logger.debug("New stat : "+event)
            Akka.system.eventStream.publish(event)
          case _ =>
        }
        context.system.scheduler.scheduleOnce(5 seconds,self,"speedMean")

      case "speedMax"=>
        aggregateOne[Int]("max") {
          MongoDBList(
            MongoDBObject(
              "$group" -> MongoDBObject(
                "_id" -> "$type",
                "max" -> MongoDBObject("$max"->"$speed")
              )
            ),
            MongoDBObject(
              "$match" -> MongoDBObject("_id" -> "speed")
            )
          )
        }.map(value=>SpeedStatEvent("max",value)) match {
          case Some(event) =>
            logger.debug("New stat : "+event)
            Akka.system.eventStream.publish(event)
          case _ =>
        }
        context.system.scheduler.scheduleOnce(5 seconds,self,"speedMax")

      case "start" =>
        self ! "speedMean"
        self ! "speedMax"
    }


    import scala.reflect.Manifest
    private def aggregateOne[T: Manifest](key:String)(pipeline: MongoDBList):Option[T]=
      connection.command(
        MongoDBObject(
          "aggregate"->"events",
          "pipeline" -> pipeline
        )
      ).get("result") match {
          case result:BasicDBList if result.size > 0 =>
            result.head.asInstanceOf[BasicDBObject].getAs[T](key)
          case _ => 
            None
        }
    }))
	
}