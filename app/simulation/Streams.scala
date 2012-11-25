package simulation

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import simulation.Race._
import scala.util.Random
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import akka.actor._
import akka.util.duration._
import models.Events._

/*  
    These streams are used to obtain test datas.
    We assume that in a real application, the `events` Enumerator is provided by an external streaming service (like HTTP streaming)
*/
class Streams(race:Race) {
  
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