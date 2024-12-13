package WeatherApp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContextExecutor
import java.nio.file.{Files, Paths}

object WeatherApp extends App {
  implicit val system: ActorSystem = ActorSystem("WeatherApp")

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("Starting WeatherApp server...")

  val htmlPath = Paths.get(getClass.getResource("/app/weather-app.html").toURI)

  val route =
    path("") {
      get {
        logger.debug("Serving the weather-app.html page")
        if (Files.exists(htmlPath)) {
          val htmlContent = Files.readAllBytes(htmlPath)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, htmlContent))
        } else {
          logger.error("HTML file not found!")
          complete(StatusCodes.NotFound, "HTML file not found!")
        }
      }
    } ~ pathPrefixTest("javascript" | "css") {
      extractUnmatchedPath { p =>
        getFromResource("app" + p)
      }
    }

  Http().newServerAt("localhost", 8080).bind(route)

  logger.info("WeatherApp server running on port 8080")
}
