(function() {
  'use strict';
  goog.provide('geocat_shared_objects_edit');
  goog.require('gn_module');
  goog.require('gn_urlutils_service');
  goog.require('geocat_shared_objects_edit_controller');
  goog.require('geocat_shared_objects_extent_controller');
  goog.require('geocat_shared_objects_extent_directive');
  goog.require('geocat_shared_objects_keyword_controller');
  goog.require('geocat_shared_objects_factories');
  goog.require('geocat_shared_objects_translate_config');

// Declare app level module which depends on filters, and services
angular.module('geocat_shared_objects_edit', [
      'gn_module',
      'gn_urlutils_service',
      'geocat_shared_objects_factories',
      'geocat_shared_objects_edit_controller',
      'geocat_shared_objects_extent_controller',
      'geocat_shared_objects_extent_directive',
      'geocat_shared_objects_keyword_controller',
      'geocat_shared_objects_translate_config',
      'ngRoute']).
  config(['$routeProvider', function($routeProvider) {
    var partials = '../../catalog/geocat-shared-objects/partials';
      $routeProvider.when('/edit', { templateUrl: partials + '/edit.html', controller: 'SharedObjEditController' });
      $routeProvider.otherwise({ redirectTo: '/edit' });
  }])

 .constant('gnSearchSettings', {});

})();

