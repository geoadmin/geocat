(function() {
  goog.provide('shared-object-subtemplate-service');
  'use strict';

  var module = angular.module('shared-object-subtemplate-service', []).
    factory('subtemplateService', ['$http',
      function ($http) {
        var transformToFormUrlEncoded = function(obj) {
          var str = [];
          for(var p in obj) {
            if (obj.hasOwnProperty(p))
              str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
          }
          return str.join("&");
        };

        return {
          transformToFormUrlEncoded: transformToFormUrlEncoded,
          createNewSubtemplate: function(template, validated, before) {
            before();
            var data = {
              insert_mode:0,
              template: 's',
              fullPrivileges: 'y',
              data: template,
              group: 0,
              extra: validated ? 'validated' : 'nonvalidated',
              schema: 'iso19139.che'
            };
            return $http({
              method: 'POST',
              url: 'md.insert?_content_type=json',
              headers: {'Content-Type': 'application/x-www-form-urlencoded'},
              transformRequest: transformToFormUrlEncoded,
              data: data
            })
          },
          contactTemplate: '<che:CHE_CI_ResponsibleParty xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" gco:isoType="gmd:CI_ResponsibleParty">\n' +
          '   <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">\n' +
          '      <gmd:PT_FreeText>\n' +
          '         <gmd:textGroup>\n' +
          '            <gmd:LocalisedCharacterString locale="#DE">~~ Template First Name ~~</gmd:LocalisedCharacterString>\n' +
          '         </gmd:textGroup>\n' +
          '         <gmd:textGroup>\n' +
          '            <gmd:LocalisedCharacterString locale="#FR">~~ Template First Name ~~</gmd:LocalisedCharacterString>\n' +
          '         </gmd:textGroup>\n' +
          '         <gmd:textGroup>\n' +
          '            <gmd:LocalisedCharacterString locale="#IT">~~ Template First Name ~~</gmd:LocalisedCharacterString>\n' +
          '         </gmd:textGroup>\n' +
          '         <gmd:textGroup>\n' +
          '            <gmd:LocalisedCharacterString locale="#EN">~~ Template First Name ~~</gmd:LocalisedCharacterString>\n' +
          '         </gmd:textGroup>\n' +
          '      </gmd:PT_FreeText>\n' +
          '   </gmd:organisationName>\n' +
          '   <gmd:contactInfo>\n' +
          '      <gmd:CI_Contact>\n' +
          '         <gmd:address>\n' +
          '            <che:CHE_CI_Address gco:isoType="gmd:CI_Address">\n' +
          '               <gmd:electronicMailAddress>\n' +
          '                  <gco:CharacterString>~~ Template Email ~~</gco:CharacterString>\n' +
          '               </gmd:electronicMailAddress>\n' +
          '            </che:CHE_CI_Address>\n' +
          '         </gmd:address>\n' +
          '      </gmd:CI_Contact>\n' +
          '   </gmd:contactInfo>\n' +
          '   <gmd:role>\n' +
          '      <gmd:CI_RoleCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact" />\n' +
          '   </gmd:role>\n' +
          '   <che:individualFirstName>\n' +
          '      <gco:CharacterString>~~ Template First Name ~~</gco:CharacterString>\n' +
          '   </che:individualFirstName>\n' +
          '   <che:individualLastName>\n' +
          '      <gco:CharacterString>~~ Template Last Name ~~</gco:CharacterString>\n' +
          '   </che:individualLastName>\n' +
          '</che:CHE_CI_ResponsibleParty>',
          formatTemplate: '<gmd:MD_Format xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco">' +
          '  <gmd:name>' +
          '    <gco:CharacterString >~~ Template Name ~~</gco:CharacterString>' +
          '  </gmd:name>' +
          '  <gmd:version>' +
          '    <gco:CharacterString>~~ Template Version ~~</gco:CharacterString>' +
          '  </gmd:version>' +
          '</gmd:MD_Format>'
        };
      }]);
})();
