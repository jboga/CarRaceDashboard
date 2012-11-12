package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import models.Streams._
import models.Streams
import models.Race._

object Application extends Controller {

  def startRace = Action {
    // Start the race
    models.Race.raceActor ! "start"

    // Connect the event stream to the storage actor
    Streams.events(Iteratee.foreach[Event]{event=>
      models.Storage.storeActor ! event
    })

    Ok("started")
  }


  def index = Action {
    Ok(views.html.index())
  }
  
}