package iso19139che

/**
 * @author Jesse on 12/22/2014.
 */
class Functions extends iso19139.Functions {
  Functions() {
    isoUrlText = localizedUrlText;
  }

  def localizedUrlText = { el ->
    def uiCode = '#' + env.lang2.toUpperCase()
    def locStrings = el.'**'.findAll {
      it.name() == 'che:LocalisedURL' && !it.text().isEmpty() && !(it.text().length() == 1 && (it.text() =~ CHAR_PATTERN).matches())
    }
    def ptEl = locStrings.find { it.'@locale' == uiCode }
    if (ptEl != null) return ptEl.text()
    def charString = el.'**'.findAll {
      it.name() == 'gmd:URL' && !it.text().isEmpty() && !(it.text().length() == 1 && (it.text() =~ CHAR_PATTERN).matches())
    }
    if (!charString.isEmpty()) return charString[0].text()
    if (!locStrings.isEmpty()) return locStrings[0].text()

    el.'gmd:URL'.text()
  }
}
