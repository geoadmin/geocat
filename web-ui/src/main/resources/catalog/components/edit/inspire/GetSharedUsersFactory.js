(function() {
  'use strict';
  goog.provide('inspire_get_shared_users_factory');

  var module = angular.module('inspire_get_shared_users_factory', []);
  /**
   * Returns results of shared users request:
   *
   * {
   *     "url":"local://shared.user.edit?closeOnSave&id=5&validated=n&operation=fullupdate",
   *     "id":"5",
   *     "type":"contact",
   *     "xlink":"local://xml.user.get?id=5*",
   *     "desc":"&lt;geodata@swisstopo.ch&gt;",
   *     "search":"5 &lt;geodata@swisstopo.ch&gt;"
   * }
   */
  module.factory('inspireGetSharedUsersFactory', [ '$http', '$q', function($http, $q) {
    var selectArray = function(json, path) {
      var i;
      for(i = 0; i < path.length; i++) {
        if (angular.isDefined(json)) {
          if (angular.isArray(json)) {
            var data = json;
            json = [];
            angular.forEach(data, function(elem) {
              var val = elem[path[i]];
              if (angular.isArray(val)) {
                json = json.concat(val);
              } else {
                json.push(val);
              }
            });
          } else if (!angular.isArray(json)) {
            json = json[path[i]];
          } else {
            json = undefined;
          }
        }
      }

      if (angular.isDefined(json)) {
        if (!angular.isArray(json)) {
          json = [json];
        }
        return json;
      } else {
        return [];
      }
    };
    var charString = function (json, path, def) {
      var result = selectArray(json, path.concat(['gco:CharacterString', '#text']));
      if (result.length > 0) {
          return result[0];
      } else {
        return def;
      }
    };
    return {
      loadDetails: function(url, userId, validated) {
        var deferred = $q.defer();

        $http.get(url + 'subtemplate?_content_type=json&uuid=' + userId).success(function(data) {
          var locale, threeLetterCode;
          var langMap = {
            '#DE': 'ger',
            '#EN': 'eng',
            '#FR': 'fre',
            '#IT': 'ita',
            '#RM': 'roh'
          };
          var user = {
            id: userId,
            name: charString(data, ['che:individualFirstName'], ''),
            surname: charString(data, ['che:individualLastName'], ''),
            email: charString(data, ['gmd:contactInfo', 'gmd:CI_Contact', 'gmd:address', 'che:CHE_CI_Address', 'gmd:electronicMailAddress'], ''),
            organization: {},
            validated: validated === "true"
          };

          var orgName = selectArray(data, ['gmd:organisationName', 'gmd:PT_FreeText', 'gmd:textGroup', 'gmd:LocalisedCharacterString'], []);
          for (var i = 0; i < orgName.length; i++) {
            locale = orgName[i];
            threeLetterCode = langMap[locale['@locale']];
            user.organization[threeLetterCode] = orgName[i]['#text'];;
          }

          deferred.resolve(user);
        }).error(function (data) {
          deferred.reject(data);
        });
        return deferred.promise;
      },
      loadAll: function(url) {
        var deferred = $q.defer();
        var validated = false, nonValidated = false;
        var users = {
          validated: [],
          nonValidated: []
        };
        var error = function (error) {
          deferred.reject(error);
        };
        var contactURL = url + 'reusable.list.js?type=contacts&validated=';

        var processData = function(data) {
          var i;
          if (data.indexOf("<") !== 0) {
            for (i = 0; i < data.length; i++) {
              if (data[i].url) {
                data[i].url = data[i].url.replace(/local:\/\//g, '');
              }
              if (data[i].desc) {
                data[i].desc = data[i].desc.replace(/\&lt;/g, '<').replace(/\&gt;/g, '>');
              } else {
                data[i].desc = 'No description provided';
              }
            }

            data.sort(function(u1, u2) {
              var startsWithCharacter = /^[0-9a-zA-Z]/;
              if (startsWithCharacter.test(u1.desc) && !startsWithCharacter.test(u2.desc)) {
                return -1;
              } else if (!startsWithCharacter.test(u1.desc) && startsWithCharacter.test(u2.desc)) {
                return 1;
              }
              return u1.desc.localeCompare(u2.desc);
            });

            return data;
          }
          return undefined;
        };

        $http.get(contactURL + false).then (
          function success(data) {
            nonValidated = true;

            users.nonValidated = processData(data.data);
            if (validated) {
              deferred.resolve(users);
            }
          },
          error
        );

        $http.get(contactURL + true).then (
          function success(data) {
            validated = true;
            users.validated = processData(data.data);
            if (nonValidated) {
              deferred.resolve(users);
            }
          },
          error
        );

        return deferred.promise;
      }
    };
  }]);
}());

