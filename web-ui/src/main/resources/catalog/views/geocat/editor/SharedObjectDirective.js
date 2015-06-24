(function() {
  goog.provide('gc_sharedobject');

  goog.require('gn_editor_xml_service');
  goog.require('gn_metadata_manager_service');
  goog.require('gn_schema_manager_service');
  goog.require('geocat_shared_objects_extent_directive');
  goog.require('geocat_shared_objects_extent_controller');
  goog.require('geocat_shared_objects_keyword_controller');
  goog.require('shared-object-subtemplate-service');

  var module = angular.module('gc_sharedobject', [
    'gn_metadata_manager_service',
    'gn_schema_manager_service',
    'gn_editor_xml_service',
    'geocat_shared_objects_extent_directive',
    'geocat_shared_objects_extent_controller',
    'shared-object-subtemplate-service',
    'geocat_shared_objects_keyword_controller'
  ]);

  module.config(['$LOCALES', function($LOCALES) {
    $LOCALES.push('shared');
  }]);


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

          scope.prop = {};

          /**
           * Load shared object list depending on type and search filter
           */
          scope.loadSO = function(e) {
            scope.entry = undefined;

            if(e) {
              if(element.find('.gc-shared-object').hasClass('open')) {
                e.stopImmediatePropagation();
              }
            }

            var validated;
            if(scope.templateType == 'extents' && scope.regionType) {
              validated = 'gn:' + scope.regionType;
            }
            gcSharedobject.loadRecords(scope.templateType, scope.searchValue,
            validated).
                then(function(data) {
                  scope.objects = data;
                }
            );
          };

          scope.setRole = function(role) {
            scope.role = role;
          };

          scope.setRegionType = function(regionType) {
            scope.regionType = regionType;
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

          scope.selectEntry = function(entry) {
            scope.searchValue = entry.desc;
            scope.entry = entry;
            var role = angular.isDefined(scope.role) ? scope.role.code : undefined;
            scope.addEntry(entry, role, true);
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
                scope.snippet = snippets.join(gcSharedobject.separator);
                $timeout(function() {
                  // Save the metadata and refresh the form
                  gnEditor.save(gnCurrentEdit.id, true);
                });
              }
            };

            angular.forEach(entry, function(c) {
              var extraXLinkParams = {
                'xlink:show' : 'embed'
              };
              if (!c.validated) {
                extraXLinkParams['xlink:role'] = "http://www.geonetwork.org/non_valid_obj";
              }
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
              if(scope.templateType == 'contacts' ||
                  scope.templateType == 'formats') {
                gcSharedobject.addXlinkXml(scope, snippets, extraXLinkParams, extraXLinkParams, c.xlink, params).then(checkState);
              }
              else if(scope.templateType == 'extents') {

                c.xlink = c.xlink.replace('*', 'extentTypeCode=true&format=AUTO');

                var extUrl = c.xlink.replace(/local:\/\//g, '');

                $http.get(extUrl).success(function(xml) {
                  if (usingXlink) {
                    snippets.push(gnEditorXMLService.
                        buildXMLForXlink(scope.elementName,
                            c.xlink +
                            '&uuid=' + uuid, extraXLinkParams));
                  } else {
                    snippets.push(gnEditorXMLService.
                        buildXML(scope.elementName, xml));
                  }
                  checkState();
                });
              }
            });
            return false;
          };

          if(scope.templateType == 'contacts') {
            gnSchemaManagerService
                .getCodelist(gnCurrentEdit.schema + '|' +
                    gnElementsMap['roleCode'][gnCurrentEdit.schema])
                .then(function(data) {
                  scope.roles = data[0].entry;
                });
          }
          else if(scope.templateType == 'extents') {
            $http.get('reusable.object.categories/extents').success(function(data) {
              scope.regionTypes = data;
            });

          }

          scope.editEntry = function() {
            gcSharedobject.editEntry(this.templateType, undefined, scope);
          };

          /**
           * Will add a classic extent panel for temporal or vertical ..
           */
          scope.addExtent = function() {
            gnEditor.add(gnCurrentEdit.id,
                scope.elementRef, scope.elementName, scope.domId, 'before');
          };
        }
      };
    }]);

  module.service('gcSharedobject', [
    '$q',
    '$http',
    '$rootScope',
    '$timeout',
    'gnEditor',
    'gnPopup',
    'gnUrlUtils',
    'gnHttp',
    'gnEditorXMLService',
    'gnCurrentEdit',
    'extentsService',
    'keywordsService',
    'subtemplateService',
    'gnThesaurusService',
    'Keyword',
    function ($q,
              $http,
              $rootScope,
              $timeout,
              gnEditor,
              gnPopup,
              gnUrlUtils,
              gnHttp,
              gnEditorXMLService,
              gnCurrentEdit,
              extentsService,
              keywordsService,
              subtemplateService,
              gnThesaurusService,
              Keyword) {


      /**
       * Load a list of shared object and format response
       * @param type
       * @param searchValue
       * @param validated
       * @returns {*}
       */
      this.loadRecords = function (type, searchValue, validated) {

        var defer = $q.defer();

        $http({
          method: 'GET',
          url: 'reusable.list.js',
          params: {
            validated: validated,
            type: type,
            q: searchValue,
            maxResults: 10
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

      var scope;
      var finishEditExtent = function() {
        var service = scope.xlink ? extentsService.updateService :
            extentsService.addService;

        extentsService.updateExtent(service, scope.formObj).
            success(function(data) {

              $('#sharedobjectModal').modal('hide');

              if(!scope.xlink) {

                var params = gnUrlUtils.parseKeyValue(data[0].split('?')[1]);
                params.xlink = data[0];

                // get the first extent directive to insert XML snippet once.
                angular.element(
                    $('[gc-add-sharedobject][data-template-type="extents"]').
                        first()).isolateScope().
                    addEntry(params, undefined, true);
              }
              else {
                gnEditor.save(true);
              }
            });
      };

      var finishEditKeyword = function() {

        var params = keywordsService.createUpdateParams(scope.keyword);
        params.params.namespace = 'http://geocat.ch/concept#';
        params.params.ref = 'local._none_.non_validated';

        if(scope.xlink) {
          var parts = scope.xlink.props.uri.split('#', 2);
          params.params.newid = parts[1];
          params.params.oldid = parts[1];

        }
        if (!params.isEmpty) {
          $http({
            method: 'POST',
            url: 'geocat.thesaurus.updateelement',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            data: $.param(params.params),
            params: {
              _content_type: 'json'
            }
          }).success(function (data) {

            $('#sharedobjectModal').modal('hide');
            var kwUrl = gnThesaurusService.getKeywordsSearchUrl('',
                'local._none_.non_validated', 1, 2);

            kwUrl = kwUrl.replace('pKeyword=', 'pUri=' +
                encodeURIComponent(data.id));

            $http.get(kwUrl).success(function(data) {
              if(data && angular.isArray(data))

              var newKw = new Keyword(data[0][0]);
                var s = angular.element(
                    $('[data-thesaurus-key="'+ params.params.ref +'"]').
                        first()).isolateScope();

              // Case where the non-validated thesaurus exists
              if(s) {
                s.addKeyword(newKw);
              }

              // We add it into All thesaurus
              else {
                s = angular.element(
                  $('[data-thesaurus-key="external.none.allThesaurus"]').
                        first()).isolateScope();

                newKw.props.uri = 'http://org.fao.geonet.thesaurus.all/' +
                    'local._none_.non_validated@@@' +
                    encodeURIComponent(newKw.props.uri);

                s.addKeyword(newKw);
              }
            });
          });
        }
      };

      var editExtent = function(xlink) {

        scope.finishEdit = finishEditExtent;
        scope.formObj = angular.copy(extentsService.formObjTemplate);

        var modalConfig = {
          title: 'createNewSharedObject',
          id: 'sharedobjectModal',
          content: '<form class="form-horizontal" role="form"><div gc-edit-extent=""></div></form>',
          footer: '<div class="modal-footer">' +
          '<button type="button" class="btn btn-default" data-dismiss="modal" translate>cancel</button>' +
          '<button type="button" class="btn btn-primary" data-ng-click="finishEdit()" translate>accept</button>' +
          '</div>'
        };

        // Extent creation
        if(!xlink) {
          scope.formObj.typename = 'gn:non_validated';
          gnPopup.createModal(modalConfig, scope);
          $timeout(function() {
            $rootScope.$broadcast('modalShown', {
              feature: {
                geoId: {},
                desc: {},
                geom: 'POLYGON((481500 88000,481500 297250,832500 297250,832500 88000,481500 88000))'
              }
            })
          }, 200, true);

        }
        // Extent edition
        else {

          var params = gnUrlUtils.parseKeyValue(xlink.split('?')[1]);
          angular.extend(scope.formObj, {
            id: params.id,
            typename: params.typename
          });

          $http({
            method: 'GET',
            url: extentsService.getService,
            params: {
              id: scope.formObj.id,
              typename: scope.formObj.typename,
              format: 'wkt',
              crs: 'EPSG:21781',
              _content_type: 'json'
            }
          }).success(function (data) {

            gnPopup.createModal(modalConfig, scope);
            $timeout(function() {
              $rootScope.$broadcast('modalShown',  data[0].featureType)
            }, 200, true);
          });
        }
      };

      var editKeyword = function (xlink) {

        scope.finishEdit = finishEditKeyword;
        scope.keyword = angular.copy(keywordsService.defaultKeyword);

        var modalConfig = {
          title: 'createNewSharedObject',
          id: 'sharedobjectModal',
          content: '<form class="form-horizontal" role="form">' +
          '<div class="form-group" ng-repeat="(lang, obj) in keyword">' +
          '  <label class="col-lg-2" for="{{$index}}Label">{{lang | translate}}</label>' +
          '    <span class="col-lg-5">' +
          '      <input type="text" class="form-control" ' +
          '         ng-model="keyword[lang].label" name="{{lang}}Label"' +
          '         id="Text1" placeholder="{{\'label\' | translate}}" />' +
          '    </span>' +
          '  </div>' +
          '</form>',
          footer: '<div class="modal-footer">' +
          '<button type="button" class="btn btn-default" data-dismiss="modal" translate>cancel</button>' +
          '<button type="button" class="btn btn-primary" data-ng-click="finishEdit()" translate>accept</button>' +
          '</div>'
        };

        // Keyword creation
        if(!xlink) {
          gnPopup.createModal(modalConfig, scope);
        }
        else {
          $http({
            method: 'GET',
            url: 'json.keyword.get',
            params: {
              lang: 'eng,fre,ger,roh,ita',
              id: xlink.props.uri,
              thesaurus: xlink.props.thesaurus.key
            }
          })
            .success(function (data) {
              for (var lang in scope.keyword) {
                scope.keyword[lang].label = data[lang].label;
                scope.keyword[lang].desc = data[lang].definition;
              }
              gnPopup.createModal(modalConfig, scope);
            });
        }
      };

      this.separator = '&&&';
      var self = this;

      var createNewSubtemplate = function(sharedObjectControllerScope, template, subtplCustomParams) {

        subtemplateService.createNewSubtemplate(template, false, function(){}).
        success(function(data) {
            var win = window.open('catalog.edit#/metadata/' + data.id +'/tab/simple?closeWindow=true');

            var intervalId = setInterval(function() {
              if (win.closed) {
                clearInterval(intervalId);
                var snippets = [];

                var extraXLinkParams = {
                  'xlink:show' : 'embed',
                  'xlink:role' : "http://www.geonetwork.org/non_valid_obj"
                };

                var params = angular.extend({uuid: data.uuid}, subtplCustomParams);
                self.addXlinkXml(sharedObjectControllerScope, snippets, true, extraXLinkParams,
                                 'local://subtemplate?uuid=' + data.uuid, params).
                  then( function () {
                    sharedObjectControllerScope.snippet = snippets.join(self.separator);
                    $timeout(function() {
                      // Save the metadata and refresh the form
                      gnEditor.save(gnCurrentEdit.id, true);
                    });
                  });
              }
            }, 100);
          }).
        error(function(data) {
            alert('Error creating subtemplate');
          }
        );
      };

      /**
       * Edit or create a shared object.
       * @param type
       */
      this.editEntry = function(type, xlink, sharedObjectControllerScope) {
        scope = $rootScope.$new();
        scope.xlink = xlink;

        if(!type && xlink) {
          if(xlink.indexOf('xml.extent.get') >= 0) {
            type = 'extents';
          }
        }
        scope.type = type;

        if(type == 'extents') {
           editExtent(xlink);
        } else if(type == 'keywords') {
           editKeyword(xlink);
        } else if (type === 'contacts') {
          var params = {
            process: '*//gmd:CI_RoleCode/@codeListValue~'+sharedObjectControllerScope.role.code
          };
          createNewSubtemplate(sharedObjectControllerScope, subtemplateService.contactTemplate, params);
        } else if (type === 'formats') {
          createNewSubtemplate(sharedObjectControllerScope, subtemplateService.formatTemplate, {});
        }
      };

      this.addXlinkXml = function (scope, snippets, usingXlink, extraXLinkParams, xlink, params) {
        var promise = gnHttp.callService('subtemplate', params);
        promise.success(function(xml) {
          if (usingXlink) {
            snippets.push(gnEditorXMLService.
              buildXMLForXlink(scope.elementName, xlink + '&process=' + params.process, extraXLinkParams));
          } else {
            snippets.push(gnEditorXMLService.
              buildXML(scope.elementName, xml));
          }
        });
        return promise;
      };

    }]);

  /**
   * Note: ng-model and angular checks could not be applied to
   * the editor form as it would require to init the model
   * from the form content using ng-init for example.
   */
  module.directive('gcSharedObjectUpdate', [
    'gnSearchManagerService',
    'gcSharedobject',
    function(gnSearchManagerService, gcSharedobject) {
      return {
        restrict: 'A',
        scope: {
          href: '@gcSharedObjectUpdate'
        },
        link: function(scope, element, attrs) {
          var subtemplateRegexp = new RegExp(
              "local://subtemplate\\?uuid=([^&]+).*");

            element.click(function(e) {
              e.stopPropagation();
              if (subtemplateRegexp.test(scope.href)) {
                var uuid = subtemplateRegexp.exec(scope.href)[1];
                gnSearchManagerService.gnSearch({
                  _uuid: uuid,
                  _content_type: 'json',
                  fast: 'index',
                  _isTemplate: 's'
                }).then(function(data) {
                  window.open('catalog.edit#/metadata/' +
                      data.metadata[0]['geonet:info'].id+'/tab/simple?closeWindow=true');
                });
              } else {
                gcSharedobject.editEntry(undefined, scope.href);
/*
                window.open('admin.shared.objects.edit#/edit?href=' +
                    encodeURIComponent(scope.href), '_blank');
*/
              }
            });
            element.keyup();
        }
      };
    }]);

})();
