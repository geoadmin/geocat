(function () {
  goog.provide('geocat_shared_objects_keyword_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  /* Controllers */

  var module = angular.module('geocat_shared_objects_keyword_controller', []).
    controller('KeywordControl', ['$scope', '$routeParams', '$http', 'commonProperties',
      function ($scope, $routeParams, $http, commonProperties) {
        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_keyword';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_keyword';
        }
        $scope.keyword = {
          eng: {label: '', desc: ''},
          fre: { label: '', desc: '' },
          ger: { label: '', desc: '' },
          ita: { label: '', desc: '' },
          roh: { label: '', desc: '' }
        };

        $scope.edit = function (row) {
          var parts = row.url.substring(row.url.indexOf('?') + 1).split(/\&/g, 2);
          var thesaurus = '';
          var id = '';

          for (var i = 0; i < parts.length; i++) {
            if (parts[i].indexOf('thesaurus=') > -1) {
              thesaurus = decodeURIComponent(parts[i].split(/=/, 2)[1]);
            }

            if (parts[i].indexOf('id=') > -1) {
              id = decodeURIComponent(parts[i].split(/=/, 2)[1]);
            }
          }

          $http({
            method: 'GET',
            url: $scope.baseUrl + '/json.keyword.get',
            params: {
              lang: 'eng,fre,ger,roh,ita',
              id: id,
              thesaurus: thesaurus
            }
          })
            .success(function (data) {
              $scope.finishEdit = function () {
                $scope.submitEdit(thesaurus, id);
              };
              for (var lang in $scope.keyword) {
                $scope.keyword[lang].label = data[lang].label;
                $scope.keyword[lang].desc = data[lang].definition;
              }
              $('#editModal').modal('show');
            });
        };

        var createUpdateParams = function () {
          var params = {
            ref: 'local._none_.geocat.ch',
            refType: '_none_',
            namespace: 'http://custom.shared.obj.ch/concept#',
            id: ''
          };

          var isEmpty = true;
          for (var lang in $scope.keyword) {
            if ('' !== $scope.keyword[lang].label) {
              isEmpty = false;
              params['loc_' + lang + '_label'] = $scope.keyword[lang].label;
            }
            if ('' !== $scope.keyword[lang].desc) {
              isEmpty = false;
              params['loc_' + lang + '_definition'] = $scope.keyword[lang].desc;
            }
          }

          return params;
        };

        $scope.submitEdit = function (thesaurus, id) {
          var params = createUpdateParams();
          var parts = id.split('#', 2);
          params.newid = parts[1];
          params.oldid = parts[1];
          params.namespace = parts[0];

          params.ref = thesaurus;

          $scope.performOperation({
            method: 'POST',
            url: $scope.baseUrl + '/thesaurus.updateelement',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            data: $.param(params)
          });
        };

        $scope.createNewObject = function () {
          var params = createUpdateParams();

          if (!isEmpty) {
            $scope.performOperation({
              method: 'POST',
              url: $scope.baseUrl + '/thesaurus.addelement',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              data: $.param(params)
            }).
              success(function () {
                for (var lang in $scope.keyword) {
                  $scope.keyword[lang].label = '';
                  $scope.keyword[lang].desc = '';
                }
                $location.path("/validated/keywords");
              });
          } else {
            $('#editModal').modal('hide');
          }
        }
      }]);
})();
