package models

import play.api.Play._
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin

object DB {

  // Get a connection to the MongoDB
  lazy val db = ReactiveMongoPlugin.db

}
