package controllers

import akka.util.duration._
import play.api.libs.{EventSource, Comet}
import play.api.mvc._
import play.api.Play.current
import akka.actor.{Actor, Props}
import play.api.libs.iteratee.{PushEnumerator, Enumerator}
import akka.pattern.ask
import play.api.libs.concurrent._
import akka.util.Timeout
import play.api.libs.json.Json._
import play.api.libs.iteratee._
import play.api.libs.json.JsValue
import scala.Predef._
import models.Streams
import models.Streams.Event
import play.Logger
import models.Race

object Application extends Controller {

  var currentRace:Option[Race]=None

  def startRace = Action {request=>
    val formData=request.body.asFormUrlEncoded
    val trackURL = 
      (formData.get("track"), formData.get("trackURL")) match {
        case (url :: xs,_) if url.length > 0    => Some(url)
        case (_, url :: xs) if url.length > 0   => Some(url)
        case _ => None
      }
    trackURL.map{url=>
      currentRace match {
        case None =>
          val race=new Race(url)

          // Start the race
          race.actor ! "start"
          // Compute statistics
          models.StatsActor.actor ! "start"

          // Connect the event stream to the storage actor
          new Streams(race).events(Iteratee.foreach[Event] {
            event =>
              models.StorageActor.actor ! event
          })

          currentRace=Some(race)

          Redirect("/")

        case _ =>
          // a race is already started
          BadRequest(views.html.chooseRace(Some("""A race is in progress! <a href="/">Click here</a> to view the race.""")))
      }
    }.getOrElse(BadRequest(views.html.chooseRace(Some("Track is mandatory!"))))
  }


  def index = Action {
    currentRace match {
      case Some(race) => Ok(views.html.viewRace(race))
      case None => Ok(views.html.chooseRace())
    }
  }

  def rtEventSourceStream = Action {
    AsyncResult {
      implicit val timeout = Timeout(5.seconds)
      val actor = Akka.system.actorOf(Props[RTEventListener])
      // Actor is listening for event on the eventStream
      // For each event, stream the data to client
      (actor ? "start").mapTo[Enumerator[JsValue]].asPromise.map {
        chunks =>
          Ok.stream((chunks) &> EventSource()).as("text/event-stream")
      }
    }
  }
}

class RTEventListener extends Actor {
  lazy val channel: PushEnumerator[JsValue] = Enumerator.imperative[JsValue](
    onComplete = {
      Akka.system.eventStream.unsubscribe(self)
      context.stop(self)
    }
  )

  def receive = {
    case "start" =>
      Akka.system.eventStream.subscribe(self, classOf[Event])
      sender ! channel
    case change: Event =>
      channel.push(toJson(change)) // Push jsonified event
  }
}