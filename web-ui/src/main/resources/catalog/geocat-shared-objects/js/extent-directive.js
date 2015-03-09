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

              // Manage form control display
              scope.prop = {
                showWKT: false,
                showBbox: true
              };

              scope.extent = [];

              featureOverlay.setMap(map);
              map.addInteraction(drawInteraction);
              map.addInteraction(dragboxInteraction);

              /**
               * Clear features on map and geometry input.
               */
              scope.clearMap = function() {
                clearMap();
                scope.extent = [];
                scope.formObj.geomString = '';
              };

              /**
               * Set geometry as text input value. I could be formated
               * to WKT or GML.
               * @geom {ol.geometry}
               */
              scope.fillInput = function() {
                var geom = featureOverlay.getFeatures().item(0).getGeometry().
                    clone().transform(map.getView().getProjection(), scope.formObj.proj);
                scope.formObj.geomString = formatWkt.writeGeometry(geom);
              };

              drawInteraction.on('drawend', function() {
                scope.extent = [];
                scope.fillInput();
                scope.$apply();
              });

              dragboxInteraction.on('boxend', function() {
                scope.$apply(function() {
                  var f = new ol.Feature();
                  var g = dragboxInteraction.getGeometry();
                  f.setGeometry(g);
                  featureOverlay.addFeature(f);

                  scope.extent = ol.proj.transformExtent(g.getExtent(),
                      map.getView().getProjection(), scope.formObj.proj);

                  scope.fillInput();
                });
              });

              scope.drawInteraction = drawInteraction;
              scope.dragboxInteraction = dragboxInteraction;

              /**
               * Update featureOverlay drawn bbox after bbox form change
               */
              scope.updateBbox = function() {

                var coordinates, geom, f, extentProj;

                clearMap();
                drawInteraction.active = false;
                dragboxInteraction.active = false;

                extentProj = ol.proj.transformExtent(scope.extent,
                    scope.formObj.proj, map.getView().getProjection());
                coordinates = gnMap.getPolygonFromExtent(extentProj);
                geom = new ol.geom.Polygon(coordinates);

                f = new ol.Feature();
                f.setGeometry(geom);
                f.getGeometry().setCoordinates(coordinates);
                featureOverlay.addFeature(f);

                map.getView().fitExtent(extentProj, map.getSize());
              };

              /**
               * On form input WKT change, update the map geometry.
               */
              scope.updateWKT = function() {
                if(scope.formObj.geomString) {
                  featureOverlay.getFeatures().clear();
                  var geom = formatWkt.readGeometry(scope.formObj.geomString).
                      transform(scope.formObj.proj,
                      map.getView().getProjection());;
                  var f = new ol.Feature();
                  f.setGeometry(geom);
                  featureOverlay.addFeature(f);
                }
              };

              /**
               * Update form bbox values depending on current porj
               */
              scope.$watch('formObj.proj', function(newV, oldV) {
                if(newV && oldV) {
                  scope.extent = ol.proj.transformExtent(scope.extent,
                  oldV, newV);
                }
              });
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
                  scope.extent = geom.getExtent();
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
