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

/*  
    These streams are used to obtain test datas.
    We assume that in a real application, the `events` Enumerator is provided by an external streaming service (like HTTP streaming)
*/
object Streams {
  implicit val timeout = Timeout(5 seconds)

  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): Event = {
      val eventType = (json \ "type").as[String]
      val car = (json \ "car").as[String]
      eventType match {
        case "speed" =>
          val speed = (json \ "value").as[Int]
          SpeedEvent(car, speed)
        case "dist" =>
          val dist = (json \ "value").as[Double] * 1000
          DistEvent(car, dist)
        case "pos" =>
          val longitude = (json \ "long").as[Double]
          val latitude = (json \ "lat").as[Double]
          PositionEvent(car, latitude, longitude)
      }
    }

    def writes(e: Event): JsValue = e match {
      case SpeedEvent(car, speed) =>
        JsObject(List(
          "type" -> JsString("speed"),
          "car" -> JsString(car),
          "value" -> JsNumber(speed)
        ))
      case DistEvent(car, dist) =>
        JsObject(List(
          "type" -> JsString("dist"),
          "car" -> JsString(car),
          "value" -> JsNumber(dist/1000)
        ))
      case PositionEvent(car, latitude, longitude) =>
        JsObject(List(
          "type" -> JsString("pos"),
          "car" -> JsString(car),
          "value" -> JsObject(List(
            "latitude" -> JsNumber(latitude),
            "longitude" -> JsNumber(longitude)
            )
          )
        ))
      case StatSpeedEvent(statType, car, speed) =>
        JsObject(List(
            "type" -> JsString(statType),
            "car" -> JsString(car),
            "value" -> JsNumber(speed.toInt)
        ))
      case RankingEvent(car, position) =>
        JsObject(List(
            "type" -> JsString("rank"),
            "car" -> JsString(car),
            "value" -> JsNumber(position)
        ))
    }
  }

  trait Event

  trait RTEvent extends Event
  case class SpeedEvent(car: String, speed: Int) extends RTEvent
  case class DistEvent(car: String, dist: Double) extends RTEvent
  case class PositionEvent(car: String, latitude: Double, longitude: Double) extends RTEvent

  trait StatEvent extends Event
  case class StatSpeedEvent(statType:String, car:String, value:Double) extends StatEvent
  case class RankingEvent(car:String, position:Int) extends StatEvent

    def position(actor:ActorRef)=Enumerator.fromCallback[Event]{()=>
        Promise.timeout("",randomInt(2000,3000) milliseconds).flatMap{str=>
            (actor ? "getState").mapTo[Car].asPromise.map{car=>
                Some(PositionEvent(
                    car.label,
                    car.point.position.latitude,
                    car.point.position.longitude
                ))
            }
        }
    }

    def distance(actor:ActorRef)=Enumerator.fromCallback[Event]{()=>
        Promise.timeout("",randomInt(2000,3000) milliseconds).flatMap{str=>
            (actor ? "getState").mapTo[Car].asPromise.map{car=>
                Some(DistEvent(
                    car.label,
                    car.totalDist
                ))
            }
        }
    }

    def speed(actor:ActorRef)=Enumerator.fromCallback[Event]{()=>
        Promise.timeout("",randomInt(2000,3000) milliseconds).flatMap{str=>
            (actor ? "getState").mapTo[Car].asPromise.map{car=>
                Some(SpeedEvent(
                    car.label,
                    car.speed.toInt
                ))
            }
        }
    }
  

  val allPositions:Enumerator[Event] = 
        carActors.map(position).foldLeft(Enumerator[Event]())((acc,enum)=>acc.interleave(enum))

  val allDistances:Enumerator[Event] = 
        carActors.map(distance).foldLeft(Enumerator[Event]())((acc,enum)=>acc.interleave(enum))

  val allSpeeds:Enumerator[Event] = 
        carActors.map(speed).foldLeft(Enumerator[Event]())((acc,enum)=>acc.interleave(enum))

  lazy val events = allPositions >- allDistances >- allSpeeds


        
}