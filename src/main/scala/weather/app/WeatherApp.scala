package weather.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.ConfigFactory
import scala.util.{Success, Failure}
import weather.app._
import weather.app.auth.AuthDirectives._

object WeatherApp extends App {
  implicit val system: ActorSystem = ActorSystem("WeatherApp")

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load

  val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("Starting WeatherApp server...")

  val route =
    path("api" / "stations") {
      authenticateBearerToken {
        get {
          val url = config.getString("weather.url")
          onComplete(getDataFromUrl(url)) {
            case Success(content) =>
              val stationListString = listStationList(content)
              logger.info(stationListString)
              complete("Result printed to list: \n"+ stationListString)

            case Failure(ex) =>
              logger.error(s"Request failed with error: $ex")
              complete(StatusCodes.InternalServerError, "Failed to fetch data.")
          }
        }
      }
    } ~ path("api" / "station" / Segment) { stationId =>
      authenticateBearerToken {
        get {
          val url = config.getString("weather.url")
          onComplete(getDataFromUrl(url)) {
            case Success(content) =>
              val stationDataString = getStationData(content, stationId)
              logger.info(stationDataString)
              complete("Data of selected station: "+ stationDataString)

            case Failure(ex) =>
              logger.error(s"Request failed with error: $ex")
              complete(StatusCodes.InternalServerError, "Failed to fetch data.")
          }
        }
      }
    }

  Http().newServerAt("localhost", 8080).bind(route)

  logger.info("WeatherApp server running on port 8080")
}
