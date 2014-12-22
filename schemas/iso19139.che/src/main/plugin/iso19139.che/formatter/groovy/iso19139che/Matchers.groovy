package iso19139che

/**
 * @author Jesse on 12/22/2014.
 */
class Matchers extends iso19139.Matchers{
    Matchers() {
        def isoIsUrlEl = super.isUrlEl;
        super.isUrlEl = {el ->
            isoIsUrlEl(el) ||
            !el.'che:PT_FreeURL'.text().isEmpty() ||
            !el.'che:LocalisedURL'.text().isEmpty()
        }
    }

}
