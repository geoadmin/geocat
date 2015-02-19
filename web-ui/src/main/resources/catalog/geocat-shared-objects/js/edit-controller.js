(function() {
  goog.provide('geocat_shared_objects_edit_controller');
  goog.require('geocat_shared_objects_extent_controller');
  goog.require('geocat_shared_objects_keyword_controller');
  goog.require('geocat_shared_objects_extent_directive');
  'use strict';

  var module = angular.module('geocat_shared_objects_edit_controller', [
    'geocat_shared_objects_extent_directive', 'geocat_shared_objects_extent_controller', 'geocat_shared_objects_keyword_controller']).
    controller('SharedObjEditController',  ['$scope', '$routeParams', '$http', 'extentsService', 'keywordsService', 'gnUrlUtils',

      function ($scope, $routeParams, $http, extentsService, keywordsService, gnUrlUtils) {
        var onDialogHidden = function() {
          window.close();
        };

        //alert($routeParams.href);
        var href = $routeParams.href;
        if (href.indexOf("extent") > 0) {
          $scope.formObj = angular.copy(extentsService.formObjTemplate);

          $scope.type = "extents";
          var paramMap = gnUrlUtils.parseKeyValue(href);
          var uuid = paramMap.uuid;
          var typename = paramMap.typename;

          if (typename !== 'gn:non_validated') {
            alert("Error: " + typename + " is not to be modified");
            window.close();
          }

          $http.get("reusable.list.js?maxResults=1&type=extents&q=@id@" + typename + "@" + uuid).success(function(data){
            $scope.rows = data;
            extentsService.edit(data[0], $scope);

            $('#editModal').on('hidden.bs.modal', onDialogHidden);
          }).error(function(data) {
            alert("Error occurred while looking up extent: " + data.error.message);
          });
        }


      }]);
})();
