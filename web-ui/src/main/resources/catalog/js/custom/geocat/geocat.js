(function() {

  goog.provide('gn_search_geocat');

  goog.require('gn_search');
  goog.require('gn_search_geocat_config');
  goog.require('gn_selection_directive');

  var module = angular.module('gn_search_geocat', [
    'gn_search',
    'gn_search_geocat_config',
    'gn_selection_directive'
  ]);

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
      '$timeout',
      'gnMap',
      'gnSearchSettings',
    function($scope, $timeout, gnMap, gnSearchSettings) {

      angular.extend($scope.searchObj, {
        advancedMode: false,
        searchMap: gnSearchSettings.searchMap
      });

      $scope.collapsed = false;

      $scope.searchExpanded = false;
      $scope.toggleSearch = function() {
        $scope.searchExpanded = !$scope.searchExpanded;
        $timeout(function(){
          gnSearchSettings.searchMap.updateSize();
        }, 300);
      };

      $scope.searchMapExpanded = true;
      $scope.toggleSearchMap = function() {
        $scope.searchMapExpanded = !$scope.searchMapExpanded;
        $timeout(function(){
          gnSearchSettings.searchMap.updateSize();
        }, 1);
      };

      $scope.resultviewFns = {
        addMdLayerToMap: function (link) {
          gnMap.addWmsToMap(gnSearchSettings.searchMap,{
                LAYERS:link.name
              },{
                url: link.url
              });
        }
      };

      // To access CatController $scope (gnsGeocat > GnSearchController > GnCatController)
      $scope.$parent.$parent.langs = {'fre': 'fr', 'eng': 'en', 'ger': 'ge', 'ita': 'it'};


      $('#anySearchField').focus();
    }]);

  module.controller('gnsGeocatHome', [
    '$scope',
    'gnSearchManagerService',
    function($scope, gnSearchManagerService) {

      $scope.mdLists = {};
      var callSearch = function(sortBy, to) {
        return gnSearchManagerService.gnSearch({
          sortBy: sortBy,
          fast: 'index',
          from: 1,
          to: to
        });
      };

      // Fill last updated section
      callSearch('changeDate', 15).then(function(data) {
        $scope.lastUpdated = data.metadata;
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
    'gnSearchManagerService',
    'ngeoDecorateInteraction',
    '$q',

    function($scope, gnHttp, gnHttpServices, gnRegionService,
             $timeout, suggestService,$http, gnSearchSettings,
             gnSearchManagerService, ngeoDecorateInteraction, $q) {


      // data store for types field
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

      // data store for archives field
      $scope.archives = [{
        value: '',
        label: 'archiveincluded'
      }, {
        value: 'n',
        label: 'archiveexcluded'
      },{
        value: 'y',
        label: 'archiveonly'
      }];

      // data store for valid field
      $scope.validStore = [{
        value: '',
        label: 'anyValue'
      }, {
        value: 'y',
        label: 'yes'
      }, {
        value: 'n',
        label: 'no'
      },{
        value: '-1',
        label: 'unchecked'
      }];

      // data store for topic category
      var topicCats = gnSearchSettings.gnStores.topicCat;
      angular.forEach(topicCats, function(cat, i) {
        topicCats[i] = {
          id: cat[0],
          name: cat[1],
          hierarchy: cat[0].indexOf('_') > 0 ? 'second' : 'main'
        }
      });
      $scope.topicCatsOptions= {
        mode: 'local',
        data: topicCats,
        config: {
          templates: {
            suggestion: Handlebars.compile('<p class="topiccat-{{hierarchy}}">{{name}}</p>')
          }
        }
      };

      // data store for formats
      $scope.formatsOptions= {
        mode: 'local',
        data: topicCats //TODO
      };

      // config for sources option (sources and groups)
      $scope.sourcesOptions = {
        mode: 'prefetch',
        promise: (function(){
          var defer = $q.defer();
          $http.get(suggestService.getInfoUrl('sources', 'groups')).success(function(data) {
            var res = [];
            var parseBlock = function(block) {
              var a = data[block];
              for(var i=0; i<a.length;i++) {
                res.push({
                  id: a[i]['@id'],
                  name : a[i].name
                });
              }
            };
            parseBlock('sources');
            parseBlock('group');

            defer.resolve(res);
          });
          return defer.promise;
        })()
      };


      var map = $scope.searchObj.searchMap;
      var wktFormat = new ol.format.WKT();

      // Set the geometry field of the indexed search in WKT
      // draw polygon or bbox set the field (not AU ones).
      var setSearchGeometry = function(geometry) {
        $scope.searchObj.params.geometry = wktFormat.writeGeometry(
          geometry.clone().transform(map.getView().getProjection(), 'EPSG:4326')
        );
      };

      /** Manage draw area on search map */
      var featureOverlay = new ol.FeatureOverlay({
        style: gnSearchSettings.olStyles.drawBbox
      });
      featureOverlay.setMap(map);

      var cleanDraw = function() {
        featureOverlay.getFeatures().clear();
        drawInteraction.active = false;
        dragboxInteraction.active = false;
      };

      var drawInteraction = new ol.interaction.Draw({
        features: featureOverlay.getFeatures(),
        type: 'Polygon',
        style: gnSearchSettings.olStyles.drawBbox
      });
      drawInteraction.on('drawend', function(){
        setSearchGeometry(featureOverlay.getFeatures().item(0).getGeometry());
        setTimeout(function() {
          drawInteraction.active = false;
        }, 0);
      });
      drawInteraction.on('drawstart', function(){
        featureOverlay.getFeatures().clear();
      });
      ngeoDecorateInteraction(drawInteraction, map);

      var dragboxInteraction = new ol.interaction.DragBox({
        style: gnSearchSettings.olStyles.drawBbox
      });
      dragboxInteraction.on('boxend', function(){
        var f = new ol.Feature();
        var g = dragboxInteraction.getGeometry();

        f.setGeometry(g);
        setSearchGeometry(g);
        featureOverlay.addFeature(f);
        setTimeout(function() {
          dragboxInteraction.active = false;
        }, 0);
      });
      dragboxInteraction.on('drawstart', function(){
        featureOverlay.getFeatures().clear();
      });
      ngeoDecorateInteraction(dragboxInteraction, map);

      /**
       * On refresh 'Draw on Map' click
       * Clear the drawing and activate the drawing Interaction again.
       */
      $scope.refreshDraw = function(e) {
        if($scope.restrictArea == 'draw') {
          featureOverlay.getFeatures().clear();
          drawInteraction.active = true;
          e.stopPropagation();
        }
      };
      $scope.refreshDrag = function(e) {
        if($scope.restrictArea == 'bbox') {
          featureOverlay.getFeatures().clear();
          dragboxInteraction.active = true;
          e.stopPropagation();
        }
      };

      $scope.$watch('restrictArea', function(v){
        $scope.searchObj.params.geometry = '';
        $scope.searchObj.params.countries = '';
        $scope.searchObj.params.cantons = '';
        $scope.searchObj.params.cities = '';

        if(angular.isDefined(v)) {
          if($scope.restrictArea == 'draw') {
            drawInteraction.active = true;
          }
          else if($scope.restrictArea == 'bbox') {
            dragboxInteraction.active = true;
          }
          else {
            cleanDraw();
          }
        }
      });

      // Remove geometry on map if geometry field is reset from url or from model
      $scope.$watch('searchObj.params.geometry', function(v) {
        if(!v || v =='') {
          featureOverlay.getFeatures().clear();
        }
      });

        /** When we switch between simple and advanced form*/
      $scope.$watch('advanced', function(v){
        if(v == false) {
          $scope.restrictArea = '';
        }
      });

      /** Manage cantons selection (add feature to the map) */
      var cantonSource = new ol.source.Vector();
      var nbCantons = 0;
      var cantonVector = new ol.layer.Vector({
        source: cantonSource,
        style: gnSearchSettings.olStyles.drawBbox
      });

      var getRegionOptions = function(type) {
        return {
          mode: 'prefetch',
          promise: gnRegionService.loadRegion({id:type}, 'fre')
        };
      };
      $scope.regionOptions = {
        kantone: getRegionOptions('kantone'),
        country: getRegionOptions('country'),
        gemeinden: getRegionOptions('gemeinden')
      };

      var addCantonFeature = function(id) {
        nbCantons++;

        return gnHttp.callService('regionWkt', {
          id: id,
          srs: 'EPSG:21781'
        }).success(function(wkt) {
          var parser = new ol.format.WKT();
          var feature = parser.readFeature(wkt);
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

      $scope.$watch('searchObj.params.countries', onRegionSelect);
      $scope.$watch('searchObj.params.cantons', onRegionSelect);
      $scope.$watch('searchObj.params.cities', onRegionSelect);


      $scope.searchObj.params.relation = 'within';

      $scope.scrollToBottom = function($event) {
        var elem = $($event.target).parents('.panel-body')[0];
        setTimeout(function() {
          elem.scrollTop = elem.scrollHeight;
        }, 0);
      };

      if ($scope.initial) {
        var url = 'qi@json?summaryOnly=true';
        gnSearchManagerService.search(url).then(function(data) {
          $scope.searchResults.facet = data.facet;
        });
      } else {
        $scope.triggerSearch(true);
      }

/*
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
*/


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
