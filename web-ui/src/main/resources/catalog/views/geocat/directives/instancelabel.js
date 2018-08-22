(function() {

  goog.provide('gc_instance_label');

  var module = angular.module('gc_instance_label', []);


  module.directive('gcInstanceLabel',
    function() {
      return {
        restrict: 'E',
        replace: true,
        templateUrl: '../../catalog/views/geocat/directives/' +
            'partials/instancelabel.html',
        link: function linkFn(scope, element, attrs) {
          scope.isDevInstance = function() {
            return scope.info && scope.info['system/site/name'] &&
                scope.info['system/site/name'].toLowerCase()
                .indexOf('[dev]') > -1;
          }
          scope.isIntInstance = function() {
            return scope.info && scope.info['system/site/name'] &&
                scope.info['system/site/name'].toLowerCase()
                .indexOf('[int]') > -1;
          }
        }
      };
    }
  );
})();
