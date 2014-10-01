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

package org.fao.geonet.geocat.services.user;

import com.google.common.base.Functions;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geocat;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.geocat.GeocatUserInfo;
import org.fao.geonet.domain.geocat.Phone;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.geocat.kernel.reusable.ContactsStrategy;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.util.PasswordUtil;
import org.jdom.Element;

import java.util.*;

//=============================================================================

/** Update the information of a user
  */

public class SharedUpdate implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String operation = Util.getParam(params, Params.OPERATION);
		String id       = params.getChildText(Params.ID);
		String username = Util.getParam(params, Params.USERNAME);

        UserSession usrSess = context.getUserSession();
        Profile      myProfile = usrSess.getProfile();

        if (myProfile != Profile.Administrator) {
            throw new IllegalArgumentException("Only Administrator should be able to access this service");
        }

        Processor.uncacheXLinkUri(ContactsStrategy.baseHref(id));

        final UserGroupRepository groupRepository = context.getBean(UserGroupRepository.class);
        final UserRepository userRepository = context.getBean(UserRepository.class);

        User user = getUser(userRepository, operation, id, username);
        updateUserEntity(user, params, context);

        // -- For adding new user
        if (operation.equals(Params.Operation.NEWUSER)) {
            user = userRepository.save(user);

            setUserGroups(user, params, context);
        } else if (operation.equals(Params.Operation.FULLUPDATE) || operation.equals(Params.Operation.EDITINFO)) {
            user = userRepository.save(user);

            //--- add groups
            groupRepository.deleteAllByIdAttribute(UserGroupId_.userId, Arrays.asList(user.getId()));

            setUserGroups(user, params, context);
        } else {
            throw new IllegalArgumentException("unknown user update operation " + operation);
        }

        final ContactsStrategy strategy = new ContactsStrategy(userRepository, groupRepository, context.getAppPath(), context.getBaseUrl(),
                context.getLanguage());
        ArrayList<String> fields = new ArrayList<String>();

        fields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));
        fields.addAll(Arrays.asList(strategy.getValidXlinkLuceneField()));
        final Set<MetadataRecord> referencingMetadata = Utils.getReferencingMetadata(context, strategy, fields, id, null, false,
                Functions.<String>identity());

        DataManager dm = context.getBean(DataManager.class);
        for (MetadataRecord metadataRecord : referencingMetadata) {
            dm.indexMetadata(""+metadataRecord.id, false, true, false, false, true);
        }

        return new Element(Jeeves.Elem.RESPONSE);
	}

    private void updateUserEntity(User user, Element params, ServiceContext context) {

        String username = Util.getParam(params, Params.USERNAME);
        if (username != null) {
            user.setUsername(username);
        }

        String name     = Util.getParam(params, Params.NAME,    "");
        if (name != null) {
            user.setName(name);
        }
        String surname  = Util.getParam(params, Params.SURNAME, "");
        if (surname != null) {
            user.setSurname(surname);
        }

        user.setProfile(Profile.Shared);

        String kind     = Util.getParam(params, Params.KIND,    "");
        if (kind != null) {
            user.setKind(kind);
        }

        String organ    = LangUtils.createDescFromParams(params, Params.ORG);
        if (organ != null) {
            user.setOrganisation(organ);
        }

        Address addressEntity;
        if (user.getAddresses().isEmpty()) {
            addressEntity = new Address();
        } else {
            addressEntity = user.getPrimaryAddress();

        }

        String address  = Util.getParam(params, Params.ADDRESS, "");
        if (address != null) {
            addressEntity.setAddress(address);
        }

        String city     = Util.getParam(params, Params.CITY,    "");
        if (city != null) {
            addressEntity.setCity(city);
        }

        String state    = Util.getParam(params, Params.STATE,   "");
        if (state != null) {
            addressEntity.setState(state);
        }
        String zip      = Util.getParam(params, Params.ZIP,     "");
        if (zip != null) {
            addressEntity.setZip(zip);
        }
        String country  = Util.getParam(params, Params.COUNTRY, "");
        if (country != null) {
            addressEntity.setCountry(country);
        }

        String streetnb = Util.getParam(params, Geocat.Params.STREETNUMBER, "");
        addressEntity.setStreetnumber(streetnb);

        String street   = Util.getParam(params, Geocat.Params.STREETNAME, "");
        addressEntity.setStreetname(street);

        String postbox  = Util.getParam(params, Geocat.Params.POSTBOX, "");
        addressEntity.setPostbox(postbox);

        user.getAddresses().clear();
        user.getAddresses().add(addressEntity);

        String email    = Util.getParam(params, Params.EMAIL,   "");
        String email1    = Util.getParam(params, Params.EMAIL+1,   "");
        String email2    = Util.getParam(params, Params.EMAIL+2,   "");
        user.getEmailAddresses().add(email);
        user.getEmailAddresses().add(email1);
        user.getEmailAddresses().add(email2);

        user.getPhones().clear();
        String phone    = Util.getParam(params, Geocat.Params.PHONE, "");
        String fac      = Util.getParam(params, Geocat.Params.FAC, "");
        String directnumber = Util.getParam(params, Geocat.Params.DIRECTNUMBER, "");
        String mobile = Util.getParam(params, Geocat.Params.MOBILE, "");
        user.getPhones().add(new Phone()
                .setDirectnumber(directnumber)
                .setFacsimile(fac)
                .setMobile(mobile)
                .setPhone(phone));
        String phone1    = Util.getParam(params, Geocat.Params.PHONE+1, "");
        String fac1      = Util.getParam(params, Geocat.Params.FAC+1, "");
        user.getPhones().add(new Phone()
                .setFacsimile(fac1)
                .setPhone(phone1));
        String phone2    = Util.getParam(params, Geocat.Params.PHONE+2, "");
        String fac2      = Util.getParam(params, Geocat.Params.FAC+2, "");
        user.getPhones().add(new Phone()
                .setFacsimile(fac2)
                .setPhone(phone2));

        String validated = Util.getParam(params, Geocat.Params.VALIDATED, "y");
        final GeocatUserInfo geocatUserInfo = user.getGeocatUserInfo();
        geocatUserInfo.setValidated(Constants.toBoolean_fromYNChar(validated.charAt(0)));

        String instruct = LangUtils.createDescFromParams(params, Geocat.Params.CONTACTINST);
        geocatUserInfo.setContactinstructions(instruct);

        String hours    = Util.getParam(params, Geocat.Params.HOURSOFSERV, "");
        geocatUserInfo.setHoursofservice(hours);

        String onlinedesc  = LangUtils.createDescFromParams(params, "onlinedescription");
        geocatUserInfo.setOnlinedescription(onlinedesc);

        String orgacronym = LangUtils.createDescFromParams(params, Geocat.Params.ORGACRONYM);
        geocatUserInfo.setOrgacronym(orgacronym);

        String onlinename  = LangUtils.createDescFromParams(params, "onlinename");
        geocatUserInfo.setOnlinename(onlinename);

        String online      = LangUtils.createDescFromParams(params, Geocat.Params.ONLINE);
        geocatUserInfo.setOnlineresource(online);

        String position = LangUtils.createDescFromParams(params, Geocat.Params.POSITIONNAME);
        geocatUserInfo.setPositionname(position);

        String publicAccess = Util.getParam(params, Geocat.Params.PUBLICACC, "n");
        geocatUserInfo.setPublicaccess(publicAccess);

        user.getSecurity().setPassword(PasswordUtil.encode(context, UUID.randomUUID().toString()));
    }

    private User getUser(final UserRepository repo, final String operation, final String id, final String username) {
        if (Params.Operation.NEWUSER.equalsIgnoreCase(operation)) {
            if (username == null) {
                throw new IllegalArgumentException(Params.USERNAME + " is a required parameter for " + Params.Operation.NEWUSER + " " +
                                                   "operation");
            }
            User user = repo.findOneByUsername(username);

            if (user != null) {
                throw new IllegalArgumentException("User with username " + username + " already exists");
            }
            return new User();
        } else {
            User user = repo.findOne(id);
            if (user == null) {
                throw new IllegalArgumentException("No user found with id: " + id);
            }
            return user;
        }
    }

    private void setUserGroups(final User user, final Element params, final ServiceContext context) throws Exception {
        String[] profiles = {Profile.UserAdmin.name(), Profile.Reviewer.name(), Profile.Editor.name(), Profile.RegisteredUser.name()};
        Collection<UserGroup> toAdd = new ArrayList<UserGroup>();

        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);

        for (String profile : profiles) {

            @SuppressWarnings("unchecked")
            java.util.List<Element> userGroups = params.getChildren(Params.GROUPS + '_' + profile);
            for (Element element : userGroups) {
                String groupEl = element.getText();
                if (!groupEl.equals("")) {
                    int groupId = Integer.valueOf(groupEl);
                    Group group = groupRepository.findOne(groupId);

                    // Combine all groups editor and reviewer groups
                    if (profile.equals(Profile.Reviewer.name())) {
                        final UserGroup userGroup = new UserGroup()
                                .setGroup(group)
                                .setProfile(Profile.Editor)
                                .setUser(user);
                        toAdd.add(userGroup);
                    }

                    final UserGroup userGroup = new UserGroup()
                            .setGroup(group)
                            .setProfile(Profile.findProfileIgnoreCase(profile))
                            .setUser(user);
                    toAdd.add(userGroup);
                }
            }
        }

        userGroupRepository.save(toAdd);

    }
}

//=============================================================================

