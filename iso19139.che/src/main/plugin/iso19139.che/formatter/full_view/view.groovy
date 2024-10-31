import iso19139.SummaryFactory

def isoHandlers = new iso19139che.Handlers(handlers, f, env)
SummaryFactory.summaryHandler({ it.parent() is it.parent() }, isoHandlers)

isoHandlers.addDefaultHandlers()

handlers.add name: 'gmd:topicCategory', select: 'gmd:topicCategory', group: true, priority: 5, { elems ->
  def listItems = elems
          .findAll { !it.text().isEmpty()}
          .collectMany{[it.text(), it.text().split("_")[0]]}
          .unique()
          .sort{f.codelistValueLabel("MD_TopicCategoryCode", it)}
          .collect {f.codelistValueLabel("MD_TopicCategoryCode", it)};
  handlers.fileResult("html/list-entry.html", [label: f.nodeLabel(elems[0]), listItems: listItems])
}

handlers.skip name: 'skip che:CHE_MD_Appraisal_AAP', select: { it -> it.name() == 'che:CHE_MD_Appraisal_AAP' }, {
  it.children()
}
