(function () {
  goog.provide('geocat_shared_objects_extent_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  /* Controllers */

  var module = angular.module('geocat_shared_objects_extent_controller', []).

    controller('ExtentControl', [
        '$scope',
        '$routeParams',
        '$location',
        '$http',
        'gnUrlUtils',
        'commonProperties',
      function ($scope, $routeParams, $location, $http,
                gnUrlUtils, commonProperties) {

        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_extent';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_extent';
        }

        var getService = 'xml.extent.get',
            updateService = 'xml.extent.update';

        // Initiate obj that will be filled by extent form directive
        $scope.formObj = {
          langs: ['DE', 'EN', 'FR', 'IT']
        };

        var updateExtent = function(o) {
          var params = {
            id: o.id,
            geoId: o.geoId,
            typename: o.typename,
            desc: o.desc,
            geom: o.geomString,
            crs: o.proj,
            format: 'WKT',
            _content_type: 'json'
          };
          angular.forEach($scope.formObj.langs, function(l) {
            params['geoId'+l] = $scope.formObj.geoId[l];
            params['desc'+l] = $scope.formObj.desc[l];
          });

          $http({
            method: 'GET',
            url: updateService,
            params: params
          }).success(function(data) {
            console.log('Save extent success');
          });
        };

        $scope.edit = function (row) {

          angular.extend($scope.formObj, {
            id: row.id,
            typename: gnUrlUtils.parseKeyValue(row.url).typename
          });

          $http({
            method: 'GET',
            url: getService,
            params: {
              id: $scope.formObj.id,
              typename: $scope.formObj.typename,
              format: 'wkt',
              crs:'EPSG:21781',
              _content_type: 'json'
            }
          })
              .success(function (data) {

                $scope.$broadcast('modalShown', data[0].featureType);
                $scope.finishEdit = function () {
                  updateExtent($scope.formObj);
                  $('#editModal').modal('hide');
                  $scope.reloadData();
                };
                for (var lang in $scope.keyword) {
                  $scope.keyword[lang].label = data[lang].label;
                  $scope.keyword[lang].desc = data[lang].definition;
                }
                $('#editModal').modal('show');

              });
        };
        $scope.startCreateNew = function () {
          $location.path("/validated/extents");
          $scope.reloadOnWindowClosed(open($scope.baseUrl + '/extent.edit?crs=EPSG:21781&typename=gn:xlinks&id=&wfs=default&modal', '_sharedObject'));
        };

      }]);

})();
