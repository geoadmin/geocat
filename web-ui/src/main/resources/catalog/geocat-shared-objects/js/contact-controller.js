(function() {
  goog.provide('geocat_shared_objects_contact_controller');
  goog.require('geocat_shared_objects_translate_config');
  'use strict';

  var module = angular.module('geocat_shared_objects_contact_controller', []).
    controller('ContactControl',  ['$scope', '$routeParams', '$location', 'commonProperties', '$http',
      function ($scope, $routeParams, $location, commonProperties, $http) {
        var template = '<che:CHE_CI_ResponsibleParty xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" gco:isoType="gmd:CI_ResponsibleParty">\n' +
'   <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">\n' +
'      <gmd:PT_FreeText>\n' +
'         <gmd:textGroup>\n' +
'            <gmd:LocalisedCharacterString locale="#DE">-- Template First Name --</gmd:LocalisedCharacterString>\n' +
'         </gmd:textGroup>\n' +
'         <gmd:textGroup>\n' +
'            <gmd:LocalisedCharacterString locale="#FR">-- Template First Name --</gmd:LocalisedCharacterString>\n' +
'         </gmd:textGroup>\n' +
'         <gmd:textGroup>\n' +
'            <gmd:LocalisedCharacterString locale="#IT">-- Template First Name --</gmd:LocalisedCharacterString>\n' +
'         </gmd:textGroup>\n' +
'         <gmd:textGroup>\n' +
'            <gmd:LocalisedCharacterString locale="#EN">-- Template First Name --</gmd:LocalisedCharacterString>\n' +
'         </gmd:textGroup>\n' +
'      </gmd:PT_FreeText>\n' +
'   </gmd:organisationName>\n' +
'   <gmd:contactInfo>\n' +
'      <gmd:CI_Contact>\n' +
'         <gmd:phone>\n' +
'            <che:CHE_CI_Telephone gco:isoType="gmd:CI_Telephone">\n' +
'               <che:mobile>\n' +
'                  <gco:CharacterString>-- Template Mobile Number --</gco:CharacterString>\n' +
'               </che:mobile>\n' +
'            </che:CHE_CI_Telephone>\n' +
'         </gmd:phone>\n' +
'         <gmd:address>\n' +
'            <che:CHE_CI_Address gco:isoType="gmd:CI_Address">\n' +
'               <gmd:electronicMailAddress>\n' +
'                  <gco:CharacterString>-- Template Email --</gco:CharacterString>\n' +
'               </gmd:electronicMailAddress>\n' +
'            </che:CHE_CI_Address>\n' +
'         </gmd:address>\n' +
'      </gmd:CI_Contact>\n' +
'   </gmd:contactInfo>\n' +
'   <gmd:role>\n' +
'      <gmd:CI_RoleCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact" />\n' +
'   </gmd:role>\n' +
'   <che:individualFirstName>\n' +
'      <gco:CharacterString>-- Template First Name --</gco:CharacterString>\n' +
'   </che:individualFirstName>\n' +
'   <che:individualLastName>\n' +
'      <gco:CharacterString>-- Template Last Name --</gco:CharacterString>\n' +
'   </che:individualLastName>\n' +
'</che:CHE_CI_ResponsibleParty>';
        commonProperties.addValidated($scope, $routeParams);
        commonProperties.add($scope, $routeParams);
        $scope.edit = function (row) {
          $scope.reloadOnWindowClosed($scope.open(row.url));
        };
        if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_contact';
        } else {
          $scope.luceneIndexField = 'V_valid_xlink_contact';
        }
        $scope.startCreateNew = function () {
          $scope.createNewSubtemplate(template, "/validated/contacts");
        };
        $scope.includeRowPartial = 'row-formless.html';
      }]);
})();
