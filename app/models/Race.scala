package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.util.Random
import akka.actor._
import akka.util.duration._
import models.CourseParser._

/*
  Represent a race and the states of cars.
  Used to get some test datas.
*/
object Race {

    case class Position(latitude:Double,longitude:Double)
    case class CheckPoint(id:Int,position:Position,distFromPrevious:Double)    
    type Course = List[CheckPoint]

    // Represent a Car at a specific point of the course
    case class Car(label:String,point:CheckPoint,totalDist:Double){
      def moveToCheckpoint(newPoint:CheckPoint)=
        copy(
          point = newPoint,
          totalDist = totalDist + newPoint.distFromPrevious
        )

    }

    // Load course from kml (list of checkpoints)
    private lazy val course:Course = readCourse("LeMans.kml")

    // Get next checkpoint, based on course
    private def next(point:CheckPoint)=
      if (point.id+1>=course.size)
        course(0) // new lap
      else
        course(point.id+1)

    // List of concurrents
    private val cars = Vector(
      "voiture 1",
      "voiture 2",
      "voiture 3"
    )

    // Starting grid : all cars at first checkpoint
    private lazy val startingGrid = cars.map(Car(_,course(0),0))

    // Stream of the race. Each value is the list of all Car positions at time t
    val race:Stream[Vector[Car]]={
      def loop(prev:Vector[Car]):Stream[Vector[Car]]={
        val index=Random.nextInt(prev.size)
        val car=prev(index)
        prev #:: loop(
          prev.updated(
            index,
            car.moveToCheckpoint(next(car.point))
          )
        )
      }
      loop(startingGrid)
    }

    // Actor which iterate from the stream
    val raceActor =  ActorSystem("RaceSystem").actorOf(Props(new Actor {

      val iterator=race.iterator
      var currentState:Option[Vector[Car]]=None

      def receive = {
        // Get current state, and schedule next run
        case "nextRun" =>
          currentState=Some(iterator.next)
          context.system.scheduler.scheduleOnce(Random.nextInt(2000) millisecond,self,"nextRun")

        // Get data about a specific car at time t
        case ("getCar",car:Int) =>
          sender ! currentState.map(state=>Some(state(car))).getOrElse(None)
      }
    }))

    // Get a random car index in the vector
    def randomCar=Random.nextInt(cars.size)

}