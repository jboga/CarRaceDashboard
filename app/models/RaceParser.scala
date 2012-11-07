package models

import models.Race._
import scala.math._
import scala.xml._

object RaceParser{

  def readRace(filename:String):Race={
    val data = XML.loadFile(filename)
    val positions=(for {
      pos <- (data \\ "coordinates").text.split("\n")
      if (pos.trim.length>0)
    } yield pos.trim).toList.map{pos=>
      val coordinates=pos.split(",")
      Position(coordinates(1).toDouble,coordinates(0).toDouble)
    }
    positions.zipWithIndex.map{
      case (position,index) if index==0 => CheckPoint(index,position,0)
      case (position,index) => CheckPoint(index,position,distance(positions(index-1),position))
    }
  }

  private def distance(pos1:Position,pos2:Position):Double=
    acos(
      sin(toRadians(pos1.latitude))*sin(toRadians(pos2.latitude))+cos(toRadians(pos1.latitude))*cos(toRadians(pos2.latitude))*cos(toRadians(pos1.longitude)-toRadians(pos2.longitude))
    ) * 6366
}