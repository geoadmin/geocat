package iso19139che

public class Handlers extends iso19139.Handlers {

    public Handlers(handlers, f, env) {
        super(handlers, f, env);
        this.packageViews += 'che:legislationInformation'
    }
}