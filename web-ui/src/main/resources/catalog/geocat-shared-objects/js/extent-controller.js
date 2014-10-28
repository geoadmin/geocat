(function () {
  goog.provide('geocat_shared_objects_extent_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  /* Controllers */

  var module = angular.module('geocat_shared_objects_extent_controller', []).
    controller('ExtentControl', ['$scope', '$routeParams', '$location', 'commonProperties',
      function ($scope, $routeParams, $location, commonProperties) {
        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_extent';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_extent';
        }
        $scope.edit = function (row) {
          $scope.open(row.url);
        };
        $scope.startCreateNew = function () {
          $location.path("/validated/extents");
          $scope.reloadOnWindowClosed(open($scope.baseUrl + '/extent.edit?crs=EPSG:21781&typename=gn:xlinks&id=&wfs=default&modal', '_sharedObject'));
        };

      }]);

})();
