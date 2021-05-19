package iso19139che

/**
 * @author Jesse on 12/22/2014.
 */
class Matchers extends iso19139.Matchers {
  ThreadLocal<Boolean> handlingRejectedEls = new ThreadLocal<Boolean>();

  Matchers() {
    handlingRejectedEls.set(Boolean.FALSE)
    def isoIsUrlEl = super.isUrlEl;
    isUrlEl = { el ->
      isoIsUrlEl(el) ||
        !el.'che:PT_FreeURL'.text().isEmpty() ||
        !el.'che:LocalisedURL'.text().isEmpty()
    }
  }

  def isRejected = { el ->
    !handlingRejectedEls.get() && el.'@xlink:href'.text().contains('xml.reusable.deleted')
  }
}
