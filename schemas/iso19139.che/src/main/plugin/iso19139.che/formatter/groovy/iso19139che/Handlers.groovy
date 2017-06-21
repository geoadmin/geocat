package iso19139che

import groovy.util.slurpersupport.GPathResult

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
}
