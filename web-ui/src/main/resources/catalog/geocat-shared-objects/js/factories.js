(function() {
  goog.provide('geocat_shared_objects_factories');
  'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('geocat_shared_objects_factories', []).
  factory('commonProperties', ['$window', '$http', '$translate', '$location', function ($window, $http, $translate, $location) {
      var tranformToFormUrlEncoded = function(obj) {
        var str = [];
        for(var p in obj) {
          if (obj.hasOwnProperty(p))
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        }
        return str.join("&");
      };
      var loadRecords = function ($scope) {
          $scope.loading = '-1';
          var validated = $scope.isValidated ? 'true' : 'false';
          return $http({ method: 'GET', url: $scope.baseUrl + '/reusable.list.js?validated=' + validated + '&type=' + $scope.type }).
              success(function (data, status, headers, config) {
                  $scope.loading = undefined;
                  if (data.indexOf("<") != 0) {
                      for (var i = 0; i < data.length; i++) {
                          if (data[i].url) {
                              data[i].url = data[i].url.replace(/local:\/\//g, '');
                          }
                          if (data[i].desc) {
                              data[i].desc = data[i].desc.replace(/\&lt;/g, '<').replace(/\&gt;/g, '>');
                          } else {
                              data[i].desc = 'No description provided';
                          }
                      }

                      $scope.data = data;
                  }

              }).
              error(function (data, status, headers, config) {
                  $scope.loading = undefined;
                  alert("An error occurred when loading shared objects: " + data.error.message);
              });
      };

      return {
          addValidated: function ($scope, $routeParams) {
              $scope.type = $window.location.href.split("#")[1].split("/")[2].split("?")[0];
              $scope.validated = $routeParams.validated;
              $scope.isValidated = $routeParams.validated === 'validated';
              if ($scope.isValidated) {
                  $scope.validatedTitle = 'reusable_validated';// Geonet.translate('reusable_validated');
              } else {
                  $scope.validatedTitle = 'reusable_nonValidated'; //Geonet.translate('reusable_nonValidated');
              }
          },

          add: function ($scope, $routeParams) {

              $scope.sort = 'desc';
              $scope.reverseSort = false;
              $scope.refSort = function(row) {
                if (row.referenceCount === '?') {
                  return $scope.reverseSort ? -999999 : 999999;
                }

                return row.referenceCount;
              };
              $scope.language = 'eng'; //Geonet.language;
              $scope.search = {
                  search: ''
              };
              $scope.edit = {
                  
              };
              var baseUrl = '../../srv/' + $scope.language;
              $scope.baseUrl = baseUrl;
              $scope.filter = "";
              $scope.selected = null;
              $scope.select = function (row) {
                  $scope.selected = row;
              };
              $scope.data = [];
              $scope.metadata = [];

              $scope.reloadData = function () { loadRecords($scope); };
              var referenceMdUrl = function(ids, onlyCount) {
                var url = baseUrl + '/reusable.references?_content_type=json&type=' + $scope.type + '&validated=' + $scope.isValidated;
                for (var i = 0; i < ids.length; i ++) {
                  url += '&id=' + ids[i];
                }
                if (onlyCount) {
                  url += "&count=true";
                }
                return url;
              };
              $scope.loadReferencedMetadata = function (row, collapseDiv, containerDivId) {
                  var id = encodeURIComponent(row.id);
                  $('.in').collapse('hide');
                  $scope.loading = id;
                  $('#' + collapseDiv).collapse('show');
                  $http({ method: 'GET', url: referenceMdUrl([id], false) }).
                     success(function (data, status, headers, config) {
                        $scope.loading = undefined;
                        data = data[0];
                        row.referenceCount = parseInt(data['@count']);
                        $scope.metadata[row.id] = data.records;
                         $('#' + containerDivId).remove();
                     }).
                     error(function (data, status, headers, config) {
                         $scope.loading = undefined;
                         alert("An error occurred when loading referenced metadata: " + data.error.message);
                     });
              };
              $scope.edit = function (row) {
                  $scope.open(row.url);
              };
              $scope.open = function (url, params) {
                  var finalUrl = baseUrl + '/' + url;
                  if (params) {
                      if (finalUrl.indexOf("?") > -1) {
                          finalUrl += "&" + jQuery.param(params);
                      } else {
                          finalUrl += "?" + jQuery.param(params);
                      }
                  }
                  return window.open(finalUrl, '_blank');
              };
              $scope.editTitle = $translate('createNewSharedObject');
              $scope.startCreateNew = function () {
                  $scope.finishEdit = $scope.createNewObject;
                  $('#editModal').modal('show');
              };
              $scope.delete = function () {
                $scope.performUpdateOperation('reusable.reject')
              };
              $scope.updateReferenceCount = function () {
                $scope.loading = "-1";
                return $http({method: 'GET', url: referenceMdUrl([$scope.selected.id], true)}).
                  success(function (data) {
                    $scope.loading = undefined;
                    var referenceCount = parseInt(data[0]['@count']);
                    $scope.selected.referenceCount = referenceCount;
                  }).
                  error(function (data) {
                    $scope.loading = undefined;
                    alert("An error occurred when loading referenced metadata: " + data.error.message);
                  });

              };
              $scope.openRejectModal = function () {
                var rejectFunc = function() {
                  if ($scope.selected.referenceCount > 0) {
                    $scope.reject.msg = $translate('reusable_rejectDefaultMsg', $scope.selected);
                    $scope.reject.description = $scope.selected.desc;
                    $('#rejectModal').modal('show');
                  } else {
                    $('#confirmDeleteModal').modal('show');
                  }
                };

                if ($scope.selected.referenceCount != undefined) {
                  rejectFunc();
                } else {
                  $scope.updateReferenceCount().then(rejectFunc);
                }
              };
              $scope.performOperation = function (requestObject) {
                  $('.modal').modal('hide');
                  var executeModal = $('#executingOperation');

                  executeModal.modal('show');
                  return $http(requestObject)
                  .success(function (data, status, headers, config) {
                      executeModal.modal('hide');
                      loadRecords($scope);
                  })
                  .error(function (data, status, headers, config) {
                      executeModal.modal('hide');
                      alert('An error occurred during validation: ' + data.message);
                  });
              };
              $scope.reject = { msg: '', description: '', referenceCount: 0 };
              $scope.performUpdateOperation = function (service, extraParams) {
                  var params = {
                    type: $scope.type,
                    id: $scope.selected.id,
                    isValidObject: $scope.isValidated,
                    msg: $scope.reject.message,
                    description: $scope.selected.desc
                  };

                  if (extraParams) {
                      angular.extend(params, extraParams);
                  }

                  $scope.performOperation({
                      method: 'POST',
                      url: baseUrl + '/' + service,
                      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                      transformRequest: tranformToFormUrlEncoded,
                      data: params
                  });
              };


              $scope.alert = function (name) {
                  alert(name);
              };
              $scope.createNewSubtemplate = function(template, validatedUrl) {
                $scope.loading = '-1';
                var data = {
                  insert_mode:0,
                  template: 's',
                  fullPrivileges: 'y',
                  data: template,
                  group: 0,
                  extra: 'validated',
                  schema: 'iso19139.che'
                };
                $http({
                  method: 'POST',
                  url: 'md.insert?_content_type=json',
                  headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                  transformRequest: tranformToFormUrlEncoded,
                  data: data
                }).
                  success(function(data) {
                    $scope.loading = undefined;
                    var id = data.id;
                    $scope.reloadOnWindowClosed($scope.open('catalog.edit#/metadata/' + id));
                    $location.path(validatedUrl);
                  }).
                  error(function(data) {
                    $scope.loading = undefined;
                    if (data.error) {
                      alert(data.error.message);
                    } else {
                      alert('Error occurred creating a new shared object: ' + data.message);
                    }
                  });
              };
              $scope.reloadOnWindowClosed = function (win) {
                  var intervalId = setInterval(function() {
                      if (win.closed) {
                          clearInterval(intervalId);
                          $scope.reloadData();
                      }
                  }, 100);
              };
              $scope.loadReferenceCounts = function(rowIndex) {
                var ids, idMap, row, i;
                ids = [];
                idMap = {};

                if (rowIndex == 0) {
                  for (i = 0; i < $scope.data.length; i++) {
                    row = $scope.data[i];
                    if (row.referenceCount === undefined) {
                      row.referenceCount = "?";
                    }

                  }
                }

                for (i = 0; ids.length < 20 && rowIndex + i < $scope.data.length; i++) {
                  row = $scope.data[rowIndex + i];
                  if (row.referenceCount === '?') {
                    ids.push(encodeURIComponent(row.id));
                    idMap[row.id] = row;
                  }
                }

                if (ids.length > 0) {
                  row = $scope.data[rowIndex];
                  $scope.loading = "-1";
                  $http({method: 'GET', url: referenceMdUrl(ids, true)}).
                    success(function (data) {
                      $scope.loading = undefined;
                      for (var mdIdx = 0; mdIdx < data.length; mdIdx++) {
                        var md = data[mdIdx];
                        row = idMap[md['@id']];
                        row.referenceCount = parseInt(md['@count']);
                      }
                      idMap = ids = undefined;
                      $scope.loadReferenceCounts(rowIndex + i);
                    }).
                    error(function (data) {
                      $scope.loading = undefined;
                      alert("An error occurred when loading referenced metadata: " + data.error.message);
                    });
                }
              };

            return loadRecords($scope);
          }

      }
  }]);
})();
