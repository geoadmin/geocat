import iso19139.SummaryFactory

def isoHandlers = new iso19139che.Handlers(handlers, f, env)

SummaryFactory.summaryHandler({it.parent() is it.parent()}, isoHandlers)

isoHandlers.addDefaultHandlers()