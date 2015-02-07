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
            addService = 'xml.extent.add',
            updateService = 'xml.extent.update';

        // Initiate obj that will be filled by extent form directive
        $scope.formObj = {
          langs: ['DE', 'EN', 'FR', 'IT']
        };

        var getString = function(val) {
          if (val && typeof(val) === 'string') {
            return val;
          }
          return null;
        };
        var addTranslation = function (params, paramPrefix, obj, field) {
          var val = getString(obj[field]);
          if (val) {
            params.push(paramPrefix+field + "=" + val);
          }
        };
        var encodeTranslation = function(params, paramPrefix, obj) {
          addTranslation(params, paramPrefix, obj, "DE");
          addTranslation(params, paramPrefix, obj, "FR");
          addTranslation(params, paramPrefix, obj, "EN");
          addTranslation(params, paramPrefix, obj, "IT");
          addTranslation(params, paramPrefix, obj, "RM");
        };
        var updateExtent = function(service, o) {
          var params = [
            'typename='+ o.typename,
            'geom='+ o.geomString,
            'crs='+ o.proj,
            'format='+ 'WKT',
            '_content_type='+ 'json'
          ];
          if (o.id !== undefined) {
            params.push('id=' + o.id);
          }
          encodeTranslation(params, "geoId", o.geoId);
          encodeTranslation(params, "desc", o.desc);

          return $http({
            method: 'POST',
            url: service,
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            data: params.join("&")
          });
        };
        var getBestTranslation = function (obj) {
          var curLang = $scope.lang.substring(0, 2).toUpperCase();
          var val = getString(obj[curLang]);
          angular.forEach(['DE', 'FR', 'IT', 'EN', 'RM'], function(lang) {
            if (!val && curLang !== lang) {
              val = getString(obj[lang]);
            }
          });
          return val;
        };
        var bestDescription = function() {
          var geoId = getBestTranslation($scope.formObj.geoId);
          var desc = getBestTranslation($scope.formObj.desc);
          var finalDesc;
          if (desc) {
            finalDesc = desc;
          }
          if (geoId) {
            if (finalDesc) {
              finalDesc += " <"+geoId+">"
            } else {
              finalDesc = "<"+geoId+">";
            }
          }
          if (!finalDesc) {
            finalDesc = "No Description";
          }
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
          }).success(function (data) {
              $scope.$broadcast('modalShown', data[0].featureType);
              $scope.finishEdit = function () {
                updateExtent(updateService, $scope.formObj);
                $('#editModal').modal('hide');
                row.desc = bestDescription();
              };

                $('#editModal').modal('show');

              });
        };
        $scope.startCreateNew = function () {
          $scope.formObj = {
            typename: 'gn:xlinks',
            geomString: '',
            proj: 'EPSG:21781',
            geoId: {
              DE: '',
              FR: '',
              EN: '',
              IT: '',
              RM: ''
            },
            desc: {
              DE: '',
              FR: '',
              EN: '',
              IT: '',
              RM: ''
            },
            geom: ''
          };

          $scope.$broadcast('modalShown', {
            feature: {
              geoId: {},
              desc: {},
              geom: 'POLYGON((481500 88000,481500 297250,832500 297250,832500 88000,481500 88000))'
            }
          });
          $scope.finishEdit = function () {
            $('#editModal').modal('hide');
            updateExtent(addService, $scope.formObj).success(function(){
              if ($location.path().contains("nonvalidated")) {
                setTimeout(function () {
                  $location.path('/validated/extents');
                }, 200);
              } else {
                $scope.reloadData();
              }
            });
          };
          $('#editModal').modal('show');
        };

      }]);

})();
