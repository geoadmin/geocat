//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata;

import jeeves.xlink.XLink;

import org.fao.geonet.domain.geocat.HiddenMetadataElement;
import org.fao.geonet.exceptions.BadParameterEx;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.repository.geocat.HiddenMetadataElementsRepository;

import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.lib.Lib;
import org.jdom.*;

import java.util.*;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;


/**
 * Utilities.
 */
class EditUtils {

    public EditUtils(ServiceContext context) {
        this.context = context;
        this.gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        this.dataManager = gc.getBean(DataManager.class);
        this.xmlSerializer = gc.getBean(XmlSerializer.class);
        this.accessMan = gc.getBean(AccessManager.class);
        this.session = context.getUserSession();

    }
    protected ServiceContext context;
    protected DataManager dataManager;
    protected XmlSerializer xmlSerializer;
	protected GeonetContext gc;
	protected AccessManager accessMan;
	protected UserSession session;

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

    /**
     * Performs common editor preprocessing tasks.
     *
     * @param params
     * @param context
     * @throws Exception
     */
	public void preprocessUpdate(Element params, ServiceContext context) throws Exception {

		String id = Util.getParam(params, Params.ID);

		//-----------------------------------------------------------------------
		//--- handle current tab and position

		Element elCurrTab = params.getChild(Params.CURRTAB);
		Element elCurrPos = params.getChild(Params.POSITION);
		boolean useEditTab = Util.getParam(params, "editTab", false);
        String sessionTabProperty = useEditTab ? Geonet.Session.METADATA_EDITING_TAB : Geonet.Session.METADATA_SHOW;
       
		if (elCurrTab != null) {
			session.setProperty(sessionTabProperty, elCurrTab.getText());
		}
		if (elCurrPos != null)
			session.setProperty(Geonet.Session.METADATA_POSITION, elCurrPos.getText());

		//-----------------------------------------------------------------------
		//--- check access
		int iLocalId = Integer.parseInt(id);

		if (!dataManager.existsMetadata(iLocalId))
			throw new BadParameterEx("id", id);

		if (!accessMan.canEdit(context, id))
		    Lib.resource.denyAccess(context);
	}

    /**
     * Updates metadata content.
     *
     * @param params
     * @param validate
     * @throws Exception
     */
	public void updateContent(Element params, boolean validate) throws Exception {
		 updateContent(params, validate, false);
	}

    /**
     * TODO javadoc.
     *
     * @param params
     * @param validate
     * @param embedded
     * @throws Exception
     */
	public void updateContent(Element params, boolean validate, boolean embedded) throws Exception {
		String id      = Util.getParam(params, Params.ID);
		String version = Util.getParam(params, Params.VERSION);
        String minor      = Util.getParam(params, Params.MINOREDIT, "false");

		//--- build hashtable with changes
		//--- each change is a couple (pos, value)

		Map<String, String> htChanges = new HashMap<String, String>(100);
        // GEOCAT
		Map<String, String> htHide = new HashMap<String, String>(100);
        // END GEOCAT

		@SuppressWarnings("unchecked")
        List<Element> list = params.getChildren();
		for (Element el : list) {
			String sPos = el.getName();
			String sVal = el.getText();

            // GEOCAT
			if (sPos.startsWith("_") && !sPos.startsWith("_d_")) {
				htChanges.put(sPos.substring(1), sVal);
            } else if (sPos.startsWith("hide_")) {

                String ref = sPos.substring(5);
                if(ref.startsWith("d_")) {
                    ref = ref.substring(2);
            }
                htHide.put(ref, sVal);
		    }
            // END GEOCAT

			if (sPos.startsWith("_")) {
				htChanges.put(sPos.substring(1), sVal);
            }
		}

        //
		// update element and return status
        //

        Metadata result = null;
        // whether to request automatic changes (update-fixed-info)
        boolean ufo = true;
        // whether to index on update
        boolean index = true;
        // does not need to process here.  AddXLink will handle
        // processing of reusable object
        // mind you xml submission should probably change this.
        boolean processReusableObject = false;

        boolean updateDateStamp = !minor.equals("true");
        String changeDate = null;
		if (embedded) {
            Element updatedMetada = new AjaxEditUtils(context).applyChangesEmbedded(id, htChanges, htHide, version, context.getLanguage());
            if(updatedMetada != null) {
                result = dataManager.updateMetadata(context, id, updatedMetada, validate, ufo, index, context.getLanguage(), changeDate, updateDateStamp, processReusableObject);
            }
   		}
        else {
            Element updatedMetada = applyChanges(id, htChanges, htHide, version, context.getLanguage());
            if(updatedMetada != null) {
			    result = dataManager.updateMetadata(context, id, updatedMetada, validate, ufo, index, context.getLanguage(), changeDate, updateDateStamp, processReusableObject);
            }
		}
		if (result == null) {
			throw new ConcurrentUpdateEx(id);
        }
	}

