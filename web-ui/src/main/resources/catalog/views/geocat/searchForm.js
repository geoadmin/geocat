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

  goog.provide('gc_searchform');

  var module = angular.module('gc_searchform',[
  ]);


  var SearchFormController = function(
    $http,
    $q,
    suggestService,
    gnLangs,
    gnSearchSettings) {

    // Catalog search tagsinput values (groups)
    this.catalogOptions = {
      mode: 'prefetch',
      promise: (function() {
        var defer = $q.defer();
        $http.get('../api/groups', {cache: true}).
        success(function(a) {
          var res = [];
          for (var i = 0; i < a.length; i++) {
            res.push({
              id: a[i].id,
              name: a[i].label[gnLangs.current] || a[i].name
            });
          }
          defer.resolve(res);
        });
        return defer.promise;
      })()
    };

    // data store for topic category
    if (gnSearchSettings.gnStores) {
      var topicCats = gnSearchSettings.gnStores.topicCat;
      topicCats.forEach(function(cat, i) {
        topicCats[i] = {
          id: cat.id || cat[0],
          name: cat.name || cat[1],
          hierarchy: cat.hierarchy ||
            (cat[0].indexOf('_') > 0 ? 'second' : 'main')
        };
      });
      this.topicCatsOptions = {
        mode: 'local',
        data: topicCats,
        config: {
          templates: {
            suggestion: Handlebars.compile(
              '<p class="topiccat-{{hierarchy}}">{{name}}</p>')
          }
        }
      };

      // data store for formats
      this.formatsOptions = {
        mode: 'remote',
        remote: {
          url: suggestService.getUrl('QUERY', 'format',
            'STARTSWITHFIRST'),
          filter: suggestService.bhFilter,
          wildcard: 'QUERY'
        }
      };
    }

    // data store for types field
    this.types = [
      'dataset',
      'basicgeodata',
      'basicgeodata-federal',
      'basicgeodata-cantonal',
      'basicgeodata-communal',
      'service',
      'service-OGC:WMS',
      'service-OGC:WFS'
    ];

    // data store for types field
    this.geodataTypes = [
      'basicGeodata',
      'oereb',
      'oerebRegister',
      'openGovernmentData',
      'openData',
      'referenceGeodata'
    ];

    // data store for valid field
    this.validStore = [{
      value: '',
      label: 'anyValue'
    }, {
      value: '1',
      label: 'yes'
    }, {
      value: '0',
      label: 'no'
    },{
      value: '-1',
      label: 'unchecked'
    }];


  };





  SearchFormController['$inject'] = [
    '$http',
    '$q',
    'suggestService',
    'gnLangs',
    'gnSearchSettings'
  ];

  module.controller('gcSearchFormCtrl', SearchFormController);

})();
