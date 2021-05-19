package iso19139che

import groovy.util.slurpersupport.GPathResult
import org.fao.geonet.api.records.formatters.groovy.MapConfig

public class Handlers extends iso19139.Handlers {
  public Handlers(handlers, f, env) {
    super(handlers, f, env);
    isofunc = new Functions(handlers: handlers, f: f, env: env, commonHandlers: commonHandlers)
    matchers = new Matchers(handlers: handlers, f: f, env: env)
    this.rootEl = 'che:CHE_MD_Metadata'
    this.packageViews.remove('gmd:MD_Metadata')
    this.packageViews << 'che:legislationInformation'
    this.packageViews << this.rootEl
  }

  def addDefaultHandlers() {
    super.addDefaultHandlers();
    handlers.add name: 'Color Rejected Elements', select: matchers.isRejected, priority: 100, rejectedElementHandler
  }

  def rejectedElementHandler = { el ->
    try {
      matchers.handlingRejectedEls.set(true)
      def childData = handlers.processElements([el]);
      return handlers.fileResult('html/rejected.html', [
        childData   : childData,
        label       : f.translate("rejectedTitle"),
        rejectedDesc: f.translate("rejectedDesc")
      ])
    } finally {
      matchers.handlingRejectedEls.set(false)
    }
  }

  def pointOfContactGeneralData(party) {
    def generalChildren = [
      party.'gmd:organisationName',
      party.'che:organisationAcronym',
      party.'gmd:positionName',
      party.'gmd:role'
    ]
    def nameString = party.'che:individualFirstName'.text() + " " + party.'che:individualLastName'.text()
    def name = ""
    if (!nameString.trim().isEmpty()) {
      name = commonHandlers.func.textEl(f.nodeLabel('gmd:individualName', null), nameString)
    }
    def childData = name.toString() + handlers.processElements(generalChildren)
    handlers.fileResult('html/2-level-entry.html', [label: f.translate('general'), childData: childData])
  }


  def findParentXLink(GPathResult el) {
    if (el.name() != 'gmd:extent' || el['@xlink:href'].text().isEmpty()) {
      if (el.parent().is(el)) {
        return "";
      }
      return findParentXLink(el.parent())
    }
    return el['@xlink:href'].text()
  }

  // bbox bounds are checked against the limits of the swiss projection (EPSG:21781)
  def isWorldExtentUsed(extentEl) {
      def bboxEl = extentEl.'*'.'gmd:EX_GeographicBoundingBox'
      if (bboxEl.isEmpty()) {
          return false
      }
      // bounds are from the EPSG 21781: http://spatialreference.org/ref/epsg/ch1903-lv03/
      // a buffer of 200% was applied
      if (bboxEl.'gmd:westBoundLongitude'.'gco:Decimal'.text().toFloat() <  1.45 ||
          bboxEl.'gmd:eastBoundLongitude'.'gco:Decimal'.text().toFloat() > 15.01 ||
          bboxEl.'gmd:southBoundLatitude'.'gco:Decimal'.text().toFloat() < 43.79 ||
          bboxEl.'gmd:northBoundLatitude'.'gco:Decimal'.text().toFloat() > 49.82) {
          return true
      }
      return false
  }

  def geomOrBbox = { node -> node.parent().name() in ["gmd:geographicElement"] }

  def polygonEl(thumbnail) {
      return { el ->
          MapConfig mapConfig = env.mapConfiguration
          def mapproj = mapConfig.mapproj
          def background = mapConfig.background
          def width = thumbnail? mapConfig.thumbnailWidth : mapConfig.width
          def mdId = env.getMetadataUUID();
          def xpath = f.getXPathFrom(el);

          // Geocat specific: use 4326 projection when bbox bounds are too wide
          if (isWorldExtentUsed(el.parent().parent())) {
            mapproj = 'EPSG:4326'
            background = 'osm'
          }

          if (xpath != null) {
              def count = el.parent().parent().parent().parent().depthFirst().findAll(geomOrBbox).indexOf(el) + 1

              def locUrl= env.localizedUrl.substring(0, env.localizedUrl.lastIndexOf("/"))
              locUrl= locUrl.substring(0, locUrl.lastIndexOf("/"))
              def source = "$locUrl/api/records/$mdId/extents/${count}.png?mapsrs=$mapproj&amp;width=$width&amp;background=$background";
              def image = "<img src=\"$source\" style=\"min-width:${width/4}px; min-height:${width/4}px;\" />"

              def inclusion = el.'gmd:extentTypeCode'.text() == '0' ? 'exclusive' : 'inclusive';

              def label = f.nodeLabel(el) + " (" + f.translate(inclusion) + ")"
              handlers.fileResult('html/2-level-entry.html', [label: label, childData: image])
          }
      }
  }

    def aggPolygonEl(thumbnail) {
        return { el ->
            MapConfig mapConfig = env.mapConfiguration
            def mapproj = mapConfig.mapproj
            def background = mapConfig.background
            def width = thumbnail? mapConfig.thumbnailWidth : mapConfig.width
            def mdId = env.getMetadataUUID();
            def xpath = f.getXPathFrom(el);

            // Geocat specific: use 4326 projection when bbox bounds are too wide
            if (isWorldExtentUsed(el.parent().parent())) {
                mapproj = 'EPSG:4326'
                background = 'osm'
            }

            if (xpath != null) {
                def locUrl= env.localizedUrl.substring(0, env.localizedUrl.lastIndexOf("/"))
                locUrl= locUrl.substring(0, locUrl.lastIndexOf("/"))
                def source = "$locUrl/api/records/$mdId/extents.png?mapsrs=$mapproj&amp;width=$width&amp;background=$background";
                def image = "<img src=\"$source\" style=\"min-width:${width/4}px; min-height:${width/4}px;\" />"

                def inclusion = el.'gmd:extentTypeCode'.text() == '0' ? 'exclusive' : 'inclusive';

                def label = f.nodeLabel(el) + " (" + f.translate(inclusion) + ")"
                handlers.fileResult('html/2-level-entry.html', [label: label, childData: image])
            }
        }
    }

  def bboxEl(thumbnail) {
      return { el ->
          if (el.parent().'gmd:EX_BoundingPolygon'.text().isEmpty() &&
                  el.parent().parent().'gmd:geographicElement'.'gmd:EX_BoundingPolygon'.text().isEmpty()) {

              def inclusion = el.'gmd:extentTypeCode'.text() == '0' ? 'exclusive' : 'inclusive';

              def label = f.nodeLabel(el) + " (" + f.translate(inclusion) + ")"

              def replacements = bbox(thumbnail, el)
              replacements['label'] = label
              replacements['pdfOutput'] = commonHandlers.func.isPDFOutput()

              // Geocat specific: use 4326 projection when bbox bounds are too wide
              if (isWorldExtentUsed(el.parent().parent())) {
                replacements['mapconfig']['mapproj'] = 'EPSG:4326'
                replacements['mapconfig']['background'] = 'osm'
              }

              handlers.fileResult("html/bbox.html", replacements)
          }
      }
  }
}