    /**
     * TODO javadoc.
     *
     * @param id
     * @param changes
     * @param currVersion
     * @return
     * @throws Exception
     */
    // GEOCAT
    private Element applyChanges(String id, Map<String, String> changes, Map<String, String> htHide, String currVersion,
                                 String lang) throws Exception {
        // END GEOCAT
        Lib.resource.checkEditPrivilege(context, id);
        Element md = xmlSerializer.select(context, id);

		//--- check if the metadata has been deleted
		if (md == null) {
			return null;
        }

        EditLib editLib = dataManager.getEditLib();

        String schema = dataManager.getMetadataSchema(id);
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
			return null;
        }

        // GEOCAT
        HashSet<Element> updatedXLinks = new HashSet<Element>();
        // END GEOCAT

		//--- update elements
		for (Map.Entry<String, String> entry : changes.entrySet()) {
			String ref = entry.getKey().trim();
			String val = entry.getValue().trim();
			String attr= null;

            // GEOCAT
			if(updatedLocalizedTextElement(md, ref, val, editLib, updatedXLinks) && updatedLocalizedURLElement(md, ref, val, editLib, updatedXLinks)) {
                // END GEOCAT
			    continue;
			}

			int at = ref.indexOf('_');
			if (at != -1) {
				attr = ref.substring(at +1);
				ref  = ref.substring(0, at);
			}
			boolean xmlContent = false;
            if (ref.startsWith("X")) {
                ref = ref.substring(1);
                xmlContent = true;
            }
			Element el = editLib.findElement(md, ref);
			if (el == null)
				throw new IllegalStateException("Element not found at ref = " + ref);

            // GEOCAT
            Element xlinkParent = findXlinkParent(el);
            if( xlinkParent!=null && ReusableObjManager.isValidated(xlinkParent)){
                continue;
            }
            if( xlinkParent!=null ){
                updatedXLinks.add(xlinkParent);
            }
            // END GEOCAT

			if (attr != null) {
                // The following work-around decodes any attribute name that has a COLON in it
                // The : is replaced by the word COLON in the xslt so that it can be processed
                // by the XML Serializer when an update is submitted - a better solution is
                // to modify the argument handler in Jeeves to store arguments with their name
                // as a value rather than as the element itself
				Integer indexColon = attr.indexOf("COLON");
                if (indexColon != -1) {
					String prefix = attr.substring(0,indexColon);
                    String localname = attr.substring(indexColon + 5);
                    String namespace = editLib.getNamespace(prefix + ":" + localname, md, dataManager.getSchema(schema));
					Namespace attrNS = Namespace.getNamespace(prefix,namespace);
                    if (el.getAttribute(localname,attrNS) != null) {
                        el.setAttribute(new Attribute(localname,val,attrNS));
                    }
                // End of work-around
                }
                else {
                    if (el.getAttribute(attr) != null)
                        el.setAttribute(new Attribute(attr, val));
                }
			}
            else if(xmlContent) {
                if(Log.isDebugEnabled(Geonet.EDITOR))
                    Log.debug(Geonet.EDITOR, "replacing XML content");
				el.removeContent();
				val = EditLib.addNamespaceToFragment(val);
				el.addContent(Xml.loadString(val, false));
            }
			else {
				@SuppressWarnings("unchecked")
                List<Content> content = el.getContent();

				for (Iterator<Content> iterator = content.iterator(); iterator.hasNext();) {
                    Content content2 = iterator.next();
                    
					if (content2 instanceof Text) {
					    iterator.remove();
					}
				}
				el.addContent(val);
			}
		}
        // GEOCAT
		applyHiddenElements(context, dataManager, editLib, md, id, htHide);
        // END GEOCAT

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

