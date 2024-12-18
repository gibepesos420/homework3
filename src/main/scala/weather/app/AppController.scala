package weather.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*

import scala.concurrent.Future
import scala.concurrent.duration.*
import model.*
import org.slf4j.LoggerFactory

implicit val system: ActorSystem = ActorSystem("WeatherApp")

import system.dispatcher

val logger = LoggerFactory.getLogger(this.getClass)

def getDataFromUrl(url: String): Future[String] = {
  Http().singleRequest(HttpRequest(uri = url)).flatMap { response =>
    response.status match {
      case StatusCodes.OK =>
        logger.info(s"Connected to: $url")
        response.entity.toStrict(5.seconds).map { entity =>
          val content = entity.data.utf8String
          logger.info(s"CSV Content: $content")

          content
          
        }
      case _ =>
        logger.error(s"Failed to connect to: $url")
        Future.failed(new Exception(s"Request failed with status code ${response.status}"))
    }
  }
}

def listStationList(content: String): String = {
  val lines = content.split("\n").filter(_.nonEmpty)

  val rows = lines.tail.flatMap { row =>
    val values = row.split(",").map(_.trim.replaceAll("\"", ""))

    if (values.lift(0).exists(_.nonEmpty) && values.lift(1).exists(_.nonEmpty)) {
      Some(StationBasicData(
        Station_id = values.lift(0).getOrElse(""),
        Name = values.lift(1).getOrElse("")
      ))
    } else None
  }.toList

  if (rows.isEmpty) {
    "No stations found."
  } else {
    rows.map(station => s"Station ID: ${station.Station_id}, Name: ${station.Name}").mkString("\n")
  }
}

def getStationData(content: String, stationId: String): String = {
  val lines = content.split("\n").filter(_.nonEmpty)

  val rows = lines.tail.flatMap { row =>
    val values = row.split(",").map(_.trim.replaceAll("\"", ""))

    if (values.lift(0).contains(stationId)) {
      Some(
        StationDetails(
          Station_id = values.lift(0).getOrElse(""),
          Name = values.lift(1).getOrElse(""),
          WMO_id = values.lift(2).filter(_.nonEmpty),
          Begin_date = values.lift(3).getOrElse(""),
          End_date = values.lift(4).getOrElse(""),
          Latitude = values.lift(5).getOrElse(""),
          Longitude = values.lift(6).getOrElse(""),
          Gauss1 = values.lift(7).flatMap(tryParseDouble),
          Gauss2 = values.lift(8).flatMap(tryParseDouble),
          Geogr1 = values.lift(9).flatMap(tryParseDouble),
          Geogr2 = values.lift(10).flatMap(tryParseDouble),
          Elevation = values.lift(11).flatMap(tryParseDouble),
          ELEVATION_PRESSURE = values.lift(12).flatMap(tryParseDouble)
        )
      )
    } else {
      None
    }
  }

  rows.headOption match {
    case Some(station) =>
      s"""
         |Station ID: ${station.Station_id}
         |Name: ${station.Name}
         |WMO ID: ${station.WMO_id.getOrElse("N/A")}
         |Begin Date: ${station.Begin_date}
         |End Date: ${station.End_date}
         |Latitude: ${station.Latitude}
         |Longitude: ${station.Longitude}
         |Gauss1: ${station.Gauss1.getOrElse("N/A")}
         |Gauss2: ${station.Gauss2.getOrElse("N/A")}
         |Geogr1: ${station.Geogr1.getOrElse("N/A")}
         |Geogr2: ${station.Geogr2.getOrElse("N/A")}
         |Elevation: ${station.Elevation.getOrElse("N/A")}
         |Elevation Pressure: ${station.ELEVATION_PRESSURE.getOrElse("N/A")}
      """.stripMargin
    case None =>
      "No station found with the given ID."
  }
}

def tryParseDouble(str: String): Option[Double] = {
  try {
    Some(str.toDouble)
  } catch {
    case _: NumberFormatException => None
  }
}
