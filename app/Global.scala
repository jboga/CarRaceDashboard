import models.Todo
import play.api.{Application, GlobalSettings}
import play.api.Play.current
import play.api.Play._

/**
 * Created with IntelliJ IDEA.
 * User: antoine
 * Date: 17/09/12
 * Time: 18:52
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    // Add some data if not yet initialized
    if(!isTest && !Todo.findAll().hasNext){
      Todo.create(Todo(None,"Try scala",true,1))
      Todo.create(Todo(None,"Try Play!",true,2))
      Todo.create(Todo(None,"Try MongoDB",true,3))
      Todo.create(Todo(None,"Have fun!",false,4))
    }
  }
}
