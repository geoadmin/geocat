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
        '$timeout',
        '$http',
        'gnGlobalSettings',
        'commonProperties',
        'extentsService',
      function ($scope, $routeParams, $location, $timeout, $http,
                gnGlobalSettings, commonProperties, extentsService) {

        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);


        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_extent';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_extent';
        }

        // Initiate obj that will be filled by extent form directive
        $scope.formObj = angular.copy(extentsService.formObjTemplate);

        $scope.edit = function (row) {extentsService.edit(row, $scope)};
        $scope.startCreateNew = function () {
          $scope.formObj = {
            typename: 'gn:xlinks',
            geomString: '',
            proj: gnGlobalSettings.srs,
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
              geom: gnGlobalSettings.defaultBbox
            }
          });
          $scope.finishEdit = function () {
            $('#editModal').modal('hide');

            var serverProj = gnGlobalSettings.srs;
            var formObj = angular.copy($scope.formObj);
            var formatWKT = new ol.format.WKT();

            if (formObj.proj != serverProj) {
              formObj.geomString = formatWKT.writeGeometry(formatWKT.readGeometry(formObj.geomString).transform(formObj.proj, serverProj));
              formObj.proj = serverProj;
            }

            extentsService.updateExtent(extentsService.addService, formObj).success(function(data){
              $timeout(function () {
                var id = /.+id=(\d+).*/.exec(data[0])[1];
                $location.url('/validated/extents?search='+id);
              }, 200);
            });
          };
          $('#editModal').modal('show');
        };

}]).factory('extentsService', ['gnUrlUtils', 'gnGlobalSettings', '$http', function(gnUrlUtils, gnGlobalSettings, $http) {

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
      var getBestTranslation = function ($scope, obj) {
        var curLang = $scope.lang.substring(0, 2).toUpperCase();
        var val = getString(obj[curLang]);
        angular.forEach(['DE', 'FR', 'IT', 'EN', 'RM'], function(lang) {
          if (!val && curLang !== lang) {
            val = getString(obj[lang]);
          }
        });
        return val;
      };
      var bestDescription = function($scope) {
        var geoId = getBestTranslation($scope, $scope.formObj.geoId);
        var desc = getBestTranslation($scope, $scope.formObj.desc);

        geoId = geoId ? "("+geoId+")" : "";
        desc = desc ? desc + " " : "";

        var finalDesc = desc + geoId;
        return finalDesc;
      };

      var service = {
        edit: function (row, $scope) {
          angular.extend($scope.formObj, {
            id: row.id,
            typename: gnUrlUtils.parseKeyValue(row.url).typename
          });

          $http({
            method: 'GET',
            url: service.getService,
            params: {
              id: $scope.formObj.id,
              typename: $scope.formObj.typename,
              format: 'wkt',
              crs: gnGlobalSettings.srs,
              _content_type: 'json'
            }
          }).success(function (data) {
            $scope.$broadcast('modalShown', data[0].featureType);
            $scope.finishEdit = function () {
              service.updateExtent(service.updateService, $scope.formObj);
              $('#editModal').modal('hide');
              row.desc = bestDescription($scope);
            };

            $('#editModal').modal('show');

          });
        },
        updateExtent: function (service, o) {
          var params = [
            'typename=' + o.typename,
            'geom=' + o.geomString,
            'crs=' + o.proj,
            'format=WKT'
          ];
          if (o.id !== undefined) {
            params.push('id=' + o.id);
          }
          encodeTranslation(params, "geoId", o.geoId);
          encodeTranslation(params, "desc", o.desc);

          return $http({
            method: 'POST',
            url: service,
            params: {
              _content_type: 'json'
            },
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            data: params.join("&")
          });
        },
        formObjTemplate: {
          langs: ['DE', 'EN', 'FR', 'IT', 'RM']
        },
        getService: 'xml.extent.get',
        addService: 'xml.extent.add',
        updateService: 'xml.extent.update'
    };

      return service;
    }]);

})();
