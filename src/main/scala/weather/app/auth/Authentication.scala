package weather.app.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.*
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

implicit val system: ActorSystem = ActorSystem("WeatherAppAuth")

val logger = LoggerFactory.getLogger(this.getClass)

val config = ConfigFactory.load

object AuthDirectives {
  private val validToken = config.getString("auth.token")
  def authenticateBearerToken = {
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring("Bearer ".length)
        if (token == validToken) {
          pass
        } else {
          complete((401, "Unauthorized"))
        }
      case _ =>
          complete((401, "Unauthorized"))
    }
  }
}