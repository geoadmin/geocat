package iso19139che

public class Handlers extends iso19139.Handlers {

    public Handlers(handlers, f, env) {
        super(handlers, f, env);
        super.isofunc = new Functions(handlers: handlers, f:f, env:env, commonHandlers: commonHandlers)
        super.matchers = new Matchers(handlers: handlers, f:f, env:env, commonHandlers: commonHandlers)
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
        def nameString = party.'che:individualFirstName'.text() + party.'che:individualLastName'.text()
        def name = handlers.fileResult("html/2-level-entry.html", [label: f.nodeLabel('gmd:individualName', null), childData: nameString])
        def childData = name.toString() + handlers.processElements(generalChildren)
        handlers.fileResult('html/2-level-entry.html', [label: f.translate('general'), childData: childData])
    }
}