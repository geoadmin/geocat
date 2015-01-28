(function () {
  goog.provide('geocat_shared_objects_format_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  /* Controllers */

  var module = angular.module('geocat_shared_objects_format_controller', []).
    controller('FormatControl', ['$scope', '$routeParams', '$http', '$location', 'commonProperties',
      function ($scope, $routeParams, $http, $location, commonProperties) {
        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        $scope.format = {
          name: '',
          version: ''
        };
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_format';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_format';
        }

        $scope.edit = function (row) {
          $scope.open(row.url);
        };
        $scope.createNewObject = function () {
          $scope.doUpdate(undefined, 'y');
        };

        $scope.doUpdate = function (id, validated) {
          $scope.performOperation({
            method: 'GET',
            url: $scope.baseUrl + '/format',
            params: {
              action: 'PUT',
              name: $scope.format.name,
              version: $scope.format.version,
              validated: 'y',
              id: id
            }
          }).
            success(function () {
              $location.path("/validated/formats");
              $scope.format.name = '';
              $scope.format.version = '';
            });
          $scope.includeRowPartial = 'row-format.html';
        };

      }]);
})();
