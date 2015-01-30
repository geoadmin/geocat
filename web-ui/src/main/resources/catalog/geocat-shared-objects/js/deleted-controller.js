(function() {
  goog.provide('geocat_shared_objects_deleted_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

/* Controllers */

var module = angular.module('geocat_shared_objects_deleted_controller', []).
 controller('DeletedControl', ['$scope', '$routeParams', 'commonProperties', function ($scope, $routeParams, commonProperties) {
      $scope.type = 'deleted';
      $scope.isDeletePage = true;
      $scope.validated = 'validated';
      $scope.isValidated = true;
      $scope.validatedTitle = 'rejected' //Geonet.translate('rejected');
      commonProperties.add($scope, $routeParams);
      if ($scope.isValidated) {
          $scope.luceneIndexField = 'V_invalid_xlink_keyword';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_keyword';
      }

      $scope.delete = function () {
        if ($scope.selected.referenceCount != undefined) {
          $scope.performUpdateOperation('reusable.delete');
        } else {
          $scope.updateReferenceCount().then(function(){$scope.performUpdateOperation('reusable.delete');});
        }

      }

  }]);

})();
