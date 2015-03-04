(function () {
  goog.provide('geocat_shared_objects_format_controller');
  goog.require('geocat_shared_objects_translate_config');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  /* Controllers */

  var module = angular.module('geocat_shared_objects_format_controller', ['geocat_shared_objects_translate_config']).
    controller('FormatControl', ['$scope', '$routeParams', '$http', '$location', 'commonProperties', 'subtemplateService',
      function ($scope, $routeParams, $http, $location, commonProperties, subtemplateService) {
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
          $scope.reloadOnWindowClosed($scope.open(row.url));
        };

        $scope.startCreateNew = function () {
          subtemplateService.createNewSubtemplate(subtemplateService.formatTemplate, true, function(){
            $scope.loading = '-1';
          }).
            success(commonProperties.createSubtemplateSuccess($scope, "/validated/formats")).
            error(commonProperties.createSubtemplateError($scope));
        };

      }]);
})();
