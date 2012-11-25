import play.api._
import akka.actor.ActorSystem

object Global extends GlobalSettings {

  override def onStart(app: Application) {
  }

  override def onStop(app: Application) {
    ActorSystem("RaceSystem").shutdown()
  }

}