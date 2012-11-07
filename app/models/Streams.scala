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

    case class Car(label:String, checkpoint:Int=0, dist:Double=0){
      def atCheckpoint(newPoint:CheckPoint)=copy(checkpoint=newPoint.id,dist=dist+newPoint.distFromPrevious)
    }

    val cars = new Array[Car](3)
    cars(0)=Car("voiture 1")
    cars(1)=Car("voiture 2")
    cars(2)=Car("voiture 3")

    val race:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
        Promise.timeout({
            val index=Random.nextInt(cars.size)
            val car=cars(index)
            Race.next(car.checkpoint) match {
                case Some(checkpoint) =>
                    cars.update(index,car.atCheckpoint(checkpoint))
                    Some(
                        PositionEvent(car.label,checkpoint.position.latitude,checkpoint.position.longitude)
                    )
                case None =>  
                    None // finish
            }
        }, Random.nextInt(1000))
    }

    val speed:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
        Promise.timeout(
            Some(
                SpeedEvent(
                    cars(Random.nextInt(cars.size)).label,
                    randomInt(100,130)
                )
            )
            , Random.nextInt(1000))
    }

    val distance:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
        Promise.timeout({
            val car=cars(Random.nextInt(cars.size))
            Some(
                DistEvent(
                    car.label,
                    car.dist
                )
            )
        }, Random.nextInt(1000))
    }

    val events: Enumerator[Event] = race >- speed >- distance

    private def randomInt(min:Int,max:Int)=min+Random.nextInt(max-min)

}