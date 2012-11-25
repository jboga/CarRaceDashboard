package models

import play.api.libs.json._
import play.api.libs.json.Json._

object Events{

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
}