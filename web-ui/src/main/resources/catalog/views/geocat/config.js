(function() {

  goog.provide('gn_search_geocat_config');

  var module = angular.module('gn_search_geocat_config', []);

  module.run(['gnSearchSettings',

    function(searchSettings) {

      proj4.defs('EPSG:21781', '+proj=somerc +lat_0=46.95240555555556' +
          ' +lon_0=7.439583333333333 +k_0=1 +x_0=600000' +
          ' +y_0=200000 +ellps=bessel +towgs84=660.077,13.551,' +
          '369.344,2.484,1.783,2.939,5.66 +units=m +no_defs');

      ol.proj.get('EPSG:21781').setExtent([420000, 30000, 900000, 350000]);
      ol.proj.get('EPSG:21781').setWorldExtent([ 5.5, 45.5, 10.6, 48 ]);

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
            color: 'black',
            width: 2
          })
        }),
        mdExtentHighlight: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'black',
            width: 3
          }),
          fill: new ol.style.Fill({
            color: 'rgba(0,0,0,0.3)'
          })
        })
      };

      var projection = ol.proj.get('EPSG:3857');
      var projectionExtent = projection.getExtent();
      var size = ol.extent.getWidth(projectionExtent) / 256;
      var resolutions = new Array(14);
      var matrixIds = new Array(14);
      for (var z = 0; z < 14; ++z) {
        // generate resolutions and matrixIds arrays for this WMTS
        resolutions[z] = size / Math.pow(2, z);
        matrixIds[z] = z;
      }

      var defaultUrl = 'https://server.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer/WMTS/tile/1.0.0/World_Topo_Map/default/default028mm/{TileMatrix}/{TileRow}/{TileCol}.jpg';

      var url = angular.isDefined(searchSettings.mapConfig) ?
          searchSettings.mapConfig.url || defaultUrl : defaultUrl;

      var chLayer = new ol.layer.Tile({
        source: new ol.source.WMTS({
          url: defaultUrl,
          tileGrid: new ol.tilegrid.WMTS({
            origin: ol.extent.getTopLeft(projectionExtent),
            resolutions: resolutions,
            matrixIds: matrixIds
          }),
          requestEncoding: 'REST',
          wrapX: true
        })
      });

      var mousePositionControl = new ol.control.MousePosition({
        coordinateFormat: ol.coordinate.createStringXY(4),
        projection: 'EPSG:3857',
        // comment the following two lines to have the mouse position
        // be placed within the map.
        className: 'custom-mouse-position',
        target: document.getElementById('mouse-position'),
        undefinedHTML: '&nbsp;'
      });

      searchSettings.searchMap = new ol.Map({
        controls: ol.control.defaults({
          attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
            collapsible: false
          })
        }).extend([mousePositionControl]),
        layers: [chLayer],
        view: new ol.View({
          center: [929317, 5909466],
          zoom: 7
        })
      });

      /** Facets configuration */
      searchSettings.facetsSummaryType = 'hits';

      /* Hits per page combo values configuration */
      searchSettings.hitsperpageValues = [3, 10, 20, 50, 100];

      /* Pagination configuration */
      searchSettings.paginationInfo = {
        hitsPerPage: searchSettings.hitsperpageValues[1]
      };

      /* Sort by combo values configuration */
      searchSettings.sortbyValues = [
        {sortBy: 'relevance', sortOrder: ''},
        {sortBy: 'title', sortOrder: 'reverse'},
        {sortBy: 'rating', sortOrder: ''},
        {sortBy: 'changeDate', sortOrder: ''}];
      searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

      /* Custom templates for search result views */
      searchSettings.resultTemplate = '../../catalog/components/search/' +
          'resultsview/partials/viewtemplates/geocat.html';

      searchSettings.formatter = {
        defaultUrl: 'md.format.xml?xsl=full_view&id='
      };

    }]);
})();
