package models

import play.api.libs.iteratee._
import play.api.libs.concurrent._

object Race {

    case class Position(latitude:Double,longitude:Double)
    case class CheckPoint(id:Int,position:Position,distFromPrevious:Double)    

    type Race = List[CheckPoint]

    private lazy val race=RaceParser.readRace("/Users/antoine/Dev/repositories/CarRaceDashboard/Race.kml")

    def next(checkpointId:Int)=
      if (checkpointId+1>=race.size)
        None
      else Some(race(checkpointId+1))

}