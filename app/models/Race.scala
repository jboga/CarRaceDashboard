package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.util.Random
import akka.actor._
import akka.util.duration._
import models.CourseParser._
import math._

/*
  Represent a race, and states of cars.
  Used to get some test datas.
*/
object Race {

  case class Position(latitude: Double, longitude: Double)

  case class CheckPoint(id: Int, position: Position, distFromPrevious: Double)

  type Course = List[CheckPoint]

  // Represent a Car at a specific point of the course
  case class Car(label: String, point: CheckPoint, speed: Double, totalDist: Double, time: Long) {
    def moveToCheckpoint(newPoint: CheckPoint) = {
      val t = System.currentTimeMillis - time
      val v = 3600 * newPoint.distFromPrevious / t
      copy(
        point = newPoint,
        totalDist = totalDist + newPoint.distFromPrevious,
        time = time + t,
        speed = v
      )
    }
  }

  // Load course from kml (list of checkpoints)
  private lazy val course: Course = readCourse("public/tracks/LeMans.kml")

  // Get next checkpoint, based on course
  private def next(point: CheckPoint) =
    if (point.id + 1 >= course.size)
      course.head // new lap
    else
      course(point.id + 1)

  private def next(point: CheckPoint, distance: Int): CheckPoint = {
    val nextPoint = next(point)
    val distanceBetween = computeDistance(point.position, nextPoint.position).toInt
    if (distance < distanceBetween)
      CheckPoint(point.id, computePosition(point.position, nextPoint.position, distance), distance)
    else
      next(nextPoint, (distance - distanceBetween))
  }

  // Compute the distance between two position
  private def computeDistance(pos1: Position, pos2: Position): Double =
    acos(
      sin(toRadians(pos1.latitude)) * sin(toRadians(pos2.latitude))
        + cos(toRadians(pos1.latitude)) * cos(toRadians(pos2.latitude)) * cos(toRadians(pos1.longitude)
        - toRadians(pos2.longitude))
    ) * 6366000

  private def computePosition(point: Position, nextPoint: Position, distance: Int): Position = {
    val precision = 5
    if (distance < precision)
      point
    else {
      val distanceBetween = computeDistance(point, nextPoint).toInt
      val middlePoint = Position((point.latitude + nextPoint.latitude) / 2, (point.longitude + nextPoint.longitude) / 2)
      val d2: Int = distance / 2
      if (d2 < distanceBetween)
        computePosition(middlePoint, nextPoint, distanceBetween - d2)
      else
        computePosition(point, nextPoint, d2 - distanceBetween)
    }
  }

  // List of concurrents
  private val cars = List(
    "Car 1",
    "Car 2",
    "Car 3"
  )

  // "Race" stream for a car. Each value is a state of the car `car` at a time t of the race.
  def stream(car: Car): Stream[Car] = {
    def loop(prev: Car): Stream[Car] =
      prev #:: loop(
        prev.moveToCheckpoint(next(prev.point, randomInt(30, 50)))
      )
    loop(car)
  }

  // Random schedule next move to checkpoint, according to min and max values for speed
  def scheduleNextMove(nextPoint: CheckPoint, vMin: Int, vMax: Int) =
    (nextPoint.distFromPrevious / randomInt(vMin, vMax)) * 3600

  val system = ActorSystem("RaceSystem")

  // An actor which moves a Car on the course, based on the stream
  class CarActor(carLabel: String) extends Actor {

    private lazy val iterator = stream(Car(carLabel, course.head, 0, 0, System.currentTimeMillis)).iterator
    private var state: Car = null

    def receive = {
      case "start" =>
        state = iterator.next
        context.system.scheduler.scheduleOnce(1 seconds, self, "move")

      case "move" =>
        state = iterator.next
        context.system.scheduler.scheduleOnce(1 seconds, self, "move")

      case "getState" => sender ! state
    }
  }

  // An actor which represent the race, with a BroadcastRouter to fire "start" event on all cars.
  val carActors = cars.map(car => system.actorOf(Props(new CarActor(car))))

  val raceActor = system.actorOf(Props(new Actor {

    val router = context.actorOf(Props[CarActor].withRouter(akka.routing.BroadcastRouter(carActors)))

    def receive = {
      case "start" => router ! "start"
    }
  }))

  // Get a random int between from and to
  def randomInt(from: Int, to: Int) = from + Random.nextInt(to - from)


}