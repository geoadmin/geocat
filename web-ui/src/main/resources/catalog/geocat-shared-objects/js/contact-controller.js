(function() {
  goog.provide('geocat_shared_objects_contact_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  var module = angular.module('geocat_shared_objects_contact_controller', []).
    controller('ContactControl',  ['$scope', '$routeParams', '$location', 'commonProperties',
      function ($scope, $routeParams, $location, commonProperties) {
        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        $scope.edit = function (row) {
          $scope.open(row.url);
        };
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_contact';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_contact';
        }
        $scope.startCreateNew = function () {
          $scope.reloadOnWindowClosed(open($scope.baseUrl + '/shared.user.edit?closeOnSavevalidated=y&operation=newuser', '_sharedObject'));

          $location.path("/validated/contacts");
        };
        $scope.includeRowPartial = 'row-formless.html';
      }]);
})();
