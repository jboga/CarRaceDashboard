package models

import akka.actor._
import models.DB._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.Logger
import scala.concurrent.duration._
import models.Events._
import scala.language.postfixOps
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.RawCommand
import scala.concurrent.Future
import scala.util.Success

import scala.concurrent.ExecutionContext.Implicits.global

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
        aggregatedSpeed(BSONDocument("$avg"->BSONString("$speed"))).onComplete{
          case Success(Some(carsWithAvg)) =>
            carsWithAvg
              .map((value)=>StatSpeedEvent("avgSpeed",value._1,value._2))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case _ =>
        }

      case "maxSpeed"=>
        aggregatedSpeed(BSONDocument("$max"->BSONString("$speed"))).onComplete{
          case Success(Some(carsWithMax)) =>
            carsWithMax
              .map((value)=>StatSpeedEvent("maxSpeed",value._1,value._2))
              .foreach{event=>
                logger.debug("New stat : "+event)
                Akka.system.eventStream.publish(event)
              }
          case _ =>
        }

      case "ranking" =>
        aggregatedMaxDist.onComplete{
          case Success(Some(dist)) =>
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
        db("events").drop
    }


    // Execute a aggregate command in Mongo
    import scala.reflect.Manifest
    private def aggregatedSpeed(value: BSONDocument):Future[Option[Seq[(String,Double)]]]=
      db.command(RawCommand(
        BSONDocument(
          "aggregate"-> BSONString("events"),
          "pipeline" -> BSONArray(
            BSONDocument(
              "$match" -> BSONDocument("type" -> BSONString("speed"))
            ),
            BSONDocument(
              "$group" -> BSONDocument(
                "_id" -> BSONString("$car"),
                "value" -> value
              )
            )
          )
        )
      )).map{result=>
        result.getAs[BSONArray]("result") match {
          case Some(result) =>
            Some(
              result.values.toList
                .map(v=>v.asInstanceOf[TraversableBSONDocument])
                .map(v=>
                    (v.getAs[BSONObjectID]("_id").get.stringify,
                      v.getAs[BSONDouble]("value").get.value)
                )
            )
          case c => 
            None
        }
      }
    
    private def aggregatedMaxDist:Future[Option[Seq[(String,Double)]]]=
      db.command(RawCommand(
        BSONDocument(
          "aggregate"-> BSONString("events"),
          "pipeline" -> BSONArray(
            BSONDocument(
              "$match" -> BSONDocument("type" -> BSONString("dist"))
            ),
            BSONDocument(
              "$group" -> BSONDocument(
                "_id" -> BSONString("$car"),
                "value" -> BSONDocument("$max"->BSONString("$dist"))
              )
            )
          )
        )
      )).map{result=>
        result.getAs[BSONArray]("result") match {
          case Some(result) =>
            Some(
              result.values.toList
                .map(v=>v.asInstanceOf[TraversableBSONDocument])
                .map(v=>
                    (v.getAs[BSONObjectID]("_id").get.stringify,
                      v.getAs[BSONDouble]("value").get.value)
                  )
            )
          case c => 
            None
        }
      }



    }))
	
}