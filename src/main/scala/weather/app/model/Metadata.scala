package weather.app.model

import spray.json._

case class StationList(Stations: Seq[StationDetails])
case class StationDetails(
  Station_id: String,
  Name: String,
  WMO_id: Option[String],
  Begin_date: String,
  End_date: String,
  Latitude: String,
  Longitude: String,
  Gauss1: Option[Double],
  Gauss2: Option[Double],
  Geogr1: Option[Double],
  Geogr2: Option[Double],
  Elevation: Option[Double],
  ELEVATION_PRESSURE: Option[Double]
)

object StationDetailsJsonSupport extends DefaultJsonProtocol {
  implicit val stationDetailsFormat: RootJsonFormat[StationDetails] = jsonFormat13(StationDetails)
  implicit val stationListFormat: RootJsonFormat[StationList] = jsonFormat1(StationList)
}