package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import models.Race._
import scala.util.Random
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import akka.actor._
import play.api.libs.json._
import play.api.libs.json.Json._
import akka.util.duration._


object Streams{
  // JSON representation of events
  implicit object EventFormat extends Writes[Event] {
    def writes(e: Event): JsValue = e match {
      case SpeedEvent(car, speed) => 
        jsonEvent("speed",car,JsNumber(speed))
      case DistEvent(car, dist) =>
        jsonEvent("dist",car,JsNumber(dist/1000))
      case PositionEvent(car, latitude, longitude) =>
        jsonEvent("pos",car,
          JsObject(List(
            "latitude" -> JsNumber(latitude),
            "longitude" -> JsNumber(longitude)
            )
          )
        )
      case StatSpeedEvent(statType, car, speed) =>
        jsonEvent(statType,car,JsNumber(speed.toInt))
      case RankingEvent(car, position) =>
        jsonEvent("rank",car,JsNumber(position))
    }
    private def jsonEvent(eventType:String,car:String, value:JsValue)=
      JsObject(List(
        "type" -> JsString(eventType),
        "car" -> JsString(car),
        "value" -> value
      ))
  }

  // Definition of events
  trait Event

  // Some are real time events
  trait RTEvent extends Event
  case class SpeedEvent(car: String, speed: Int) extends RTEvent
  case class DistEvent(car: String, dist: Double) extends RTEvent
  case class PositionEvent(car: String, latitude: Double, longitude: Double) extends RTEvent

  // Others are events from statistics
  trait StatEvent extends Event
  case class StatSpeedEvent(statType:String, car:String, value:Double) extends StatEvent
  case class RankingEvent(car:String, position:Int) extends StatEvent
}

/*  
    These streams are used to obtain test datas.
    We assume that in a real application, the `events` Enumerator is provided by an external streaming service (like HTTP streaming)
*/
class Streams(race:Race) {
  import models.Streams._

  implicit val timeout = Timeout(5 seconds)

  // Enumerators which produce events (Position,Speed and Distance) based on a Car actor
  val period = 1 seconds

  def position(actor:ActorRef)=Enumerator.fromCallback[Event]{()=>
    Promise.timeout("",period).flatMap{str=>
      (actor ? "getState").mapTo[Option[Car]].asPromise.map(
        _.map(car=>
          PositionEvent(
            car.label,
            car.point.position.latitude,
            car.point.position.longitude
          )
        )
      )
    }
  }

  def distance(actor:ActorRef)=Enumerator.fromCallback[Event]{()=>
    Promise.timeout("",period).flatMap{str=>
      (actor ? "getState").mapTo[Option[Car]].asPromise.map(
        _.map(car=>
          DistEvent(
            car.label,
            car.totalDist
          )
        )
      )
    }
  }

  def speed(actor:ActorRef)=Enumerator.fromCallback[Event]{()=>
    Promise.timeout("",period).flatMap{str=>
      (actor ? "getState").mapTo[Option[Car]].asPromise.map(
        _.map(car=>
          SpeedEvent(
            car.label,
            car.speed.toInt
          )
        )
      )
    }
  }
  
  // We interleave enumerators for all actors to obtain a stream with all cars for each event type
  val allPositions:Enumerator[Event] = 
        race.carActors.map(position).foldLeft(Enumerator[Event]())((acc,enum)=>acc.interleave(enum))

  val allDistances:Enumerator[Event] = 
        race.carActors.map(distance).foldLeft(Enumerator[Event]())((acc,enum)=>acc.interleave(enum))

  val allSpeeds:Enumerator[Event] = 
        race.carActors.map(speed).foldLeft(Enumerator[Event]())((acc,enum)=>acc.interleave(enum))

  // Finally, we interleave all event types to obtain a stream of all events from all cars
  lazy val events = allPositions >- allDistances >- allSpeeds


        
}