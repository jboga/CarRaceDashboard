package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import models.Race._
import scala.util.Random
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout

/*  
    These streams is used to obtain test datas.
    We can assume that in a real application, the `events` Enumerator is provided by external streaming service (like HTTP streaming)
*/
object Streams{
    implicit val timeout = Timeout(5 seconds) 

    trait Event

    case class SpeedEvent(car:String,speed:Int) extends Event
    case class DistEvent(car:String,dist:Double) extends Event
    case class PositionEvent(car:String,latitude:Double,longitude:Double) extends Event

    lazy val events: Enumerator[Event] = position >- speed >- distance

    // Enumerator of position events of a random car
    private val position = Enumerator.fromCallback[Event] {()=>
        for {
            index <- Promise.timeout(randomCar,Random.nextInt(1000))
            car <- (raceActor ? ("getCar",index)).mapTo[Option[Car]].asPromise
        } yield car.map(car=>
            PositionEvent(
                car.label,
                car.point.position.latitude,
                car.point.position.longitude
            )
        )
    }

    // Enumerator of distance events of a random car
    private val distance = Enumerator.fromCallback[Event] {()=>
        for {
            index <- Promise.timeout(randomCar,Random.nextInt(1000))
            car <- (raceActor ? ("getCar",index)).mapTo[Option[Car]].asPromise
        } yield car.map(car=>
            DistEvent(
                car.label,
                car.totalDist
            )
        )
    }

    // For the moment, random values for speed
    private val speed = Enumerator.fromCallback[Event] {()=>
        for {
            index <- Promise.timeout(randomCar,Random.nextInt(1000))
            car <- (raceActor ? ("getCar",index)).mapTo[Option[Car]].asPromise
        } yield car.map(car=>
            SpeedEvent(
                car.label,
                randomInt(100,180)
            )
        )
    }
     
    // Get a random int between from and to
    private def randomInt(from:Int,to:Int)=from+Random.nextInt(to-from)

}