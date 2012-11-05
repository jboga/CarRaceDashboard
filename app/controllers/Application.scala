package controllers

import play.api._
import play.api.mvc._
import models.Streams
import models.Streams._
import play.api.libs.iteratee._

object Application extends Controller {
  
  def index = Action {

    val toString: Enumeratee[Event, String] = Enumeratee.map[Event] { 
      case SpeedEvent(car, newSpeed) => 
      	"Speed event for car %s, new speed is : %d km/h\n".format(car,newSpeed)
      case DistEvent(car, newDist) => 
      	"Dist event for car %s, new dist is : %d m\n".format(car,newDist)
    }

    Ok.stream(Streams.events &> toString)
  }
  
}