package weather.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*

import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.util.{Failure, Success}
import spray.json.*
import weather.app.model.StationDetailsJsonSupport.stationListFormat
import model.*
import org.slf4j.LoggerFactory

implicit val system: ActorSystem = ActorSystem("WeatherApp")

import system.dispatcher

val logger = LoggerFactory.getLogger(this.getClass)

def getDataFromUrl(url: String): Future[JsValue] = {
  Http().singleRequest(HttpRequest(uri = url)).flatMap { response =>
    response.status match {
      case StatusCodes.OK =>
        logger.info(s"Connected to: $url")
        response.entity.toStrict(5.seconds).map { entity =>
          val content = entity.data.utf8String
          logger.info(s"CSV Content: $content")

          val lines = content.split("\n").filter(_.nonEmpty)

          val header = lines.head.split(",")

          val rows = lines.tail.map { row =>
            val values = row.split(",").map(_.trim.replaceAll("\"", ""))

            StationDetails(
              Station_id = values.lift(0).getOrElse(""),
              Name = values.lift(1).getOrElse(""),
              WMO_id = values.lift(2).filter(_.nonEmpty),
              Begin_date = values.lift(3).getOrElse(""),
              End_date = values.lift(4).getOrElse(""),
              Latitude = values.lift(5).getOrElse(""),
              Longitude = values.lift(6).getOrElse(""),
              Gauss1 = values.lift(7).flatMap(str => tryParseDouble(str)),
              Gauss2 = values.lift(8).flatMap(str => tryParseDouble(str)),
              Geogr1 = values.lift(9).flatMap(str => tryParseDouble(str)),
              Geogr2 = values.lift(10).flatMap(str => tryParseDouble(str)),
              Elevation = values.lift(11).flatMap(str => tryParseDouble(str)),
              ELEVATION_PRESSURE = values.lift(12).flatMap(str => tryParseDouble(str))
            )
          }.toSeq

          val stationList = StationList(Stations = rows)

          val json = stationList.toJson

          json
        }
      case _ =>
        logger.error(s"Failed to connect to: $url")
        Future.failed(new Exception(s"Request failed with status code ${response.status}"))
    }
  }
}
def tryParseDouble(str: String): Option[Double] = {
  try {
    Some(str.toDouble)
  } catch {
    case _: NumberFormatException => None
  }
}
