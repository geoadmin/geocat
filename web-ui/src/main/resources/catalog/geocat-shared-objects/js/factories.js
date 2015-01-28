(function() {
  goog.provide('geocat_shared_objects_factories');
  'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('geocat_shared_objects_factories', []).
  factory('commonProperties', ['$window', '$http', '$translate', '$location', function ($window, $http, $translate, $location) {
      var loadRecords = function ($scope) {
          $scope.loading = '-1';
          var validated = $scope.isValidated ? 'true' : 'false';
          $http({ method: 'GET', url: $scope.baseUrl + '/reusable.list.js?validated=' + validated + '&type=' + $scope.type }).
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
                  alert("An error occurred when loading shared objects");
              });
      };

      return {
          addValidated: function ($scope, $routeParams) {
              $scope.type = $window.location.href.split("#")[1].split("/")[2];
              $scope.validated = $routeParams.validated;
              $scope.isValidated = $routeParams.validated === 'validated';
              if ($scope.isValidated) {
                  $scope.validatedTitle = 'reusable_validated';// Geonet.translate('reusable_validated');
              } else {
                  $scope.validatedTitle = 'reusable_nonValidated'; //Geonet.translate('reusable_nonValidated');
              }
          },
          add: function ($scope) {
              
              $scope.translate = function(key) {return key;}; //Geonet.translate;
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

              loadRecords($scope);

              $scope.reloadData = function () { loadRecords($scope); };

              $scope.loadReferencedMetadata = function (id, collapseDiv, containerDivId) {
                  $('.in').collapse('hide');
                  $scope.loading = id;
                  $('#' + collapseDiv).collapse('show');
                  $http({ method: 'GET', url: baseUrl + '/reusable.references?id=' + id + "&type=" + $scope.type + '&validated=' + $scope.isValidated }).
                     success(function (data, status, headers, config) {
                        $scope.loading = undefined;
                        $scope.metadata[id] = data;
                         $('#' + containerDivId).remove();
                     }).
                     error(function (data, status, headers, config) {
                         $scope.loading = undefined;
                         alert("An error occurred when loading referenced metadata");
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
                      alert('An error occurred during validation');
                  });
              };
              $scope.reject = { message: '' };
              $scope.performUpdateOperation = function (service) {
                  var params = {
                    type: $scope.type,
                    id: $scope.selected.id,
                    isValidObject: $scope.isValidated,
                    msg: $scope.reject.message,
                    description: $scope.selected.desc
                  };

                  if ($scope.message) {
                      params.msg = $scope.message;
                  }
                  $scope.performOperation({
                      method: 'GET',
                      url: baseUrl + '/' + service,
                      params: params
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
                  transformRequest: function(obj) {
                    var str = [];
                    for(var p in obj) {
                      if (obj.hasOwnProperty(p))
                        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                    }
                    return str.join("&");
                  },
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
                      alert('Error occurred creating a new shared object');
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
          }

      }
  }]);
})();
