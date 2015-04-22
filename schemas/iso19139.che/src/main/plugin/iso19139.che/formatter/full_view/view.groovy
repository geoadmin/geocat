import iso19139.SummaryFactory

def isoHandlers = new iso19139che.Handlers(handlers, f, env)
SummaryFactory.summaryHandler({it.parent() is it.parent()}, isoHandlers)

isoHandlers.addDefaultHandlers()

def iso19139TopicCategories = ['planningCadastre', 'environment', 'geoscientificInformation', 'imageryBaseMapsEarthCover', 'utilitiesCommunication']
handlers.add name: 'gmd:topicCategory', select: 'gmd:topicCategory', group: true, priority: 5, { elems ->
    def listItems = elems.findAll{!it.text().isEmpty() && !iso19139TopicCategories.contains(it.text())}.collect {f.codelistValueLabel("MD_TopicCategoryCode", it.text())};
    handlers.fileResult("html/list-entry.html", [label:f.nodeLabel(elems[0]), listItems: listItems])
}
