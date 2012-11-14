package models

import akka.actor._
import models.DB._
import com.mongodb.casbah.Imports._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.Logger
import akka.util.duration._
import models.Streams._

object StatsActor{

  private val logger = Logger("actor-stats")

  val actor =  Akka.system.actorOf(Props(new Actor {

    def receive = {

      case "meanSpeed"=>
        // Pipeline is [{$match: {type: "speed"}},{$group: {_id: "$car", mean: {$avg: "$speed"}}}]
        aggregateSpeed[Double](MongoDBObject("$avg"->"$speed")) match {
          case Some(carsWithAvg) =>
            carsWithAvg
              .map((value)=>StatSpeedEvent("meanSpeed",value._1,value._2))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case None =>
        }
        context.system.scheduler.scheduleOnce(5 seconds,self,"meanSpeed")

      case "maxSpeed"=>
        // Pipeline is [{$match: {type: "speed"}},{$group: {_id: "$car", mean: {$max: "$speed"}}}]
        aggregateSpeed[Int](MongoDBObject("$max"->"$speed")) match {
          case Some(carsWithAvg) =>
            carsWithAvg
              .map((value)=>StatSpeedEvent("maxSpeed",value._1,value._2))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case None =>
        }
        context.system.scheduler.scheduleOnce(5 seconds,self,"maxSpeed")

      case "start" =>
        self ! "meanSpeed"
        self ! "maxSpeed"
    }


    import scala.reflect.Manifest
    private def aggregateSpeed[T: Manifest](value: MongoDBObject):Option[Seq[(String,T)]]=
      connection.command(
        MongoDBObject(
          "aggregate"->"events",
          "pipeline" -> MongoDBList(
            MongoDBObject(
              "$match" -> MongoDBObject("type" -> "speed")
            ),
            MongoDBObject(
              "$group" -> MongoDBObject(
                "_id" -> "$car",
                "value" -> value
              )
            )
          )
        )
      ).get("result") match {
          case result:BasicDBList =>
            Some(result.map(_.asInstanceOf[BasicDBObject]).map(v=>(v.getAs[String]("_id").get,v.getAs[T]("value").get)))
          case c => 
            None
        }
    }))
	
}