package controllers

import play.api._
import libs.json.JsNull
import play.api.mvc._
import play.api.libs.json.Json._
import models.Todo

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.main())
  }

  def getAllTodos=Action{
    Ok(
      toJson(
        Todo.findAll().map(Todo.toJson).toList
      )
    )
  }

  def createTodo=Action{request=>
    request.body.asJson.map{ json =>
      Ok(Todo.toJson(
        Todo.create(Todo.fromJson(json))
      ))
    }.getOrElse(BadRequest("Invalid JSON data"))
  }

  def updateTodo(id:String)=Action{request=>
    request.body.asJson.map{ json =>
      Todo.update(Todo.fromJson(json))
      Ok("saved")
    }.getOrElse(BadRequest("Invalid JSON data"))
  }

  def deleteTodo(id:String)=Action{request=>
    Todo.delete(id)
    Ok("deleted")
  }
  
}