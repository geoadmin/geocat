/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {

  goog.provide('gn_search_geocat');

  goog.require('gn_search_default');
  goog.require('gc_searchform');
  goog.require('gn_search_geocat_mdactionmenu');

  var module = angular.module('gn_search_geocat',[
    'gn_search_default',
    'gn_search_geocat_mdactionmenu',
    'gc_searchform'
  ]);

  module.config(['$LOCALES',
    function($LOCALES) {
      $LOCALES.push('geocat');

    }]);

  module.controller('gnsGeocat', [
    '$scope',
    '$controller',
    'gnSearchManagerService',
    'Metadata',
    'gnMap',
    'gnUrlUtils',
    'gnGlobalSettings',
    'gnLangs',
    function($scope, $controller, gnSearchManagerService, Metadata, gnMap,
             gnUrlUtils, gnGlobalSettings, gnLangs) {
      angular.extend(this, $controller('gnsDefault', { $scope: $scope }));
      $scope.resultTemplate = '../../catalog/views/geocat/templates/list.html';

      var callSearch = function(sortBy, to) {
        return gnSearchManagerService.gnSearch({
          sortBy: sortBy,
          fast: 'index',
          from: 1,
          to: to,
          _content_type: 'json'
        });
      };

      // Fill last updated section
      callSearch('changeDate', 15).then(function(data) {
        $scope.lastUpdated = [];
        for (var i = 0; i < data.metadata.length; i++) {
          $scope.lastUpdated.push(new Metadata(data.metadata[i]));
        }
      });

      // Override add layer to map functions
      var map = $scope.searchObj.searchMap;
      $scope.resultviewFns = {
        addMdLayerToMap: function(link) {
          // manage bad formed onlineresource where name is in the url getMap
          if(!link.name) {
            var parts = link.url.split('?');
            if(parts.length == 2) {
              var p = gnUrlUtils.parseKeyValue(parts[1]);
              link.name = p.layers;
              link.url = parts[0]
            }
          }
          if(link.name && link.url) {
            map.addLayer(gnMap.createOlWMS(map, {
              LAYERS: link.name
            },{
              url: link.url
            }));
          }
          else {
            console.warn('The layer cannot be added, missing parameters');
          }
        },
        addAllMdLayersToMap: function (layers, md) {
          angular.forEach(layers, function (layer) {
            $scope.resultviewFns.addMdLayerToMap(layer, md);
          });
        }
      };

      // save admin app url on scope
      $scope.adminAppUrl = gnGlobalSettings.gnCfg.mods.admin.appUrl.replace('{{lang}}', gnLangs.getCurrent());
    }
  ]);

  module.directive('gcFixMdlinks', [
    function() {

      return {
        restrict: 'A',
        scope: false,
        link: function(scope) {
          var links = scope.md.getLinksByType('LINK', 'CHTOPO:specialised-geoportal', "OGC:WFS", "OGC:WMTS");
          scope.links = [];
          scope.downloads = scope.md.getLinksByType('DOWNLOAD', 'FILE');

          if (angular.isDefined(scope.md.type) && scope.md.type.indexOf('service') >= 0) {
            scope.layers = [];
            if (angular.isDefined(scope.md.wmsuri) && !angular.isArray(scope.md.wmsuri)) {
              scope.md.wmsuri = [scope.md.wmsuri];
            }
            angular.forEach(scope.md.wmsuri, function(uri) {
              var e = uri.split('###');
              scope.layers.push({
                uuid: e[0],
                name: e[1],
                desc: e[1],
                url: e[2]
              });
            });
          } else {
            scope.layers = scope.md.getLinksByType('OGC:WMS', 'kml');
          }

          angular.forEach(links, function(l) {
            if(l.url && l.url != '' && l.url != '-') {
              if(l.desc == '' || l.desc == '-') {
                l.desc = l.url;
              }
              scope.links.push(l);
            }
          });

          angular.forEach(scope.downloads, function(l) {
            if(l.url && l.url != '' && l.url != '-') {
              if(l.desc == '' || l.desc == '-') {
                l.desc = l.url;
              }
            }
          });

          angular.forEach(scope.layers, function(l) {
            if(l.url && l.url != '' && l.url != '-') {
              if(l.desc == '' || l.desc == '-') {
                l.desc = l.url;
              }
            }
          });

          var d;
          if (scope.md['geonet:info'].changeDate) {
            d = {
              date: scope.md['geonet:info'].changeDate,
              type: 'changeDate'
            };
          }
          else if (scope.md['geonet:info'].publishedDate) {
            d = {
              date: scope.md['geonet:info'].publishedDate,
              type: 'changeDate'
            };
          }
          else if (scope.md['geonet:info'].createDate) {
            d = {
              date: scope.md['geonet:info'].createDate,
              type: 'createDate'
            };
          }
          scope.showDate = {
            date: moment(d).format('YYYY-MM-DD'),
            type: d.type
          };
        }
      };
    }]);

})();
