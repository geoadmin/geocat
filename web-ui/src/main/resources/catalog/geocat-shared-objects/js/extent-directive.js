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
    'gnGlobalSettings',
    'ngeoDecorateInteraction',
    function(gnMap, gnSearchSettings, gnGlobalSettings, goDecoI) {

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

              // Manage form control display
              scope.prop = {
                showWKT: false,
                showBbox: true
              };
              
              scope.gnGlobalSettings = gnGlobalSettings;
              scope.extent = [];
              scope.isExtent = true;

              scope.featureOverlay = featureOverlay;

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
                scope.isExtent = true;
              };

              /**
               * Set geometry as text input value. It could be formated
               * to WKT or GML.
               * @geom {ol.geometry}
               */
              scope.fillInput = function() {
                scope.formObj.geomString = formatWkt.writeGeometry(
                  ol.geom.Polygon.fromExtent(scope.extent)
                );
              };

              drawInteraction.on('drawend', function(e) {
                scope.isExtent = false;
                scope.incompatible = false;

                scope.$apply(function () {
                  var g = e.feature.getGeometry();

                  // Re-project new geometry to selected projection
                  var g_project_crs = g.clone().transform(map.getView().getProjection(), scope.formObj.proj);

                  // Update WKT field
                  scope.formObj.geomString = formatWkt.writeGeometry(g_project_crs);

                  // Update extent fields
                  scope.extent = g_project_crs.getExtent();
                });
              });

              dragboxInteraction.on('boxend', function() {
                scope.isExtent = true;
                scope.incompatible = false;
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

                var f, extentProj;
                var mapProj = map.getView().getProjection();

                clearMap();
                drawInteraction.active = false;
                dragboxInteraction.active = false;
                f = new ol.Feature();


                extentProj = ol.proj.transformExtent(scope.extent,
                    scope.formObj.proj, map.getView().getProjection());

                scope.incompatible = mapProj != scope.formObj.proj &&
                  !ol.extent.containsExtent(mapProj.getExtent(), extentProj);


                if (!scope.incompatible) {
                  f.setGeometry(new ol.geom.Polygon(
                    gnMap.getPolygonFromExtent(extentProj)
                  ));

                  featureOverlay.addFeature(f);
                  map.getView().fitExtent(extentProj, map.getSize());
                }

                scope.fillInput();

              };

              /**
               * On form input WKT change, update the map geometry.
               */
              scope.updateWKT = function() {
                if(scope.formObj.geomString) {
                  featureOverlay.getFeatures().clear();
                  var geom = formatWkt.readGeometry(scope.formObj.geomString).
                      transform(scope.formObj.proj,
                      map.getView().getProjection());
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
                  scope.extent = ol.proj.transformExtent(scope.extent, oldV, newV);
                  scope.formObj.geomString = (new ol.format.WKT()).writeGeometry(
                    ol.geom.Polygon.fromExtent(scope.extent)
                  );
                }
              });
            },

            post: function postLink(scope) {

              /**
               * Clear form, all fields and map.
               */
              var initForm = function(ft) {
                scope.clearMap();
                scope.formObj.proj = gnGlobalSettings.srs;
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

                  scope.extent = geom.getExtent();
                  scope.formObj.geomString = formatWkt.writeGeometry(geom);

                  // Reproject geometry to map projection
                  f.setGeometry(geom.transform(gnGlobalSettings.srs, scope.map.getView().getProjection()));
                  featureOverlay.addFeature(f);
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
