package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.util.Random
import akka.actor._
import akka.util.duration._
import models.TrackParser._
import math._

/*
  Represent a race, and states of cars.
  Used to get some test datas.
*/
object Race {

  case class Position(latitude: Double, longitude: Double)

  case class CheckPoint(id: Int, position: Position)

  type Track = List[CheckPoint]

  val period = 1 // in second

  // Represent a Car at a specific point of the track
  case class Car(label: String, point: CheckPoint = track.head, speed: Int, totalDist: Double)

  // Load track from kml (list of checkpoints)
  private lazy val track: Track = readTrack("public/tracks/LeMans.kml")

  lazy val lapLength: Double = lapLength(track)

  def lapLength(track: Track) =
    (for{
      List(p1,p2) <- track.sliding(2)
    } yield computeDistance(p1.position,p2.position)).sum


  // Get next checkpoint, based on track
  private def nextTrackPoint(point: CheckPoint) =
    if (point.id + 1 >= track.size)
      track.head // new lap
    else
      track(point.id + 1)

  //return new CheckPoint on the track at distance d from point
  private def next(point: CheckPoint, distance: Double): CheckPoint = {
    val nextPoint = nextTrackPoint(point)
    val distanceBetween = computeDistance(point.position, nextPoint.position)
    if (distance < distanceBetween)
      CheckPoint(point.id, computePosition(point.position, nextPoint.position, distance))
    else
      next(nextPoint, (distance - distanceBetween))
  }

  // Compute the distance between two position
  def computeDistance(pos1: Position, pos2: Position): Double =
    acos(
      sin(toRadians(pos1.latitude)) * sin(toRadians(pos2.latitude))
        + cos(toRadians(pos1.latitude)) * cos(toRadians(pos2.latitude)) * cos(toRadians(pos1.longitude)
        - toRadians(pos2.longitude))
    ) * 6366000

  //return a position between point1 and point2 at distance d from point1
  def computePosition(point1: Position, point2: Position, d: Double): Position = {
    val distanceBetween = computeDistance(point1, point2)
    val ratio = d / distanceBetween
    Position(point1.latitude * (1 - ratio) + point2.latitude * ratio, point1.longitude * (1 - ratio) + point2.longitude * ratio)
  }

  // List of concurrents
  private val cars = List(
    "Car 1",
    "Car 2",
    "Car 3"
  )

  // "Race" stream for a car. Each value is a state of the car `car` at a time t of the race.
  def raceStream(car: Car): Stream[Car] = {
    def loop(prev: Car): Stream[Car] = {
      val speed = 
        // Add/remove a random number to current speed, but with guard
        prev.speed + randomInt(-10,10) match {
          case s if s < 80  => 80
          case s if s > 200 => 200
          case s => s
        }
      val dist = speed * 1000 / 3600 * period // dist in m
      val pos = next(prev.point, dist)
      prev #:: loop(
        prev.copy(
          point = pos,
          totalDist = prev.totalDist + dist,
          speed = speed
        )
      )
    }
    loop(car)
  }

  val system = ActorSystem("RaceSystem")

  // An actor which moves a Car on the course, based on the stream
  class CarActor(carLabel: String) extends Actor {

    private lazy val iterator = raceStream(Car(carLabel, track.head, 130, 0)).iterator
    private var state: Car = null

    def receive = {
      // The race is starting!
      case "start" =>
        state = iterator.next
        context.system.scheduler.schedule(period seconds,period seconds,self,"move") // Schedule each move of the car

      // The car moves to a new point
      case "move" =>
        state = iterator.next

      // Send the current car state to the sender
      case "getState" => sender ! state
    }
  }

  // An actor which represent the race, with a BroadcastRouter to fire "start" event on all cars.
  val carActors = cars.map(car => system.actorOf(Props(new CarActor(car))))

  val raceActor = system.actorOf(Props(new Actor {

    val router = context.actorOf(Props[CarActor].withRouter(akka.routing.BroadcastRouter(carActors)))

    def receive = {
      // Let's go!
      case "start" => router ! "start"
    }
  }))

  // Get a random int between from and to
  private def randomInt(from: Int, to: Int) = from + Random.nextInt(to - from)


}