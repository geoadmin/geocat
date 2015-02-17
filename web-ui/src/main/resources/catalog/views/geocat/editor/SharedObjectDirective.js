(function() {
  goog.provide('gc_sharedobject');

  goog.require('gn_editor_xml_service');
  goog.require('gn_metadata_manager_service');
  goog.require('gn_schema_manager_service');

  var module = angular.module('gc_sharedobject', [
    'gn_metadata_manager_service',
    'gn_schema_manager_service',
    'gn_editor_xml_service'
  ]);


  module.directive('gcAddSharedobject', [
    '$rootScope', '$timeout', '$q', '$http',
    'gnEditor', 'gnSchemaManagerService',
    'gnEditorXMLService', 'gnHttp', 'gnConfig',
    'gnCurrentEdit', 'gnConfigService', 'gnElementsMap', 'gcSharedobject',
    function($rootScope, $timeout, $q, $http,
             gnEditor, gnSchemaManagerService,
             gnEditorXMLService, gnHttp, gnConfig,
             gnCurrentEdit, gnConfigService, gnElementsMap, gcSharedobject) {

      return {
        restrict: 'A',
        replace: false,
        scope: {
          mode: '@gcAddSharedobject',
          elementName: '@',
          elementRef: '@',
          domId: '@',
          // Contact subtemplates allows definition
          // of the contact role. For other cases
          // only add action is provided
          templateType: '@',
          // If true, display button to add the element
          // without using the subtemplate selector.
          templateAddAction: '@',
          // Parameters to be send when the subtemplate
          // snippet is retrieved before insertion
          // into the metadata records.
          variables: '@'
        },
        templateUrl: '../../catalog/views/geocat/editor/partials/' +
            'sharedobject.html',

        link: function(scope, element, attrs) {

          var separator = '&&&';

          // xlink local
          var url = 'local://' + scope.$parent.lang + '/subtemplate';

          angular.extend(scope, {
            gnConfig: gnConfig,
            templateAddAction: scope.templateAddAction === 'true',
            isContact: scope.templateType === 'contacts',
            hasDynamicVariable: scope.variables &&
                scope.variables.match('{.*}') !== null,
            snippet: null,
            snippetRef: gnEditor.
                buildXMLFieldName(scope.elementRef, scope.elementName)
          });


          /**
           * Load shared object list depending on type and search filter
           */
          scope.loadSO = function() {
            gcSharedobject.loadRecords(scope.templateType, scope.searchValue).
                then(function(data) {
                  scope.objects = data;
                }
            );
          };

          scope.setRole = function(role) {
            scope.role = role;
          };

          scope.add = function() {
            gnEditor.add(gnCurrentEdit.id,
                scope.elementRef, scope.elementName,
                scope.domId, 'before').then(function() {
                  if (scope.templateAddAction) {
                    gnEditor.save(gnCurrentEdit.id, true);
                  }
                });
            return false;
          };

          // <request><codelist schema="iso19139"
          // name="gmd:CI_RoleCode" /></request>
          scope.addEntry = function(entry, role, usingXlink) {
            if (!(entry instanceof Array)) {
              entry = [entry];
            }

            scope.snippet = '';
            var snippets = [];

            var checkState = function() {
              if (snippets.length === entry.length) {
                scope.snippet = snippets.join(separator);

                // Clean results
                // TODO: should call clean result from
                // searchFormController
                //                   scope.searchResults.records = null;
                //                   scope.searchResults.count = null;

                $timeout(function() {
                  // Save the metadata and refresh the form
                  gnEditor.save(gnCurrentEdit.id, true);
                });
              }
            };

            angular.forEach(entry, function(c) {
              var uuid = c.id;
              var params = {uuid: uuid};

              // For the time being only contact role
              // could be substitute in directory entry
              // selector. This is done using the process
              // parameter of the get subtemplate service.
              // eg. data-variables="gmd:role/gmd:CI_RoleCode
              //   /@codeListValue~{role}"
              // will set the role of the contact.
              // TODO: this could be applicable not only to contact role
              // No use case identified for now.
              if (scope.hasDynamicVariable && role) {
                params.process =
                    scope.variables.replace('{role}', role);
              } else if (scope.variables) {
                params.process = scope.variables;
              } else {
                params.process = '';
              }
              gnHttp.callService(
                  'subtemplate', params).success(function(xml) {
                    if (usingXlink) {
                      snippets.push(gnEditorXMLService.
                          buildXMLForXlink(scope.elementName,
                              url +
                              '?uuid=' + uuid +
                              '&process=' + params.process));
                    } else {
                      snippets.push(gnEditorXMLService.
                          buildXML(scope.elementName, xml));
                    }
                    checkState();
                  });
            });

            return false;
          };

          gnSchemaManagerService
              .getCodelist(gnCurrentEdit.schema + '|' +
                  gnElementsMap['roleCode'][gnCurrentEdit.schema])
              .then(function(data) {
                scope.roles = data[0].entry;
              });

        }
      };
    }]);

  module.service('gcSharedobject', [
    '$q',
    '$http',
    function ($q, $http) {

      this.loadRecords = function (type, searchValue, validated) {

        var defer = $q.defer();

        $http({
          method: 'GET',
          url: 'reusable.list.js',
          params: {
            validated: validated,
            type: type,
            q: searchValue
          }}).
            success(function (data) {
              if (data.indexOf("<") != 0) {
                for (var i = 0; i < data.length; i++) {
                  if (data[i].url) {
                    data[i].url = data[i].url.replace(/local:\/\//g, '');
                  }
                  if (data[i].desc) {
                    data[i].desc = data[i].desc.replace(/\&lt;/g, '<').
                        replace(/\&gt;/g, '>');
                  } else {
                    data[i].desc = 'No description provided';
                  }
                  data[i].validated = data[i].validated == 'true';
                }
                defer.resolve(data);
              }

            }).
            error(function (data) {
              alert("An error occurred when loading shared objects: " +
                  data.error.message);
              defer.reject(data);
            });
        return defer.promise;
      };
    }]);

})();
