package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.util.Random
import scala.collection.mutable.{ArrayBuffer,SynchronizedBuffer}
import models.Race._

object Streams{

    trait Event

    case class SpeedEvent(car:String,speed:Int) extends Event
    case class DistEvent(car:String,dist:Double) extends Event
    case class PositionEvent(car:String,latitude:Double,longitude:Double) extends Event

    val position:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
        Promise.timeout({
            val car=race(randomCar)
            val carPosition=car.log.head._1.position
            Some(
                PositionEvent(
                    car.label,
                    carPosition.latitude,
                    carPosition.longitude
                )
            )
        }, Random.nextInt(1000))
    }

    val speed:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
        Promise.timeout({
            val car=race(randomCar)
            Some(
                SpeedEvent(
                    car.label,
                    randomInt(100,130)
                )
            )
        }, Random.nextInt(1000))
    }

    val distance:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
        Promise.timeout({
            val car=race(randomCar)
            Some(
                DistEvent(
                    car.label,
                    car.totalDist
                )
            )
        }, Random.nextInt(1000))
    }

    val events: Enumerator[Event] = position >- speed >- distance



    private def randomInt(min:Int,max:Int)=min+Random.nextInt(max-min)
    private def randomCar=cars(Random.nextInt(cars.size))

}