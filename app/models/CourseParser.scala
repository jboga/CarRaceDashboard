package models

import models.Race._
import scala.math._
import scala.xml._
import play.api.Play
import play.api.Play.current
import java.io.File

/*
  A parser of 'kml' files to read a Course (list of CheckPoints)
*/
object CourseParser{

  // Read a course from a kml (google maps) file
  def readCourse(filename:String):Course = readCourse(XML.loadFile(Play.getFile(filename)))

  def readCourse(data: Elem):Course = {
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

  // Compute the distance between two position
  private def distance(pos1:Position,pos2:Position):Double=
    acos(
      sin(toRadians(pos1.latitude)) * sin(toRadians(pos2.latitude))
      + cos(toRadians(pos1.latitude)) * cos(toRadians(pos2.latitude)) * cos(toRadians(pos1.longitude)
      - toRadians(pos2.longitude))
    ) * 6366000
}