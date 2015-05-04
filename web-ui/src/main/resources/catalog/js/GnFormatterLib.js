(function() {
  goog.provide('gn_formatter_lib');

  gnFormatter = {};
  gnFormatter.formatterSectionTogglersEventHandler = function(e) {
    var thisEl = $(e.currentTarget);
    thisEl.toggleClass('closed');

    e.preventDefault();

    var visible = $("#" + thisEl.attr('target')).toggle().is(":visible");
    console.log()
    return visible;
  };

  gnFormatter.formatterOnComplete = function() {
    var navAnchors = $('.view-outline a[rel], .view-outline a[href]');

    $('.gn-metadata-view .toggler').on('click',
        gnFormatter.formatterSectionTogglersEventHandler);

    $.each(navAnchors, function(idx) {
      var rel = $($.attr(navAnchors[idx], 'rel'));
      var href = $.attr(navAnchors[idx], 'href');
      if (rel.length == 0 && !href) {
        $(navAnchors[idx]).attr('disabled', 'disabled');
      }
    });

    var selectGroup = function(el) {
      $('.container > .entry').hide();
      $(el.attr('rel')).show();
      $('li.active').removeClass('active');
      el.parent().addClass('active');
    };
    navAnchors.on('click', function(e) {
      var href = $(this).attr('href');
      if (!href) {
        selectGroup($(this));
        e.preventDefault();
      }
    });

    if (navAnchors.length < 2) {
      $('.view-outline').hide();
    }

    if (navAnchors.length > 0) {
      selectGroup(navAnchors.first());
    }
  };

  gnFormatter.depth = function (linkBlockEl){
    if (linkBlockEl.length > 0) {
      var inc = 0, parent = linkBlockEl.parent();
      if (parent.hasClass('associated-link-row')) {
        inc ++;
      }
      return inc + gnFormatter.depth(parent);
    }

    return 0;
  };
  gnFormatter.loadAssociated = function(event, linkBlockSel, metadataId, parentUuid, spinnerSel) {

    var linkBlockEl = $(linkBlockSel);

    if (angular.isDefined(event)) {
      var isLoading = $('div[associated-loading=loading]').length > 0;
      if (isLoading) {
        return;
      }

      var closeAssociated = linkBlockEl.is(":visible");
      var rowSelector = 'div.associated-link-row';
      var loadingDoneSel = 'div[associated-loading=done]';
      var parentSelector = linkBlockEl.parent().attr("parent");
      var rows, loaded;
      if (angular.isDefined(parentSelector)){
        var parent = $(parentSelector);
        rows = parent.find(rowSelector);
        loaded = parent.find(loadingDoneSel);
      } else {
        rows = $(rowSelector);
        loaded = $(loadingDoneSel);
      }
      rows.toggle(false);
      loaded.removeAttr("associated-loading").html('');
      if (closeAssociated) {
        return;
      }
      var associatedLinkAClass = 'associated-link-a';
      $("a." + associatedLinkAClass).addClass("disabled");
      gnFormatter.formatterSectionTogglersEventHandler(event);
      linkBlockEl.attr('associated-loading', 'loading');
    }

    linkBlockEl.html(
      '<h3><i class="fa fa-sitemap pad-right"></i>' +
          '<i class="fa fa-circle-o-notch fa-spin pad-right associated-spinner"></i></h3>');
    if (spinnerSel) {
      var spinner = $(spinnerSel);
      spinner.show();
    }

    var parentParam = '';
    if (angular.isDefined(parentUuid)) {
      parentParam = '&parentUuid=' + parentUuid;
    }

    $.ajax('md.format.xml?xsl=hierarchy_view&skipPopularity=y&id=' +
        metadataId + parentParam, {
      dataType: 'text',
      success: function(html) {
        if (spinnerSel) {
          spinner.hide();
        }

        $('a.associated-link-a').removeClass("disabled");
        if (!html) {
          return;
        }
        if (angular.isDefined(event)) {
          linkBlockEl.html(html);
          linkBlockEl.attr('associated-loading', 'done');
          linkBlockEl.find("div.associated-link-row").attr("parent", linkBlockSel);
          if (gnFormatter.depth(linkBlockEl) >= 2) {
            linkBlockEl.find("a." + associatedLinkAClass).removeClass(associatedLinkAClass).removeAttr("onclick").attr("style", "color:black; text-decoration:initial")
          }
        } else {
          linkBlockEl.replaceWith(html);
        linkBlockEl = $('.summary-links-associated-link');
        }
        var togglerElements = linkBlockEl.find('.toggler');
        togglerElements.off('click', gnFormatter.formatterSectionTogglersEventHandler);
        togglerElements.on('click', gnFormatter.formatterSectionTogglersEventHandler);

        if (linkBlockEl.find('table').children().length == 0) {
          linkBlockEl.hide();
              $('a[rel = ".container > .associated"]').
                 attr('disabled', 'disabled');
        }
      },
      error: function(req, status, error) {
        if (spinnerSel) {
          spinner.hide();
        }
        linkBlockEl.html('<h3>Error loading related metadata</h3><p><pre>' +
            '<code>' + error.replace('<', '&lt;') + '</code></pre></p>');
        linkBlockEl.show();
      }
    });
  };
  gnFormatter.updateBbox = function(event, n, e, s, w) {
    var button = $(event.target);

    button.parent().find('button').removeClass('active');
    button.addClass("active");

    button.parent().next().find(".coord-north input.form-control").val(n);
    button.parent().next().find(".coord-east input.form-control").val(e);
    button.parent().next().find(".coord-south input.form-control").val(s);
    button.parent().next().find(".coord-west input.form-control").val(w);
  };
  gnFormatter.reprojectBbox = function(event, n, e, s, w) {
    var button = $(event.target);
    if (!button.hasClass("active")) {
      var transform = proj4('EPSG:4326', '+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs');
      var ur = transform.forward([e, n]);
      var ll = transform.forward([w, s]);

      var round = Math.round;
      gnFormatter.updateBbox(event, round(ur[1]), round(ur[0]), round(ll[1]), round(ll[0]));
    }
  };
})();
