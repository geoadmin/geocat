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
          formatTemplate:  '<gmd:MD_Format xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco">' +
          '  <gmd:name>' +
          '    <gco:CharacterString >-- Template Name --</gco:CharacterString>' +
          '  </gmd:name>' +
          '  <gmd:version>' +
          '    <gco:CharacterString>-- Template Version --</gco:CharacterString>' +
          '  </gmd:version>' +
          '</gmd:MD_Format>',
          contactTemplate: '<che:CHE_CI_ResponsibleParty xmlns:che="http://www.geocat.ch/2008/che"' +
          '                             xmlns:gco="http://www.isotc211.org/2005/gco"' +
          '                             xmlns:gmd="http://www.isotc211.org/2005/gmd"' +
          '                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' +
          '                             xmlns:geonet="http://www.fao.org/geonetwork"' +
          '                             gco:isoType="gmd:CI_ResponsibleParty">' +
          '   <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">' +
          '      <gmd:PT_FreeText>' +
          '         <gmd:textGroup>' +
          '            <gmd:LocalisedCharacterString locale="#DE">~~ gmd:organizationName ~~</gmd:LocalisedCharacterString>' +
          '         </gmd:textGroup>' +
          '         <gmd:textGroup>' +
          '            <gmd:LocalisedCharacterString locale="#FR">  </gmd:LocalisedCharacterString>' +
          '         </gmd:textGroup>' +
          '         <gmd:textGroup>' +
          '            <gmd:LocalisedCharacterString locale="#IT">  </gmd:LocalisedCharacterString>' +
          '         </gmd:textGroup>' +
          '         <gmd:textGroup>' +
          '            <gmd:LocalisedCharacterString locale="#EN">  </gmd:LocalisedCharacterString>' +
          '         </gmd:textGroup>' +
          '      </gmd:PT_FreeText>' +
          '   </gmd:organisationName>' +
          '   <gmd:contactInfo>' +
          '      <gmd:CI_Contact>' +
          '         <gmd:phone>' +
          '            <che:CHE_CI_Telephone gco:isoType="gmd:CI_Telephone">' +
          '               <gmd:voice>' +
          '                  <gco:CharacterString/>' +
          '               </gmd:voice>' +
          '            </che:CHE_CI_Telephone>' +
          '         </gmd:phone>' +
          '         <gmd:address>' +
          '            <che:CHE_CI_Address gco:isoType="gmd:CI_Address">' +
          '               <gmd:city>' +
          '                  <gco:CharacterString/>' +
          '               </gmd:city>' +
          '               <gmd:postalCode>' +
          '                 <gco:CharacterString/>' +
          '               </gmd:postalCode>' +
          '               <gmd:electronicMailAddress>' +
          '                  <gco:CharacterString></gco:CharacterString>' +
          '               </gmd:electronicMailAddress>' +
          '               <che:streetName>' +
          '                  <gco:CharacterString/>' +
          '               </che:streetName>' +
          '               <che:streetNumber>' +
          '                  <gco:CharacterString/>' +
          '               </che:streetNumber>' +
          '            </che:CHE_CI_Address>' +
          '         </gmd:address>' +
          '      </gmd:CI_Contact>' +
          '  </gmd:contactInfo>' +
          '   <gmd:role>' +
          '      <gmd:CI_RoleCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode"' +
          '                       codeListValue="pointOfContact"/>' +
          '  </gmd:role>' +
          '   <che:individualFirstName>' +
          '      <gco:CharacterString></gco:CharacterString>' +
          '  </che:individualFirstName>' +
          '   <che:individualLastName>' +
          '      <gco:CharacterString></gco:CharacterString>' +
          '  </che:individualLastName>' +
          '</che:CHE_CI_ResponsibleParty>'
        };
      }]);
})();
