package simulation

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import scala.concurrent.duration._
import models.Events._
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

/*  
    These streams are used to obtain test datas.
    We assume that in a real application, the `events` Enumerator is provided by an external streaming service (like HTTP streaming)
*/
class Streams(race: Race) {

  implicit val timeout = Timeout(5 seconds)

  // Enumerators which produce events (Position,Speed and Distance) based on a Car actor
  val period = 1 seconds

  def enumeratorFromCar(actor: ActorRef, f: Car => Event) = Enumerator.generateM[Event] {
    Promise.timeout("", period).flatMap {
      str =>
        (actor ? "getState").mapTo[Option[Car]].map(_.map(f))
    }
  }

  def position(actor: ActorRef) = enumeratorFromCar(actor,
    car =>
      PositionEvent(
        car.label,
        car.point.position.latitude,
        car.point.position.longitude
      )
  )

  def distance(actor: ActorRef) = enumeratorFromCar(actor,
    car =>
      DistEvent(
        car.label,
        car.totalDist
      )
  )


  def speed(actor: ActorRef) = enumeratorFromCar(actor,
    car =>
      SpeedEvent(
        car.label,
        car.speed.toInt
      )
  )


  // We interleave enumerators for all actors to obtain a stream with all cars for each event type
  val allPositions: Enumerator[Event] =
    race.carActors.map(position).reduce((acc, enum) => acc >- enum)

  val allDistances: Enumerator[Event] =
    race.carActors.map(distance).reduce((acc, enum) => acc >- enum)

  val allSpeeds: Enumerator[Event] =
    race.carActors.map(speed).reduce((acc, enum) => acc >- enum)

  // Finally, we interleave all event types to obtain a stream of all events from all cars
  lazy val events = allPositions >- allDistances >- allSpeeds


}