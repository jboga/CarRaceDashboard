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

  // 3 types of message are sent to this actor at fixed interval : avgSpeed, maxSpeed and ranking
  // For each message, the actor computes the statistic (by using Mongo DB Aggregation Framework) and publishes a StatEvent on the eventStream
  val actor =  Akka.system.actorOf(Props(new Actor {

    var stopAvg:Option[Cancellable]=None
    var stopRank:Option[Cancellable]=None
    var stopMax:Option[Cancellable]=None

    def receive = {

      case "avgSpeed"=>
        aggregatedSpeed[Double](MongoDBObject("$avg"->"$speed")) match {
          case Some(carsWithAvg) =>
            carsWithAvg
              .map((value)=>StatSpeedEvent("avgSpeed",value._1,value._2))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case None =>
        }

      case "maxSpeed"=>
        aggregatedSpeed[Int](MongoDBObject("$max"->"$speed")) match {
          case Some(carsWithMax) =>
            carsWithMax
              .map((value)=>StatSpeedEvent("maxSpeed",value._1,value._2))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case None =>
        }

      case "ranking" =>
        aggregatedMaxDist match {
          case Some(dist) =>
            dist
              .sortWith((x,y)=>x._2 > y._2)
              .map(_._1)
              .zipWithIndex
              .map(rank=>RankingEvent(rank._1,rank._2+1))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case _ =>
        }


      case "start" =>
        stopRank=Some(context.system.scheduler.schedule(3 seconds,3 seconds,self,"ranking"))
        stopAvg=Some(context.system.scheduler.schedule(5 seconds,5 seconds,self,"avgSpeed"))
        stopMax=Some(context.system.scheduler.schedule(7 seconds,7 seconds,self,"maxSpeed"))

      case "stop" =>
        stopRank.map(_.cancel)
        stopAvg.map(_.cancel)
        stopMax.map(_.cancel)
    }


    // Execute a aggregate command in Mongo
    import scala.reflect.Manifest
    private def aggregatedSpeed[T: Manifest](value: MongoDBObject):Option[Seq[(String,T)]]=
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
    
    private def aggregatedMaxDist:Option[Seq[(String,Double)]]=
      connection.command(
        MongoDBObject(
          "aggregate"->"events",
          "pipeline" -> MongoDBList(
            MongoDBObject(
              "$match" -> MongoDBObject("type" -> "dist")
            ),
            MongoDBObject(
              "$group" -> MongoDBObject(
                "_id" -> "$car",
                "value" -> MongoDBObject("$max"->"$dist")
              )
            )
          )
        )
      ).get("result") match {
          case result:BasicDBList =>
            Some(result.map(_.asInstanceOf[BasicDBObject]).map(v=>(v.getAs[String]("_id").get,v.getAs[Double]("value").get)))
          case c => 
            None
        }



    }))
	
}