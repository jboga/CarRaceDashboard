package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.Race._
import play.Logger
import models.Race

class RacedSpec extends Specification {

  "The Race 'Le Mans'" should {

    "All Cars have same label" in {
      running(FakeApplication()) {
        val start = System.currentTimeMillis
        val it = raceStream(Car(label = "TestCar", speed = 0, totalDist = 0, time = start)).iterator
        val nextCar = it.next
        nextCar.label must beEqualTo("TestCar")
      }
    }

    "Distance between 2 points is coherent" in {
      running(FakeApplication()) {
        val start = System.currentTimeMillis
        val it = raceStream(Car(label = "TestCar", speed = 0, totalDist = 0, time = start)).iterator
        (1 to 20).map(i => it.next)
        val pos1 = it.next
        val pos2 = it.next
        pos2.totalDist must beEqualTo(pos1.totalDist + pos2.point.distFromPrevious)
      }
    }

    "Time between 2 points is coherent" in {
      running(FakeApplication()) {
        val start = System.currentTimeMillis
        val it = raceStream(Car(label = "TestCar", speed = 0, totalDist = 0, time = start)).iterator
        for (i <- 1 to 20) {
          val c = it.next
          Thread.sleep(1000)
          System.out.println("Distance pos1 : " + c.totalDist)
          System.out.println("Speed pos1 : " + c.speed)
          System.out.println("time pos1 : " + c.time)
        }
        val pos1 = it.next
        Thread.sleep(1000)
        val pos2 = it.next
        pos2.time - pos1.time - 1000 must not beGreaterThan (4)
      }
    }

    "Track length is 13716m" in {
      running(FakeApplication()) {
        Race.lapLength.toInt must beEqualTo(13716)
      }
    }
  }
}
