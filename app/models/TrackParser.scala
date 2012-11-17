package models

import models.Race._
import scala.math._
import scala.xml._
import play.api.Play
import play.api.Play.current
import java.io.File

/*
  A parser of 'kml' files to read a Track (list of CheckPoints)
*/
object TrackParser{

  // Read a Track from a kml (google maps) file
  def readTrack(url:String):Track = readTrack(XML.load(url))

  def readTrack(data: Elem):Track = {
    val positions=(for {
      pos <- (data \\ "coordinates").text.split("\n")
      if (pos.trim.length>0)
    } yield pos.trim).toList.map{pos=>
      val coordinates=pos.split(",")
      Position(coordinates(1).toDouble,coordinates(0).toDouble)
    }
    positions.zipWithIndex.map(v=>CheckPoint(v._2,v._1))
  }

}