(function () {
  goog.provide('geocat_shared_objects_format_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  /* Controllers */

  var module = angular.module('geocat_shared_objects_format_controller', []).
    controller('FormatControl', ['$scope', '$routeParams', '$http', '$location', 'commonProperties',
      function ($scope, $routeParams, $http, $location, commonProperties) {
        var template = '<gmd:MD_Format xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco">' +
        '  <gmd:name>' +
        '    <gco:CharacterString >-- Template Name --</gco:CharacterString>' +
        '  </gmd:name>' +
        '  <gmd:version>' +
        '    <gco:CharacterString>-- Template Version --</gco:CharacterString>' +
        '  </gmd:version>' +
        '</gmd:MD_Format>';
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
          $scope.createNewSubtemplate(template, "/validated/formats");
        };

      }]);
})();
