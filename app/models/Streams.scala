package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.util.Random
import scala.collection.mutable.{ArrayBuffer,SynchronizedBuffer}

object Streams{

    trait Event

    case class SpeedEvent(car:String,speed:Int) extends Event
    case class DistEvent(car:String,dist:Int) extends Event

	case class Car(label:String, dist:Int=0){
		def addDist(value:Int)=copy(dist=dist+value)
	}

	val cars = new ArrayBuffer[Car]() with SynchronizedBuffer[Car]
	cars.append(Car("voiture 1"))
	cars.append(Car("voiture 2"))
	cars.append(Car("voiture 3"))

	val speed:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
    	Promise.timeout(
    		Some(
    			SpeedEvent(
    				cars(Random.nextInt(cars.size)).label,
    				Random.nextInt(180)
    			)
    		)
    		, Random.nextInt(500))
  	}

  	val distance:Enumerator[Event] = Enumerator.fromCallback[Event] {()=>
    	Promise.timeout({
    		val index=Random.nextInt(cars.size)
    		val updated=cars(index).addDist(Random.nextInt(800))
    		cars.update(index,updated)
    		Some(
    			DistEvent(
    				updated.label,
    				updated.dist
    			)
    		)
    		}, Random.nextInt(500))
  	}

  	val events: Enumerator[Event] = speed >- distance

}