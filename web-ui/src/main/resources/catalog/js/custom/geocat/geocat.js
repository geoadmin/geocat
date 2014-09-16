(function() {

  goog.provide('gn_search_geocat');

  goog.require('gn_search');
  goog.require('gn_search_geocat_config');

  var module = angular.module('gn_search_geocat', ['gn_search', 'gn_search_geocat_config']);

  /**
   * @ngdoc controller
   * @name gn_search_geocat.controller:gnsGeocat
   *
   * @description
   * Geocat view root controller
   * its $scope inherits from gnSearchController scope.
   *
   */
  module.controller('gnsGeocat', [
      '$scope',
      'gnSearchSettings',
    function($scope, gnSearchSettings) {

      angular.extend($scope.searchObj, {
        advancedMode: false,
        searchMap: gnSearchSettings.searchMap
      });
    }]);

    module.controller('gocatSearchFormCtrl', [
    '$scope',
    'gnHttp',
    'gnHttpServices',
    'gnRegionService',
    '$timeout',
    'suggestService',
    '$http',
    'gnSearchSettings',

    function($scope, gnHttp, gnHttpServices, gnRegionService,
             $timeout, suggestService,$http, gnSearchSettings) {

      $scope.types = ['any',
        'dataset',
        'basicgeodata',
        'basicgeodata-federal',
        'basicgeodata-cantonal',
        'basicgeodata-communal',
        'service',
        'service-OGC:WMS',
        'service-OGC:WFS'
      ];

      var map = $scope.searchObj.searchMap;

      gnRegionService.loadRegion('ocean', 'fre').then(
          function (data) {
            $scope.cantons = data;
          });

      /** Manage cantons selection (add feature to the map) */
      var cantonSource = new ol.source.Vector();
      var nbCantons = 0;
      var cantonVector = new ol.layer.Vector({
        source: cantonSource,
        style: gnSearchSettings.olStyles.drawBbox
      });
      var addCantonFeature = function(id) {
        nbCantons++;

        return gnHttp.callService('regionWkt', {
          id: id,
          srs: 'EPSG:21781'
        }).success(function(wkt) {
          var parser = new ol.format.WKT();
          var feature = parser.readFeature(wkt);
          feature.setGeometry(feature.getGeometry().transform('EPSG:21781', 'EPSG:3857'));
          cantonSource.addFeature(feature);
        });
      };
      map.addLayer(cantonVector);

      // Request cantons geometry and zoom to extent when
      // all requests respond.
      var onRegionSelect = function(v){
        cantonSource.clear();
        if(angular.isDefined(v) && v != '') {
          var cs = v.split(',');
          for(var i=0; i<cs.length;i++) {
            addCantonFeature(cs[i]).then(function(){
              if(--nbCantons == 0) {
                map.getView().fitExtent(cantonSource.getExtent(), map.getSize());
              }
            });
          }
        }
      };

      $scope.$watch('searchObj.params.cantons', onRegionSelect);
      $scope.$watch('searchObj.params.cities', onRegionSelect);


      $('#categoriesF').tagsinput({
        itemValue: 'id',
        itemText: 'label'
      });
      $('#categoriesF').tagsinput('input').typeahead({
        valueKey: 'label',
        prefetch: {
          url :suggestService.getInfoUrl('categories'),
          filter: function(data) {
            var res = [];
            for(var i=0; i<data.metadatacategory.length;i++) {
              res.push({
                id: data.metadatacategory[i]['@id'],
                label : data.metadatacategory[i].label.eng
              })
            }
            return res;
          }
        }
      }).bind('typeahead:selected', $.proxy(function (obj, datum) {
        this.tagsinput('add', datum);
        this.tagsinput('input').typeahead('setQuery', '');
      }, $('#categoriesF')));

      gnHttpServices.geocatKeywords = 'geocat.keywords.list';
      $('#keywordsF').tagsinput({
        itemValue: 'id',
        itemText: 'label'
      });
      $('#keywordsF').tagsinput('input').typeahead({
        valueKey: 'label',
        prefetch: {
          url :gnHttpServices.info,
          filter: function(data) {
            var res = [];
            for(var i=0; i<data.metadatacategory.length;i++) {
              res.push({
                id: data.metadatacategory[i]['@id'],
                label : data.metadatacategory[i].label.eng
              })
            }
            return res;
          }
        }
      }).bind('typeahead:selected', $.proxy(function (obj, datum) {
        this.tagsinput('add', datum);
        this.tagsinput('input').typeahead('setQuery', '');
      }, $('#keywordsF')));


      // Keywords input list
      /*
       gnHttpServices.geocatKeywords = 'geocat.keywords.list';
       gnHttp.callService('geocatKeywords').success(function(data) {
       var xmlDoc = $.parseXML(data);
       var $xml = $(xmlDoc);
       var k = $xml.find('keyword');
       var n = $xml.find('name');
       });
       */

    }]);

})();
