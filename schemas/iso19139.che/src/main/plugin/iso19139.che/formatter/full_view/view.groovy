import iso19139.SummaryFactory

def isoHandlers = new iso19139che.Handlers(handlers, f, env)

def factory = new SummaryFactory(isoHandlers, isoHandlers.summaryCustomizer)
factory.summaryCustomizer = isoHandlers.summaryCustomizer

factory.handlers.add name: "Summary Handler", select: {it.parent() is it.parent()}, {factory.create(it).getResult()}


isoHandlers.addDefaultHandlers()