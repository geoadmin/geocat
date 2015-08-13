// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestercgp = {
  createNew : function() {
    return {
      "@id": "",
      "@type": "cgp",
      "@owner": "1",
      "site":     {
        "name": "",
        "uuid": "",
        "account":       {
          "use": "false",
          "username": [],
          "password": []
        },
        "url": "",
        "icon": "blank.gif"
      },
      "content":     {
        "validate" : "NOVALIDATION",
        "importxslt": "none"
      },
      "options":     {
        "every": "0 0 0 ? * *",
        "oneRunOnly": "false",
        "status": "active"
      },
      "searches": [],
      "privileges": [    {
        "@id" : "1",
        "operation" : [ {
          "@name" : "view"
        } ]
      }],
      "categories" : [],
      "info":     {
        "lastRun" : [],
        "running" : "false"
      }
    };
  },
  buildResponseCGPSearch : function($scope) {
    var body = '';
    if ($scope.harvesterSelected.searches) {
      console.log($scope.harvesterSelected.searches)
      for(var tag in $scope.harvesterSelected.searches[0]) {
        if($scope.harvesterSelected.searches[0].hasOwnProperty(tag)) {
          var value = $scope.harvesterSelected.searches[0][tag];
          // Save all values even if empty
          // XML to JSON does not convert single child to Object but Array
          // In that situation, saving only one parameter will make this
          // happen and then search criteria name which is the tag name
          // will be lost.
          //                if (value) {
          body += '<' + tag + '>' + value + '</' + tag + '>';
          //            }
        }
      }
    }
    return '<searches><search>' + body + '</search></searches>';
  },
  buildResponse : function(h, $scope) {
    var body = '<node id="' + h['@id'] + '" type="' + h['@type'] + '">' +
      '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>' +
      '   <site>' +
      '      <name>' + h.site.name + '</name>' +
      '      <url>' + h.site.url + '</url>' +
      '      <account>' +
      '         <use>' + h.site.account.use + '</use>' +
      '         <username>' + h.site.account.username + '</username>' +
      '         <password>' + h.site.account.password + '</password>' +
      '      </account>' +
      '      <icon>' + h.site.icon + '</icon>' +
      '   </site>' +
      '  <content>' +
      '    <validate>' + h.content.validate + '</validate>' +
      '    <importxslt>' + h.content.importxslt + '</importxslt>' +
      '  </content>' +
      '   <options>' +
      '      <every>' + h.options.every + '</every>' +
      '      <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>' +
      '   </options>' +
      gnHarvestercgp.buildResponseCGPSearch($scope) +
      $scope.buildResponseGroup(h) +
      $scope.buildResponseCategory(h) + '</node>';
    return body;
  }
};