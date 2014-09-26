(function() {

  goog.provide('gn_search_geocat_config');

  var module = angular.module('gn_search_geocat_config', []);

  module.config(['gnSearchSettings',

    function(searchSettings) {

      proj4.defs("EPSG:21781","+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=660.077,13.551,369.344,2.484,1.783,2.939,5.66 +units=m +no_defs");

      searchSettings.olStyles = {
        drawBbox: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'rgba(255,0,0,1)',
            width: 2
          }),
          fill: new ol.style.Fill({
            color: 'rgba(255,0,0,0.3)'
          })
        }),
        mdExtent: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'orange',
            width: 2
          })
        }),
        mdExtentHighlight:new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'orange',
            width: 3
          }),
          fill: new ol.style.Fill({
            color: 'rgba(255,255,0,0.3)'
          })
        })
      };

      searchSettings.searchMap = new ol.Map({
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ],
        view: new ol.View({
          center: [280274.03240585705, 6053178.654789996],
          zoom: 0
        })
      });

      /** Facets configuration */
      searchSettings.facetsConfig = {
        keyword: 'keywords',
        orgName: 'orgNames',
        denominator: 'denominator',
        format: 'formats',
        createDateYear: 'createDateYears'
      };

      /* Pagination configuration */
      searchSettings.paginationInfo = {
        hitsPerPage: 3
      };

      /* Custom templates for search result views */
      searchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/title.html',
        tooltip: 'Simple',
        icon: 'fa-list'
      }, {
        tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/geocat.html',
        tooltip: 'Geocat',
        icon: 'fa-th-list'
      }];
    }]);
})();
