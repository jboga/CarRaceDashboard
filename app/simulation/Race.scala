package simulation

import scala.util.Random
import akka.actor._
import akka.util.duration._
import math._
import play.api.libs.concurrent._
import play.api.Play.current
import play.api.libs.iteratee._

case class Position(latitude: Double, longitude: Double)
case class CheckPoint(id: Int, position: Position)
case class Car(label: String, point: CheckPoint, speed: Int, totalDist: Double)

object Race {

  val period = 1 // in second

  val system = ActorSystem("RaceSystem")

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

  lazy val raceActor=system.actorOf(Props[RaceActor])

}

class Race(val trackURL: String, val nbCars: Int) {
  import simulation.Race._

  type Track = List[CheckPoint]

  val track: Track = simulation.TrackParser.readTrack(trackURL)

  lazy val lapLength: Double = lapLength(track)

  def lapLength(track: Track) =
    (for {
      List(p1, p2) <- track.sliding(2)
    } yield computeDistance(p1.position, p2.position)).sum


  // Get next checkpoint, based on track
  private def nextTrackPoint(point: CheckPoint) =
    if (point.id + 1 >= track.size)
      track.head // new lap
    else
      track(point.id + 1)

  //return new CheckPoint on the track at distance d from point
  private def next(point: CheckPoint, d: Double): CheckPoint = {
    val nextPoint = nextTrackPoint(point)
    val distanceBetween = computeDistance(point.position, nextPoint.position)
    if (d < distanceBetween)
      CheckPoint(point.id, computePosition(point.position, nextPoint.position, d))
    else
      next(nextPoint, (d - distanceBetween))
  }

  private val drivers = List(
    "Michael Schumacher",
    "Juan Manuel Fangio",
    "Alain Prost",
    "Jack Brabham",
    "Niki Lauda",
    "Nelson Piquet",
    "Ayrton Senna",
    "Jackie Stewart",
    "Fernando Alonso",
    "Alberto Ascari",
    "Jim Clark",
    "Emerson Fittipaldi",
    "Mika HŠkkinen",
    "Graham Hill",
    "Sebastian Vettel",
    "Mario Andretti",
    "Jenson Button",
    "Nino Farina",
    "Lewis Hamilton",
    "Mike Hawthorn",
    "Damon Hill",
    "Phil Hill",
    "Denny Hulme",
    "James Hunt",
    "Alan Jones",
    "Nigel Mansell",
    "Kimi RŠikkšnen",
    "Jochen Rindt",
    "Keke Rosberg",
    "Jody Scheckter",
    "John Surtees",
    "Jacques Villeneuve"
  )

  // List of cars
  val cars = Random.shuffle(drivers).take(nbCars)

  private val minSpeed = 150
  private val maxSpeed = 260

  // "Race" stream for a car. Each value is a state of the car `car` at a time t of the race.
  def raceStream(car: Car): Stream[Car] = {
    def loop(prev: Car): Stream[Car] = {
      val speed =
      // Add/remove a random number to current speed, but with guard
        prev.speed + randomInt(-10, 10) match {
          case s if s < minSpeed => minSpeed
          case s if s > maxSpeed => maxSpeed
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

  val carActors = cars.map(car => system.actorOf(Props(new CarActor(car,this))))

  // Get a random int between from and to
  private def randomInt(from: Int, to: Int) = from + Random.nextInt(to - from)

}

// An actor which moves a Car on the track, based on the stream
class CarActor(carLabel: String, race:Race) extends Actor {

  private lazy val iterator = race.raceStream(Car(carLabel, race.track.head, 130, 0)).iterator
  private var state:Option[Car] = None
  private var stop:Option[Cancellable]=None

  def receive = {
    // The race is starting!
    case "start" =>
      state = Some(iterator.next)
      stop = Some(
        context.system.scheduler.schedule(Race.period seconds, Race.period seconds, self, "move") // Schedule each move of the car
      )

    // The car moves to a new point
    case "move" =>
      state = Some(iterator.next)

    case "stop" =>
      state = None
      stop.map(_.cancel)

    // Send the current car state to the sender
    case "getState" => 
      sender ! state
  }

}

case class StartRace(trackURL:String, nbCars:Int)

// An actor which represent the race, with a BroadcastRouter to fire "start" event on all cars.
class  RaceActor extends Actor{

  var currentRace:Option[Race]=None
  var router:Option[ActorRef]=None

  def receive = {
    case StartRace(url,nbCars) => 
      currentRace match {
        case None => 
          currentRace=Some(new Race(url,nbCars))
          router = Some(context.actorOf(Props[CarActor].withRouter(akka.routing.BroadcastRouter(currentRace.get.carActors))))

          // Fire start event
          router.get ! "start"

          // Start computing statistics
          models.StatsActor.actor ! "start"

          // Connect the event stream to the storage actor
          new Streams(currentRace.get).events(Iteratee.foreach[models.Events.Event] {
            event => models.StorageActor.actor ! event
          })

          sender ! currentRace
        case _ => 
          sender ! None
      }

    case "stop" =>
      router.map(_ ! "stop")
      models.StatsActor.actor ! "stop"
      currentRace = None
      sender ! true

    case "getRace" => 
      sender ! currentRace
  }

}