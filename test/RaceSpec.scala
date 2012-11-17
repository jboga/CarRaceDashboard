package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.Race._
import play.Logger
import models.Race
import util.Random

class RacedSpec extends Specification {

  "The Race 'Le Mans'" should {

    lazy val raceIterator: Iterator[Car] =
      running(FakeApplication()) {
        val start = System.currentTimeMillis
        val it = raceStream(Car(label = "TestCar", speed = 0, totalDist = 0, time = start)).iterator
        stepInTime(it, 20)
        it
      }

    def stepOne(it: Iterator[Car]) = {
      Thread.sleep(1000)
      it.next
    }

    def stepInTime(it: Iterator[Car], n: Int) = {
      for (i <- 1 to n) {
        val c = stepOne(it)
        System.out.println("Distance : " + c.totalDist)
        System.out.println("Speed : " + c.speed)
        System.out.println("time : " + c.time)
      }
    }

    "All Cars have same label" in {
      val nextCar = stepOne(raceIterator)
      nextCar.label must beEqualTo("TestCar")
    }

    "Calcution of a new point between two points" in {
      val pos1 = stepOne(raceIterator)
      val pos2 = stepOne(raceIterator)
      val dist = pos2.point.distFromPrevious * Random.nextInt(100)/100.0
      val newPos = computePosition(pos1.point.position, pos2.point.position, dist)
      computeDistance(pos1.point.position, newPos).round must beEqualTo(dist.round)
    }

    "Recorded distance between 2 points is coherent" in {
      val pos1 = stepOne(raceIterator)
      val pos2 = stepOne(raceIterator)
      pos2.totalDist must beEqualTo(pos1.totalDist + pos2.point.distFromPrevious)
    }

    "Calculated distance between 2 points is coherent" in {
      val pos1 = stepOne(raceIterator)
      val pos2 = stepOne(raceIterator)
      val distBetween = pos2.totalDist - pos1.totalDist
      val computedDist = computeDistance(pos1.point.position, pos2.point.position)
      distBetween.round must beEqualTo (computedDist.round)
    }

    "Time between 2 points is coherent" in {
      val pos1 = stepOne(raceIterator)
      val pos2 = stepOne(raceIterator)
      pos2.time - pos1.time - 1000 must not beGreaterThan (4)
    }

    "Track length is 13716m" in {
      running(FakeApplication()) {
        Race.lapLength.toInt must beEqualTo(13716)
      }
    }
  }
}
