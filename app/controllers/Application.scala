package controllers

import scala.concurrent.duration._
import play.api.libs.{EventSource, Comet}
import play.api.mvc._
import play.api.Play.current
import akka.actor.{Actor, Props}
import akka.pattern.ask
import play.api.libs.concurrent._
import akka.util.Timeout
import play.api.libs.json.Json._
import play.api.libs.iteratee._
import play.api.libs.json.JsValue
import models.Events._
import play.Logger
import simulation.Race
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  // Index of the application. We check if a race is already started (by calling the RaceActor)
  // - if a race exists, we display the dashboard screen
  // - if not, we display the screen to create a new race
  def index = Action {
    Async {
      (Race.raceActor ? "getRace").mapTo[Option[Race]].map {
        case Some(race) =>
          // We have a race!
          Ok(views.html.viewRace(race.trackURL, race.lapLength))
        case None =>
          // Not yet started : show the setup screen
          Ok(views.html.chooseRace())
      }
    }
  }

  def html5Track = Action {
    Ok(views.html.viewHtml5Race())
  }

  def x(pixWidth: Double, lonWidth: Double, minLon: Double)(lon: Double) = pixWidth * 0.1 + (lon - minLon) * pixWidth / lonWidth

  def y(pixHeight: Double, latHeight: Double, minLat: Double)(lon: Double) = pixHeight * 0.1 + (lon - minLat) * pixHeight / latHeight

  def getTrack = Action {
    Async {
      (Race.raceActor ? "getRace").mapTo[Option[Race]].map {
        case Some(race) =>
//          val height = 300
//          val width = 400
//          val minLon = race.track.minBy(cp => cp.position.longitude).position.longitude
//          val minLat = race.track.minBy(cp => cp.position.latitude).position.latitude
//          val maxLon = race.track.maxBy(cp => cp.position.longitude).position.longitude
//          val maxLat = race.track.maxBy(cp => cp.position.latitude).position.latitude
//          val lonWidth = maxLon - minLon
//          val latHeight = maxLat - minLat
//          val calcX = x(width * 0.8, lonWidth, minLon)(_)
//          val calcY = y(height * 0.8, latHeight, minLat)(_)
          val track = race.track.map(cp => Map("x" -> cp.position.longitude, "y" -> cp.position.latitude)).toArray
          Ok(toJson(track))
        case None =>
          // Not yet started : not track yet
          NotFound("Race Not started")
      }
    }

  }

  // Start controller, it will start a new race
  // It retrieves input parameters, validates them and starts a new race by calling the RaceActor
  // If OK, it redirects to the Index controller
  def startRace = Action {
    request => Async {
      val formData = request.body.asFormUrlEncoded
      val nbCars = formData.get("nbcarsgroup").head.toInt
      formData.get("track") match {
        case url :: xs if url.length > 0 =>
          // We have an url
          (Race.raceActor ? simulation.StartRace(url, nbCars)).mapTo[Option[Race]].map {
            case Some(race) =>
              // Race has been created!

              // Redirect to live page
              Redirect("/")
            case None =>
              // Already started...
              BadRequest(views.html.chooseRace(Some( """A race is in progress! <a href="/">Click here</a> to view the race.""")))
          }


        case _ => Promise.pure(BadRequest(views.html.chooseRace(Some("Track is mandatory!"))))
      }
    }
  }

  // Stop a race, by sending the "stop" message to the RaceActor
  def stopRace = Action {
    Async {
      (Race.raceActor ? "stop").mapTo[Boolean].map(res => Redirect("/"))
    }
  }

  // Live publishes events with SSE stream
  def rtEventSourceStream = Action {
    AsyncResult {
      implicit val timeout = Timeout(5.seconds)
      val actor = Akka.system.actorOf(Props[RTEventListener])
      // Actor is listening for event on the eventStream
      // For each event, stream the data to client
      (actor ? "start").mapTo[Enumerator[JsValue]].map {
        chunks =>
          Ok.stream((chunks) &> EventSource()).as("text/event-stream")
      }
    }
  }
}

// An actor, instanciated for each web client with the SSE stream, which is subscribed to the Akka eventStream 
// and pushes a JSON event for each event in the eventStream
class RTEventListener extends Actor {
  val (output, channel) = Concurrent.broadcast[JsValue]
  // TODO : stop actor on channel disconnection

  def receive = {
    case "start" =>
      Akka.system.eventStream.subscribe(self, classOf[Event])
      sender ! output
    case change: Event =>
      channel.push(toJson(change)) // Push jsonified event
    case _ => println("ici")
  }
}