package controllers

import akka.util.duration._
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
import scala.Predef._
import models.Streams
import models.Streams.Event
import play.Logger
import models.Race

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  def startRace = Action {request=> Async {
    val formData=request.body.asFormUrlEncoded
    val nbCars = formData.get("nbcarsgroup").head.toInt
    val trackURL =
      (formData.get("track"), formData.get("trackURL")) match {
        case (url :: xs,_) if url.length > 0    => Some(url)
        case (_, url :: xs) if url.length > 0   => Some(url)
        case _ => None
      }
    trackURL match {
      case Some(url) =>
        // We have an url
        (Race.raceActor ? models.StartRace(url,nbCars)).mapTo[Option[Race]].asPromise.map{
          case Some(race) =>
            // Race has been created!

            // Redirect to live page
            Redirect("/")
          case None => 
            // Already started...
            BadRequest(views.html.chooseRace(Some("""A race is in progress! <a href="/">Click here</a> to view the race.""")))
        }


      case _ => Promise.pure(BadRequest(views.html.chooseRace(Some("Track is mandatory!"))))
    }
  }}

  def stopRace = Action { Async{
    (Race.raceActor ? "stop").mapTo[Boolean].asPromise.map(res=>Redirect("/"))
  }}

  def index = Action { Async {
    (Race.raceActor ? "getRace").mapTo[Option[Race]].asPromise.map{
      case Some(race) => 
        // We have a race!
        Ok(views.html.viewRace(race))
      case None => 
        // Not yet started : show the setup screen
        Ok(views.html.chooseRace())
    }
  }}

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