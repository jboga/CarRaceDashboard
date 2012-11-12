import models.Streams.{PositionEvent, DistEvent, SpeedEvent, Event}
import play.api._
import akka.actor._
import akka.actor.{Actor, Props}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
  }

  override def onStop(app: Application) {
    ActorSystem("RaceSystem").shutdown()
  }

}