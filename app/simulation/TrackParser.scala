package simulation

import scala.xml._

/*
  A parser of 'kml' files to read a Track (list of CheckPoints)
*/
object TrackParser {

  // List of available Tracks
  val tracks = Map(
    "http://www.comintech.net/kml/LeMans.kml" -> "Le Mans",
    "http://www.comintech.net/kml/Monaco.kml" -> "Monaco",
    "http://www.comintech.net/kml/ValenciaF1UrbanCircuit.kml" -> "Valencia",
    "http://www.comintech.net/kml/CircuitdeFrancorchamps.kml" -> "Spa Francorchamps",
    "http://www.comintech.net/kml/Road_Atlanta.kml" -> "Atlanta"
  )

  // Read a Track from a kml (google maps) file
  def readTrack(url: String): List[TrackPoint] = readTrack(XML.load(url))

  def readTrack(data: Elem): List[TrackPoint] = {
    val positions = (for {
      pos <- (data \\ "coordinates").text.split("\n")
      if (pos.trim.length > 0)
    } yield pos.trim).toList.map {
      pos =>
        val coordinates = pos.split(",")
        Position(coordinates(1).toDouble, coordinates(0).toDouble)
    }
    positions.zipWithIndex.map(v => TrackPoint(v._2, v._1))
  }

}