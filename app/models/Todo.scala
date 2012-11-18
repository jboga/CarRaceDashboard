package models

import com.mongodb.casbah.Imports._
import play.api.libs.json.{Json, JsNull, JsValue}

/**
 * Created with IntelliJ IDEA.
 * User: antoine
 * Date: 16/09/12
 * Time: 22:23
 */
case class Todo(id:Option[String], title:String, completed:Boolean, order:Int)

object Todo{

  def toMongoObject(todo:Todo)=
    MongoDBObject(
        "title"->todo.title,
        "completed"->todo.completed,
        "order"->todo.order
    )

  def create(todo:Todo)={
    val mongoObject=toMongoObject(todo)
    DB.connection("todos").insert(mongoObject)
    todo.copy(id=mongoObject.getAs[ObjectId]("_id").map(_.toString))
  }

  def update(todo:Todo)=
    DB.connection("todos").update(
      MongoDBObject("_id"->new ObjectId(todo.id.get)),
      toMongoObject(todo)
    )

  def delete(id:String)=
    DB.connection("todos").remove(MongoDBObject("_id"->new ObjectId(id)))

  def findAll()=
    for (
      todos <- DB.connection("todos").find();
      id <- todos.getAs[ObjectId]("_id");
      title <- todos.getAs[String]("title");
      completed <- todos.getAs[Boolean]("completed");
      order <- todos.getAs[Int]("order")
    ) yield Todo(Some(id.toString),title,completed,order)

  def fromJson(json:JsValue)=
    Todo(
      (json \ "id").asOpt[String],
      (json \ "title").as[String],
      (json \ "completed").as[Boolean],
      (json \ "order").as[Int]
    )

  def toJson(todo:Todo)=
    Json.toJson(Map(
      "id"->todo.id.map(Json.toJson(_)).getOrElse(JsNull),
      "title"->Json.toJson(todo.title),
      "completed"->Json.toJson(todo.completed),
      "order"->Json.toJson(todo.order)
    ))

}