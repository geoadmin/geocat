package iso19139che

public class Handlers extends iso19139.Handlers {

    public Handlers(handlers, f, env) {
        super(handlers, f, env);
        this.rootEl = 'che:CHE_MD_Metadata'
        this.packageViews.remove('gmd:MD_Metadata')
        this.packageViews << 'che:legislationInformation'
        this.packageViews << this.rootEl
    }

}