		editLib.contractElements(md);

        // GEOCAT
        dataManager.updateXlinkObjects(id, lang, md, updatedXLinks.toArray(new Element[updatedXLinks.size()]));
        // END GEOCAT
        return md;
    }
    // GEOCAT
    public static void applyHiddenElements( ServiceContext context, DataManager dataManager, EditLib editLib, Element md, String id, Map<String, String> htHide ) throws Exception {
        // Add Hiding info to MD tree
        dataManager.addHidingInfo(context, md, id);

        // Generate and manage XPath/levels for Elements to be hidden
        Integer idInteger = new Integer(id);
        for (Map.Entry<String, String> entry: htHide.entrySet())
        {
            String ref = entry.getKey();
            String level = entry.getValue();
            String xPathExpr = null;

            // System.out.println("HIDING ref = " + ref + " - level = " + level); // DEBUG
            Element el = editLib.findElement(md, ref);
            if (el == null)
            {
                //elements may have been replaced
                continue;
            }
            final HiddenMetadataElementsRepository hiddenMetadataElementsRepository = context.getBean(HiddenMetadataElementsRepository.class);

            // Find possible existing Element-hiding info
            Element hideElm = el.getChild("hide", Edit.NAMESPACE);
            if (hideElm != null) {
                // Delete possible existing XPath expressions/level for this Element
                xPathExpr = editLib.getXPathExpr(md, ref);
                if (xPathExpr == null)
                {
                    throw new IllegalStateException("Cannot create XPath expression for (already hidden) ref = " + ref);
                }

                // Delete existing hiding info
                // System.out.println("HIDING ref = " + ref + " DELETE = " + xPathExpr); // DEBUG
                final List<HiddenMetadataElement> elems = hiddenMetadataElementsRepository
                        .findAllByMetadataIdAndXPathExpr(idInteger, xPathExpr);
                hiddenMetadataElementsRepository.delete(elems);
            }

            if ("no".equals(level))
            {
                // No hiding specified for this element, nothing to do
                continue;
            }

            // We have hiding: create XPath Expr to element if not yet generated
            if (xPathExpr == null) {
                xPathExpr = editLib.getXPathExpr(md, ref);
                if (xPathExpr == null)
                {
                    throw new IllegalStateException("Cannot create XPath expression for ref = " + ref);
                }
            }

            // Save hiding info
            // System.out.println("HIDING ref = " + ref + " UPDATE = " + xPathExpr); // DEBUG
            final HiddenMetadataElement element = new HiddenMetadataElement();
            element.setLevel(level);
            element.setMetadataId(idInteger);
            element.setxPathExpr(xPathExpr);
            hiddenMetadataElementsRepository.save(element);
        }
    }
    // END GEOCAT
    public static boolean updatedLocalizedURLElement( Element md, String ref, String val, EditLib editLib, HashSet<Element> updatedXLinks ) {
        if (ref.startsWith("url")) {
            if (val.length() > 0) {
                String[] ids = ref.split("_");
                Element parent = editLib.findElement(md, ids[2]);

                Element xlinkParent = findXlinkParent(parent);
                if (xlinkParent != null && ReusableObjManager.isValidated(xlinkParent)) {
                    return true;
                }
                if (xlinkParent != null) {
                    updatedXLinks.add(xlinkParent);
                }

                parent.setAttribute("type", "che:PT_FreeURL_PropertyType",
                        Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
                Namespace che = Namespace.getNamespace("che", "http://www.geocat.ch/2008/che");
                Element langElem = new Element("LocalisedURL", che);
                langElem.setAttribute("locale", "#" + ids[1]);
                langElem.setText(val);

                Element freeURL = getOrAdd(parent, "PT_FreeURL", che);

                Element urlGroup = new Element("URLGroup", che);
                freeURL.addContent(urlGroup);
                urlGroup.addContent(langElem);
                Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                refElem.setAttribute(Edit.Element.Attr.REF, "");
                urlGroup.addContent(refElem);
                langElem.addContent((Element) refElem.clone());
            }
            return true;
        }
        return false;
    }

    public static  Element findXlinkParent(Element el) { return findXlinkParent(el,null);}
	public static  Element findXlinkParent(Element el, Element topXLink)
    {
        if (el==null) return topXLink;
        if(el.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK)!=null){
            return findXlinkParent(el.getParentElement(), el);
        } else {
            return findXlinkParent(el.getParentElement(), topXLink);
        }
    }

    /**
     * Adds a localised character string to an element for an ISO19139 record.
     *
     * <pre>
     * <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
     *    <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
     *    <gmd:PT_FreeText>
     *        <gmd:textGroup>
     *            <gmd:LocalisedCharacterString locale="#FRE">Mod�le de donn�es vectorielles en ISO19139 (multilingue)</gmd:LocalisedCharacterString>
     *        </gmd:textGroup>
     * </pre>
     *
     * @param md metadata record
     * @param ref current ref of element. All _lang_AB_123 element will be processed.
     * @param val
     * @return
     */
    protected static boolean updatedLocalizedTextElement(Element md, String ref, String val, EditLib editLib, HashSet<Element> updatedXLinks) {
        if (ref.startsWith("lang")) {
            if (val.length() > 0) {
                String[] ids = ref.split("_");
                // --- search element in current parent
                Element parent = editLib.findElement(md, ids[2]);

                // GEOCAT
                Element xlinkParent = findXlinkParent(parent);
                if( xlinkParent!=null && ReusableObjManager.isValidated(xlinkParent)){
                    return true;
                }
                if( xlinkParent!=null ){
                    updatedXLinks.add(xlinkParent);
                }
                // END GEOCAT

                List<Element> elems = null;
                try {
                  XPath xpath = XPath.newInstance(".//gmd:LocalisedCharacterString[@locale='#" + ids[1] + "']");
                  @SuppressWarnings("unchecked")
                  List<Element> tmp = xpath.selectNodes(parent);
                  elems = tmp;
                } catch (Exception e) {
                  Log.debug(Geonet.DATA_MANAGER, "updatedLocalizedTextElement exception " + e.getMessage());
                }
                
                // Element exists, set the value
                if (elems != null  && elems.size() > 0) {
                  elems.get(0).setText(val);
                } else {
                  // --- add required attribute
                  parent.setAttribute("type", "gmd:PT_FreeText_PropertyType", Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
  
                  // --- add new translation
                  Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
                  Element langElem = new Element("LocalisedCharacterString", gmd);
                  langElem.setAttribute("locale", "#" + ids[1]);
                  langElem.setText(val);
  
                  Element freeText = getOrAdd(parent, "PT_FreeText", gmd);
  
                  Element textGroup = new Element("textGroup", gmd);
                  freeText.addContent(textGroup);
                  textGroup.addContent(langElem);
                  
                  Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                  refElem.setAttribute(Edit.Element.Attr.REF, "");
                  textGroup.addContent(refElem);
                  langElem.addContent((Element) refElem.clone());
                }
            }
            return true;
        }
        return false;
    }


	/**
     * If no PT_FreeText element exists, creates a geonet:element with an empty ref.
     *
     * @param parent
     * @param name
     * @param ns
     * @return
     */
	protected static Element getOrAdd(Element parent, String name, Namespace ns) {
		Element child = parent.getChild(name, ns);
		if (child == null) {
			child = new Element(name, ns);
			Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
			refElem.setAttribute(Edit.Element.Attr.REF, "");
			child.addContent(refElem);
			parent.addContent(child);
		}
		return child;
	}

    /**
     *
     * @param params
     * @throws Exception
     */
	public void updateContent(Element params) throws Exception {
		updateContent(params, false);
	}

    /**
     * Used for editing : swaps 2 elements.
     *
     * @param el1
     * @param el2
     * @throws Exception
     */
	protected void swapElements(Element el1, Element el2) throws Exception {

		Element parent = el1.getParentElement();
		if (parent == null) {
			throw new IllegalArgumentException("No parent element for swapping");
		}

		int index1 = parent.indexOf(el1);
		if (index1 == -1) {
			throw new IllegalArgumentException("Element 1 not found for swapping");
		}
		int index2 = parent.indexOf(el2);
		if (index2 == -1) {
			throw new IllegalArgumentException("Element 2 not found for swapping");
		}

		Element el1Spare = (Element)el1.clone();

		parent.setContent(index1, (Element)el2.clone());
		parent.setContent(index2, el1Spare);
	}
}