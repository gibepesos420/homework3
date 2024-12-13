// weather-app.js
var app = angular.module('WeatherApp', []);

app.controller('WeatherController', function($scope, $http) {
    $scope.title = "Latvijas meteroloģisko staciju saraksts";
});
