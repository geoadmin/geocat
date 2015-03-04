(function() {
  goog.provide('geocat_shared_objects_contact_controller');
  goog.require('shared-object-subtemplate-service');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  var module = angular.module('geocat_shared_objects_contact_controller', ['shared-object-subtemplate-service']).
    controller('ContactControl',  ['$scope', '$routeParams', '$location', 'commonProperties', 'subtemplateService',
      function ($scope, $routeParams, $location, commonProperties, subtemplateService) {
        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        $scope.edit = function (row) {
          $scope.reloadOnWindowClosed($scope.open(row.url));
        };
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_contact';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_contact';
        }
        $scope.startCreateNew = function () {
          subtemplateService.createNewSubtemplate(subtemplateService.contactTemplate, true, function(){
            $scope.loading = '-1';
          }).
            success(commonProperties.createSubtemplateSuccess($scope, "/validated/contacts")).
            error(commonProperties.createSubtemplateError($scope));
        };
        $scope.includeRowPartial = 'row-formless.html';
      }]);
})();
