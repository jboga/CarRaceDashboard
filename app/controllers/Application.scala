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

object Application extends Controller {

  def startRace = Action {
    // Start the race
    models.Race.raceActor ! "start"
    // Start the stats
    models.StatsActor.actor ! "start"

    // Connect the event stream to the storage actor
    Streams.events(Iteratee.foreach[Event] {
      event =>
        models.StorageActor.actor ! event
    })

    Ok("started")
  }


  def index = Action {
    Ok(views.html.index())
  }

  def rtEventSourceStream = Action {
    AsyncResult {
      implicit val timeout = Timeout(5.seconds)
      val actor = Akka.system.actorOf(Props[RTEventListener]) //,name = "comet-stream")
      // Actor is listening for event on the eventStream
      Akka.system.eventStream.subscribe(actor, classOf[Event])
      // For each event, stream the data to client
      (actor ? "start").mapTo[Enumerator[JsValue]].asPromise.map {
        chunks =>
          Ok.stream((chunks) &> EventSource()).as("text/event-stream")
      }
    }
  }
}

class RTEventListener extends Actor{
  lazy val channel: PushEnumerator[JsValue] =  Enumerator.imperative[JsValue](
    onComplete = {
      Akka.system.eventStream.unsubscribe(self)
      context.stop(self)
    }
  )
  def receive={
    case "start" =>
      Akka.system.eventStream.subscribe(self,classOf[Event])
      sender ! channel
    case change:Event =>
      channel.push(toJson(change)) // Push jsonified event
  }
}