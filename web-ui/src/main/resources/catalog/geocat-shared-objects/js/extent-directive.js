(function() {

  goog.provide('geocat_shared_objects_extent_directive');

  goog.require('gn_search_geocat_config');
  goog.require('gn_map_service');
  goog.require('gn_multilingual_field_directive');

  var module = angular.module('geocat_shared_objects_extent_directive', [
    'gn_search_geocat_config',
    'gn_map_service',
    'gn_multilingual_field_directive'
  ]);

  module.directive('gcEditExtent', [
    'gnMap',
    'gnSearchSettings',
    'ngeoDecorateInteraction',
    function(gnMap, gnSearchSettings, goDecoI) {

      // Create overlay to draw bbox and polygon
      var featureOverlay = new ol.FeatureOverlay({
        style: gnSearchSettings.olStyles.drawBbox
      });

      // create draw and bbox interaction
      var drawInteraction = new ol.interaction.Draw({
        features: featureOverlay.getFeatures(),
        type: 'Polygon',
        style: gnSearchSettings.olStyles.drawBbox
      });

      var dragboxInteraction = new ol.interaction.DragBox({
        style: gnSearchSettings.olStyles.drawBbox
      });

      var clearMap = function() {
        featureOverlay.getFeatures().clear();
      };
      drawInteraction.on('drawstart', clearMap);
      dragboxInteraction.on('boxstart', clearMap);

      goDecoI(drawInteraction);
      drawInteraction.active = false;

      goDecoI(dragboxInteraction);
      dragboxInteraction.active = false;

      var formatWkt = new ol.format.WKT();

      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/geocat-shared-objects/partials/' +
            'extent.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {

              scope.map = gnSearchSettings.searchMap;
              var map = scope.map;
              var drawnGeom;

              featureOverlay.setMap(map);
              map.addInteraction(drawInteraction);
              map.addInteraction(dragboxInteraction);

              /**
               * Clear features on map and geometry input.
               */
              scope.clearMap = function() {
                clearMap();
                scope.formObj.geomString = '';
              };

              /**
               * Set geometry as text in put value. I could be formated
               * to WKT or GML.
               * @geom {ol.geometry}
               */
              scope.fillInput = function() {
                var geom = featureOverlay.getFeatures().item(0).getGeometry().
                    clone().transform(map.getView().getProjection(), scope.formObj.proj);
                scope.formObj.geomString = formatWkt.writeGeometry(geom);
              };

              drawInteraction.on('drawend', function() {
                scope.fillInput();
                scope.$apply();
              });

              dragboxInteraction.on('boxend', function() {
                scope.$apply(function() {
                  var f = new ol.Feature();
                  var g = dragboxInteraction.getGeometry();
                  f.setGeometry(g);
                  featureOverlay.addFeature(f);
                  scope.fillInput();
                });
              });

              scope.drawInteraction = drawInteraction;
              scope.dragboxInteraction = dragboxInteraction;
            },

            post: function postLink(scope) {

              /**
               * Clear form, all fields and map.
               */
              var initForm = function(ft) {
                scope.clearMap();
                scope.formObj.proj = 'EPSG:21781';
                scope.formObj.geoId = scope.formObj.desc = {
                  DE: '',EN: '', FR: '',  IT: '', RM: ''};
                if(ft) {
                  scope.formObj.geoId = ft.feature.geoId;
                  scope.formObj.desc = ft.feature.desc;
                }
              };


              scope.$on('modalShown', function(e, featureType) {

                initForm(featureType);

                // If we load an existing shared object we load the geom
                if(featureType) {
                  var geom = formatWkt.readGeometry(featureType.feature.geom);
                  var f = new ol.Feature();
                  f.setGeometry(geom);
                  featureOverlay.addFeature(f);
                  scope.fillInput();
                }

                setTimeout(function () {
                  scope.map.updateSize();
                }, 200);
              });
            }
          };
        }
      }
    }]);
})();
