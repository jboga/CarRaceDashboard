package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import models.Streams._
import models.Streams
import models.Race._

object Application extends Controller {

  def startRace = Action {
    models.Race.raceActor ! "nextRun"
    Ok("started")
  }


  def index = Action {

    val toString: Enumeratee[Event, String] = Enumeratee.map[Event] {
      case SpeedEvent(car, newSpeed) => 
        "Speed event for car %s, new speed is : %d km/h\n".format(car,newSpeed)
      case DistEvent(car, newDist) => 
        "Dist event for car %s, new dist is : %f m \n".format(car,newDist)
      case PositionEvent(car, latitude, longitude) =>
        "Position event for car %s, new position is : %f %f\n".format(car,latitude,longitude)
    }

    Ok.stream(Streams.events &> toString)
  }
  
}