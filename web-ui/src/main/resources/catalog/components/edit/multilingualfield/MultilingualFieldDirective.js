(function() {
  goog.provide('gn_multilingual_field_directive');

  var module = angular.module('gn_multilingual_field_directive', []);

  /**
   * Decorate a set of multilingual inputs or textareas with:
   * * a button to switch from language selector to a display
   * all languages mode
   * * a list of language to display the matching input.
   *
   * On initialization, a language label is inserted before each
   * fields.
   *
   * It also set direction attribute for RTL language.
   *
   */
  module.directive('gnMultilingualField', ['$timeout', '$translate', '$http',
    function($timeout, $translate, $http) {

      return {
        restrict: 'A',
        transclude: true,
        templateUrl: '../../catalog/components/edit/' +
            'multilingualfield/partials/multilingualfield.html',
        scope: {
          mainLanguage: '@',
          expanded: '@'
        },
        link: function(scope, element, attrs) {
          // Only inputs and textarea could be multilingual fields
          var formFieldsSelector = 'input,textarea';

          // Some input should be displayed in Right-To-Left direction
          var rtlLanguages = ['AR'];

          // Get languages from attributes (could be grab from the
          // form field ? FIXME)
          scope.languages = angular.fromJson(attrs.gnMultilingualField);
          var mainLanguage = scope.mainLanguage;
          // Have to map the main language to one of the languages in the inputs
          if (angular.isDefined(scope.languages[mainLanguage])) {
            mainLanguage = scope.languages[mainLanguage].substring(1);
          } else {
            $(element).find(formFieldsSelector).each(function() {
              var lang = $(this).attr('lang');
              if (angular.isDefined(scope.languages[lang])) {
                mainLanguage = scope.languages[lang].substring(1);
              }
            });
          }

          if (!mainLanguage) {
            // when there is a gco:CharacterString and there is no
            // PT_FreeText with the same language
            // then the scope.languages map has an entry mainLanguage -> #
            // but the problem is that the input element will have the 'lang'
            // attribute to be eng (not empty string).  So
            // we need to update the map and main language to be '#' +
            // scope.mainLanguage so that all the looks ups
            // can be done correctly.
            mainLanguage = scope.mainLanguage;
            scope.languages[mainLanguage] = '#' + mainLanguage;
          }


          scope.hasData = {};

          var toTest, urlTestTimer, validUrls;

          scope.currentLanguage = mainLanguage;
          var setError = function() {
            element.removeClass('testing-url');
            if (validUrls) {
              element.removeClass('has-error');
            } else {
              element.addClass('has-error');
            }
          };
          scope.validateUrl = function(langId) {
            if (attrs['validateUrl'] === 'true') {
              if (urlTestTimer !== undefined) {
                $timeout.cancel(urlTestTimer);
              }
              if (toTest === undefined) {
                toTest = {};
              }
              toTest[langId] = true;
              validUrls = true;
              urlTestTimer = $timeout(function() {
                if (toTest !== undefined) {
                  $(element).find(formFieldsSelector).each(function () {
                    var inEl = $(this);
                    var url = inEl.val();
                    if (toTest[inEl.attr('lang')] && url !== '') {
                      if (new RegExp("^https?://.+").test(url)) {
                        element.addClass('testing-url');
                        element.removeClass('has-error');
                        var proxiedURL = "/geonetwork/proxy?url=" + encodeURIComponent(url);
                        $http.head(proxiedURL).then(function () {
                          setError();
                        }).catch(function () {
                          // head sometimes returns 404 even if it is a redirect (using the http client java library which the proxy is doing)
                          $http.get(proxiedURL).then(function () {
                            setError();
                          }).catch(function () {
                            validUrls = false;
                            setError();
                          });
                        });
                      } else {
                        element.addClass('has-error');
                      }
                    }
                  });
                  toTest = undefined;
                }
              }, 1000);
            }
          };
          /**
           * Get the 3 letter code set in codeListValue
           * from a lang identifier eg. "EN"
           */
          function getISO3Code(langId) {
            var langCode = null;
            angular.forEach(scope.languages,
                function(key, value) {
                  if (key === '#' + langId) {
                    langCode = value;
                  }
                }
            );
            return langCode;
          }

          $timeout(function() {
            scope.expanded = scope.expanded === 'true';

            $(element).find(formFieldsSelector).each(function() {
              var inputEl = $(this);
              var langId = inputEl.attr('lang');

              // FIXME : should not be the id but the ISO3Code
              if (langId) {
                // Add the language label
                inputEl.before('<span class="label label-primary">' +
                    $translate(getISO3Code(langId)) + '</span>');

                // Set the direction attribute
                if ($.inArray(langId, rtlLanguages) !== -1) {
                  inputEl.attr('dir', 'rtl');
                }

                var setNoDataClass = function() {
                  var code = ('#' + langId);
                  scope.hasData[code] = inputEl.val().trim().length > 0;

                  scope.validateUrl(langId);
                };

                inputEl.on('keyup', setNoDataClass);

                setNoDataClass();
                scope.validateUrl(langId);
              }
            });

            // By default, do not expand fields
            scope.displayAllLanguages(scope.expanded);
          });

          scope.switchToLanguage = function(langId) {
            scope.currentLanguage = langId.replace('#', '');
            $(element).find(formFieldsSelector).each(function() {
              if ($(this).attr('lang') === scope.currentLanguage ||
                ($(this).attr('lang') === mainLanguage &&
                  scope.currentLanguage === '')) {
                $(this).removeClass('hidden').focus();
              } else {
                $(this).addClass('hidden');
              }
            });
          };

          var setLabel = function(key) {
            scope.languageSwitchLabel = $translate(key);
            scope.languageSwitchHelp = $translate(key + '-help');
          };

          scope.displayAllLanguages = function(force, focus) {
            scope.expanded =
                force !== undefined ? force : !scope.expanded;

            $(element).find(formFieldsSelector).each(function() {
              if (scope.expanded) {
                setLabel('oneLanguage');
                $(this).prev('span').removeClass('hidden');
                var el = $(this).removeClass('hidden');
                if (focus) {
                  el.focus();
                }
              } else {
                setLabel('allLanguage');
                $(this).prev('span').addClass('hidden');

                if ($(this).attr('lang') !== mainLanguage) {
                  $(this).addClass('hidden');
                } else {
                  scope.currentLanguage = mainLanguage;
                  var el = $(this).removeClass('hidden');
                  if (focus) {
                    el.focus();
                }
              }
              }
            });
          };

          scope.sortedLanguages = [];
          angular.forEach(scope.languages, function(shortLang, longLang) {
            scope.sortedLanguages.push({
              longLang: longLang,
              shortLang: shortLang
            })
          });
          var sortVal = function (o) {
            if (o.longLang === scope.mainLanguage) {
              return -1;
            }
            if (o.longLang.charAt(0) == 'g') {
              return 0;
            } else if (o.longLang.charAt(0) == 'f') {
              return 2;
            } else  if (o.longLang.charAt(0) == 'i') {
              return 3;
            } else if (o.longLang.charAt(0) == 'e') {
              return 4;
            } else if (o.longLang.charAt(0) == 'r') {
              return 5;
            } else {
              return 50;
            }
          };
          scope.sortedLanguages.sort(function(o1, o2){
            return sortVal(o1) - sortVal(o2);
          });

        }
      };
    }]);
})();
