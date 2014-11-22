//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.geocat.services.thesaurus;

import com.google.common.collect.Maps;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Map;

/**
 * Returns a list of keywords given a list of thesaurus
 */

public class GetCHEKeywordById implements Service {
    private volatile Map<String, KeywordBean> gemetWords;

    public void init(Path appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	/**
	 * lang parameter should be metadata main language. If not set, use GUI 
	 * language.
	 * 
	 * locales parameters is the list of locales set in metadata record.
	 * 
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String sThesaurusName = Util.getParam(params, "thesaurus");
		String uri = Util.getParam(params, "id").replace('\\', '/');
		String lang = Util.getParam(params, "lang", context.getLanguage());
		String locales = Util.getParam(params, "locales", "");
        Element response = new Element(Jeeves.Elem.RESPONSE);

        ThesaurusManager thesaurusMan = context.getBean(ThesaurusManager.class);

        String[] langs = locales.split(",");
        KeywordBean kb;
        if ("external.theme.gemet-theme".equals(sThesaurusName)){
            kb = getGemetKeyword(context, Util.getParam(params, "id").replace('\\', '/'), thesaurusMan);
        } else {
            KeywordsSearcher searcher = null;
            // perform the search and save search result into session

            searcher = new KeywordsSearcher(context, thesaurusMan);

            for (int i = 0; i < langs.length; i++) {
                langs[i] = context.getBean(IsoLanguagesMapper.class).iso639_1_to_iso639_2(langs[i], langs[i]);
            }
            kb = searcher.searchById(uri, sThesaurusName, langs);
        }
        if (kb == null)
			return response;
		else {
			Element element = kb.toElement(thesaurusMan, context, lang, langs);
			return response.addContent(element);
		}
	}

    private KeywordBean getGemetKeyword(ServiceContext context, String uri, ThesaurusManager thesaurusMan) throws Exception {
        if (gemetWords == null) {
            synchronized (this) {
                if (gemetWords == null) {
                    KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(context.getBean(IsoLanguagesMapper.class));
                    builder.addLang("eng");
                    builder.addLang("ger");
                    builder.addLang("fre");
                    builder.addLang("ita");
                    builder.addLang("roh");
                    builder.addThesaurus("external.theme.gemet-theme");

                    KeywordsSearcher searcher = new KeywordsSearcher(context, thesaurusMan);

                    searcher.search(builder.build());
                    gemetWords = Maps.newHashMap();
                    for (KeywordBean keywordBean : searcher.getResults()) {
                        gemetWords.put(simplifyGemetId(keywordBean.getUriCode()), keywordBean);
                    }
                }
            }
        }

        return this.gemetWords.get(simplifyGemetId(uri));
    }

    private String simplifyGemetId(String uri) {
        if (uri.contains("gemet-theme.rdf/theme/")) {
            return "theme/" + uri.split("\\Qgemet-theme.rdf/theme/\\E")[1];
        }
        return uri;
    }
}

// =============================================================================

