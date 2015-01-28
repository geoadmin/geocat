(function() {
  'use strict';
  goog.provide('geocat_shared_objects_app');
  goog.require('gn_module');
  goog.require('gn_urlutils_service');
  goog.require('geocat_shared_objects_contact_controller');
  goog.require('geocat_shared_objects_deleted_controller');
  goog.require('geocat_shared_objects_extent_controller');
  goog.require('geocat_shared_objects_extent_directive');
  goog.require('geocat_shared_objects_format_controller');
  goog.require('geocat_shared_objects_keyword_controller');
  goog.require('geocat_shared_objects_factories');
  goog.require('geocat_shared_objects_translate_config');

// Declare app level module which depends on filters, and services
angular.module('geocat_shared_objects_app', [
      'gn_module',
      'gn_urlutils_service',
      'geocat_shared_objects_factories',
      'geocat_shared_objects_contact_controller',
      'geocat_shared_objects_deleted_controller',
      'geocat_shared_objects_extent_controller',
      'geocat_shared_objects_extent_directive',
      'geocat_shared_objects_format_controller',
      'geocat_shared_objects_keyword_controller',
      'geocat_shared_objects_translate_config',
      'ngRoute']).
  config(['$routeProvider', function($routeProvider) {
    var partials = '../../catalog/geocat-shared-objects/partials';

      $routeProvider.when('/:validated/contacts', { templateUrl: partials + '/shared.html', controller: 'ContactControl' });
      $routeProvider.when('/:validated/formats', { templateUrl:  partials + '/shared.html', controller: 'FormatControl' });
      $routeProvider.when('/:validated/extents', { templateUrl:  partials + '/shared.html', controller: 'ExtentControl' });
      $routeProvider.when('/:validated/keywords', { templateUrl:  partials + '/shared.html', controller: 'KeywordControl' });
      $routeProvider.when('/deleted', { templateUrl:  partials + '/shared.html', controller: 'DeletedControl' });
      $routeProvider.when('/deleted', { templateUrl:  partials + '/shared.html', controller: 'DeletedControl' });
      $routeProvider.when('/validated/deleted', { redirectTo: '/validated/contacts' });
      $routeProvider.when('/nonvalidated/deleted', { redirectTo: '/nonvalidated/contacts' });
      $routeProvider.otherwise({ redirectTo: '/nonvalidated/contacts' });
  }])

 .constant('gnSearchSettings', {});

})();

