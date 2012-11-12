package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import models.Race._
import scala.util.Random
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import akka.actor._

/*  
    These streams are used to obtain test datas.
    We assume that in a real application, the `events` Enumerator is provided by an external streaming service (like HTTP streaming)
*/
object Streams{
    implicit val timeout = Timeout(5 seconds) 

    trait Event

    case class SpeedEvent(car:String,speed:Int) extends Event
    case class DistEvent(car:String,dist:Double) extends Event
    case class PositionEvent(car:String,latitude:Double,longitude:Double) extends Event

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