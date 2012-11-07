package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.util.Random
import akka.actor._
import akka.util.duration._

object Race {

    case class Position(latitude:Double,longitude:Double)
    case class CheckPoint(id:Int,position:Position,distFromPrevious:Double)    

    type Race = List[CheckPoint]
    type RaceLog = List[(CheckPoint,Long)] // race log for a car : list of checkpoints with time passage

    private lazy val course=RaceParser.readRace("LeMans.kml")

    def next(point:CheckPoint)=
      if (point.id+1>=race.size)
        course(0) // new lap
      else
        course(point.id+1)

    
    case class Car(label:String,log:RaceLog,totalDist:Double){
      def addCheckpoint(point:CheckPoint,time:Long)=
        copy(
          log = (point,time) :: log,
          totalDist = totalDist+point.distFromPrevious
        )
    }

    val cars = List(
      "voiture 1",
      "voiture 2",
      "voiture 3"
    )

    val race = collection.mutable.Map[String,Car](
      cars.map{carLabel=>
        (
          carLabel,
          Car(carLabel,List[(CheckPoint,Long)]((course(0),0)),0)
        )
      }:_*
    )

    def move(car:String)={
      val old=race(car)
      race.update(
        car,
        old.addCheckpoint(
          next(old.log.head._1),
          System.currentTimeMillis
        )
      )
    }

    val raceActor =  ActorSystem("RaceSystem").actorOf(Props(new Actor {
      def receive = {
        case _ =>
          move(cars(Random.nextInt(cars.size)))
          context.system.scheduler.scheduleOnce(Random.nextInt(5) seconds,self,"move")
      }
    }))

    raceActor ! "move"

}