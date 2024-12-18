package weather.app

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.*
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.include

class StationListTest extends AnyFunSuite {

  test("listStationList should return a formatted list of stations") {
    val content =
      """STATION_ID,NAME,WMO_ID,BEGIN_DATE,END_DATE,LATITUDE,LONGITUDE,GAUSS1,GAUSS2,GEOGR1,GEOGR2,ELEVATION,ELEVATION_PRESSURE
        |homework3 "KALNCIEM","Kalnciems"," ","1945.01.01 00:00:00","3999.12.31 23:59:00","564756","0233700","476940.94","294474.38","23.61654","56.79878","3.18"," "
        |homework3 "SIGULDA","Sigulda"," ","1939.01.03 00:00:00","3999.12.31 23:59:00","570954","0245112","551605.75","336076.09","24.8533","57.165","100.15"," "
        |homework3 "RIVE99PA","Ventspils","26314","1873.01.01 00:00:00","3999.12.31 23:59:00","572344","0213214","352000.31","364103.86","21.5372","57.3956","2.33","3.9"
        |""".stripMargin

    val result = listStationList(content)

    val expected =
      """Station ID: homework3 KALNCIEM, Name: Kalnciems
        |Station ID: homework3 SIGULDA, Name: Sigulda
        |Station ID: homework3 RIVE99PA, Name: Ventspils""".stripMargin

    assert(result == expected)
  }

  test("listStationList should return 'No stations found.' when content has no valid stations") {
    val content =
      """STATION_ID,NAME,WMO_ID,BEGIN_DATE,END_DATE,LATITUDE,LONGITUDE,GAUSS1,GAUSS2,GEOGR1,GEOGR2,ELEVATION,ELEVATION_PRESSURE
        |"","","","","","","","","","","","",""
        |""".stripMargin

    val result = listStationList(content)

    assert(result == "No stations found.")
  }

  test("listStationList should handle an empty content string") {
    val content = ""

    val result = listStationList(content)

    assert(result == "No data provided.")
  }
}

class WeatherAppTest extends AnyFunSuite with Matchers {

  val validCsv =
    """STATION_ID,NAME,WMO_ID,BEGIN_DATE,END_DATE,LATITUDE,LONGITUDE,GAUSS1,GAUSS2,GEOGR1,GEOGR2,ELEVATION,ELEVATION_PRESSURE
      |RIVE99PA,"Ventspils","26314","1873.01.01 00:00:00","3999.12.31 23:59:00","572344","0213214","352000.31","364103.86","21.5372","57.3956","2.33","3.9"
      |RIDAGDA,"Dagda","26551","2019.04.10 00:00:00","3999.12.31 23:59:00","560626","0273336","","","27.56","56.107222","187.13","188.64"""".stripMargin

  test("Retrieve station data by ID") {
    val result = getStationData(validCsv, "RIVE99PA")
    result should include("Station ID: RIVE99PA")
    result should include("Name: Ventspils")
    result should include("WMO ID: 26314")
  }

  test("Station not found") {
    val result = getStationData(validCsv, "INVALID_ID")
    result shouldEqual "No station found with the given ID."
  }

  test("Missing data for some fields") {
    val result = getStationData(validCsv, "RIDAGDA")
    result should include("Station ID: RIDAGDA")
    result should include("Name: Dagda")
    result should include("Gauss1: N/A")
    result should include("Gauss2: N/A")
  }

  test("Leading/trailing whitespace in CSV data") {
    val whitespaceCsv =
      """STATION_ID,NAME,WMO_ID,BEGIN_DATE,END_DATE,LATITUDE,LONGITUDE,GAUSS1,GAUSS2,GEOGR1,GEOGR2,ELEVATION,ELEVATION_PRESSURE
        | RIVE99PA , "Ventspils" , "26314" , "1873.01.01 00:00:00" , "3999.12.31 23:59:00" , "572344" , "0213214" , "352000.31" , "364103.86" , "21.5372" , "57.3956" , "2.33" , "3.9" """.stripMargin
    val result = getStationData(whitespaceCsv, "RIVE99PA")
    result should include("Station ID: RIVE99PA")
    result should include("Name: Ventspils")
  }
}

class ConfigTest extends AnyFunSuite {
  val config = ConfigFactory.load()

  test("weather.url should not be empty") {
    val weatherUrl = config.getString("weather.url")
    assert(weatherUrl != null && weatherUrl.trim.nonEmpty, "weather.url should not be empty")
  }

  test("auth.token should not be empty") {
    val authToken = config.getString("auth.token")
    assert(authToken != null && authToken.trim.nonEmpty, "auth.token should not be empty")
  }
}

class AuthDirectivesTest extends AnyFunSuite with ScalatestRouteTest {
  import weather.app.auth.AuthDirectives._
  val config = ConfigFactory.load()

  // Create a route for testing the directive
  val testRoute: Route = authenticateBearerToken {
    complete("Authenticated")
  }

  test("Should authenticate with a valid Bearer token") {
    val validToken = config.getString("auth.token")// Replace with the token value in application.conf for a real test
    val header = RawHeader("Authorization", s"Bearer $validToken")

    // Test the route with a valid token
    Get("/") ~> addHeader(header) ~> testRoute ~> check {
      assert(response.status == StatusCodes.OK)
      assert(responseAs[String] == "Authenticated")
    }
  }

  test("Should fail authentication with an invalid Bearer token") {
    val invalidToken = "invalid-token"
    val header = RawHeader("Authorization", s"Bearer $invalidToken")

    // Test the route with an invalid token
    Get("/") ~> addHeader(header) ~> testRoute ~> check {
      assert(response.status == StatusCodes.Unauthorized)
      assert(responseAs[String] == "Unauthorized")
    }
  }

  test("Should fail authentication with no Authorization header") {
    // Test the route without an Authorization header
    Get("/") ~> testRoute ~> check {
      assert(response.status == StatusCodes.Unauthorized)
      assert(responseAs[String] == "Unauthorized")
    }
  }
}