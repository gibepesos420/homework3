// weather-app.js
var app = angular.module('WeatherApp', []);

app.controller('WeatherController', ['$scope', '$http', function($scope, $http) {
    $scope.title = "Latvijas meteroloģisko staciju saraksts";

    $scope.stations = [];
    $http.get('/api/stations').then(function(response) {
        $scope.stations = response.data.Stations
    }, function(error) {
        console.error('Failed to fetch station data', error);
    });
}]);
