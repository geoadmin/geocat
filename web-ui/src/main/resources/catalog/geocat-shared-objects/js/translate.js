(function () {
  goog.provide('geocat_shared_objects_translate_config');

  var module = angular.module('geocat_shared_objects_translate_config', []);

  // Define the translation files to load
  module.constant('$LOCALES', ['core', 'shared']);

  module.config(['$translateProvider', '$LOCALES',
    function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);
})();