(function() {
  goog.provide('gc_mdactions_directive');

  var module = angular.module('gc_mdactions_directive', []);
  /**
   * Directive that updates all the multilingual fields to show a different language
   */
  module.directive('gcLangFieldSwitcher', ['$translate', '$timeout',
    function($translate, $timeout) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/geocat/editor/partials/' +
            'gclangfieldswitcher.html',
        scope: { },
        link: function(scope) {
          scope.languages = ["ger", "fre", "ita", "eng", "roh"];

          scope.showLang = function(lang, $event) {
            var y = window.scrollY;
            $event.stopPropagation();
            $timeout(function(){
              $("a.lang-switcher-pill[lang='" + lang + "']").click();
              $timeout(function() {
                if (window.scrollY !== y) {
                  window.scrollTo(0, y);
                }
              },200);
            },0);
          };
        }
      };
    }]);
})();
