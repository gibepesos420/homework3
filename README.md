# To build the Docker image
docker build -t weather-app .

# To run the Docker container
docker run -d -p 8080:8080 weather-app

# To access api endpoints:
/api/stations:
curl -H "Authorization: Bearer Q4XpYzLb7vTwTlqOdp7_dHg99X8L5LYFVbm6Ig0KrVg" 127.0.0.1:8080/api/stations

/api/station/{stationId}:
For example, we will replace stationId with "VICAKI"
curl -H "Authorization: Bearer Q4XpYzLb7vTwTlqOdp7_dHg99X8L5LYFVbm6Ig0KrVg" 127.0.0.1:8080/api/station/VICAKI
