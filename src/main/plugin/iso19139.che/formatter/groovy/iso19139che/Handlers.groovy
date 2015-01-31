package iso19139che

import org.fao.geonet.services.metadata.format.groovy.util.MenuAction
import org.fao.geonet.services.metadata.format.groovy.util.Summary

public class Handlers extends iso19139.Handlers {

    Closure<Summary> summaryCustomizer = {summary ->

        List submenu = summary.findAction("export").submenu
        submenu.add(1,  new MenuAction(label: "exportIso19139", javascript: "window.open('xml_iso19139?uuid=${this.env.metadataUUID}', '_blank')", iconClasses: "fa fa-file-code-o"),)
        submenu.add(2,  new MenuAction(label: "exportGM03", javascript: "window.open('gm03.xml?uuid=${this.env.metadataUUID}', '_blank')", iconClasses: "fa fa-file-code-o"),)
    }

    public Handlers(handlers, f, env) {
        super(handlers, f, env);
        isofunc = new Functions(handlers: handlers, f:f, env:env, commonHandlers: commonHandlers)
        matchers = new Matchers(handlers: handlers, f:f, env:env)
        this.rootEl = 'che:CHE_MD_Metadata'
        this.packageViews.remove('gmd:MD_Metadata')
        this.packageViews << 'che:legislationInformation'
        this.packageViews << this.rootEl
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
        if (name.isEmpty()) {
            name = commonHandlers.func.textEl(f.nodeLabel('gmd:individualName', null), nameString)
        }
        def childData = name.toString() + handlers.processElements(generalChildren)
        handlers.fileResult('html/2-level-entry.html', [label: f.translate('general'), childData: childData])
    }
}