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

package org.fao.geonet.services.user;

import jeeves.server.ServiceConfig;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.domain.responses.UserList;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.domain.geocat.GeocatUserInfo_;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import javax.persistence.criteria.CriteriaQuery;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import javax.persistence.criteria.Expression;

//=============================================================================

/**
 * Retrieves all users in the system
 */
@Controller("admin.user.list")
public class List {
    private Type type;
	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
	    this.type = Type.valueOf(params.getValue("type", "NORMAL"));
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	@RequestMapping(value = "/{lang}/admin.user.list", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	UserList exec() throws Exception {
		User me = userRepository.findOneByUsername(SecurityContextHolder
				.getContext().getAuthentication().getName());

		Set<Integer> hsMyGroups = getGroups(me.getId(), me.getProfile());

		Collection<? extends GrantedAuthority> roles = SecurityContextHolder
				.getContext().getAuthentication().getAuthorities();

		Set<String> profileSet = new HashSet<String>();

		for (GrantedAuthority rol : roles) {
			profileSet.add(rol.getAuthority());
		}

		// --- retrieve all users
		final java.util.List<User> all = userRepository.findAll(SortUtils
				.createSort(User_.username));

		// --- now filter them

		java.util.Set<Integer> usersToRemove = new HashSet<Integer>();

        if (!findingShared && !profileSet.contains(Profile.Administrator.name())) {

			for (User user : all) {
				int userId = user.getId();
				Profile profile = user.getProfile();

				// TODO is this already equivalent to ID?
				if (user.getName().equals(
						SecurityContextHolder.getContext().getAuthentication()
								.getName())) {
                    // user is permitted to access his/her own user information
                    continue;
                }
				Set<Integer> userGroups = getGroups(userId, profile);
				// Is user belong to one of the current user admin group?
				boolean isInCurrentUserAdminGroups = false;
				for (Integer userGroup : userGroups) {
					if (hsMyGroups.contains(userGroup)) {
						isInCurrentUserAdminGroups = true;
						break;
					}
				}
				// if (!hsMyGroups.containsAll(userGroups))
				if (!isInCurrentUserAdminGroups) {
					usersToRemove.add(user.getId());
                }

				if (!profileSet.contains(profile)) {
					usersToRemove.add(user.getId());
                }
			}
		}
		UserList res = new UserList();

		for (User u : Collections.unmodifiableList(all)) {
			if (!usersToRemove.contains(u.getId())) {
				res.addUser(u);
            }
        }

		
		return res;
	}

    // GEOCAT
    static java.util.List<User> makeQuery(EntityManager entityManager, boolean sortByValidated, Type type, String name) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> cbQuery = cb.createQuery(User.class);
        final Root<User> from = cbQuery.from(User.class);

        if(sortByValidated) {
            Expression<String> nameSurname = cb.trim(cb.concat(from.get(User_.name), from.get(User_.surname)));
            Expression<Object> blankNameLast = cb.selectCase(nameSurname).when("", cb.literal("zz")).otherwise(cb.lower(nameSurname));
            Expression<Object> firstValidated = cb.selectCase(from.get(User_.geocatUserInfo).get(GeocatUserInfo_
                    .jpaWorkaround_validated)).when(Constants.YN_TRUE, cb.literal(2)).otherwise(cb.literal(1));
            cbQuery.orderBy(cb.asc(blankNameLast), cb.asc(firstValidated));
        } else {
            cbQuery.orderBy(cb.asc(from.get(User_.username)));
        }

        Specifications<User> specification;
        switch (type) {
            case NON_VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(false));
                break;
            case VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(true));
                break;
            case SHARED:
                specification = where(hasProfile(Profile.Shared));
                break;
            case NORMAL:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
            default:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
        }

        // TODO : Add organisation
        Predicate usernameMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate nameMatch = cb.like(cb.lower(from.get(User_.name)), cb.lower(cb.literal('%'+name+'%')));
        Predicate surnameMatch = cb.like(cb.lower(from.get(User_.surname)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgAcrMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));

        final Predicate or = cb.or(usernameMatch, nameMatch, surnameMatch, orgMatch, orgAcrMatch);
        Expression<Boolean> specPredicate = specification.toPredicate(from, cbQuery, cb);
        cbQuery.where(cb.and(specPredicate, or));

        return entityManager.createQuery(cbQuery).getResultList();
    }
    // END GEOCAT

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	private Set<Integer> getGroups(final int id, final Profile profile)
			throws Exception {
        Set<Integer> hs = new HashSet<Integer>();

        if (profile == Profile.Administrator) {
            hs.addAll(groupRepository.findIds());
        } else if (profile == Profile.UserAdmin) {
			hs.addAll(userGroupRepo.findGroupIds(Specifications.where(
					hasProfile(profile)).and(hasUserId(id))));
        } else {
            hs.addAll(userGroupRepo.findGroupIds(hasUserId(id)));
        }

		return hs;
	}

	@Autowired
	private ConfigurableApplicationContext jeevesApplicationContext;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserGroupRepository userGroupRepo;
	@Autowired
	private UserRepository userRepository;

}
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.fao.geonet.repository.geocat.specification.GeocatUserSpecs.isValidated;
import static org.fao.geonet.repository.specification.UserSpecs.hasProfile;
import static org.springframework.data.jpa.domain.Specifications.not;
import static org.springframework.data.jpa.domain.Specifications.where;
    private Type type;
	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
	    this.type = Type.valueOf(params.getValue("type", "NORMAL"));
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	@RequestMapping(value = "/{lang}/admin.user.list", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	UserList exec() throws Exception {
		User me = userRepository.findOneByUsername(SecurityContextHolder
				.getContext().getAuthentication().getName());

		Set<Integer> hsMyGroups = getGroups(me.getId(), me.getProfile());

		Collection<? extends GrantedAuthority> roles = SecurityContextHolder
				.getContext().getAuthentication().getAuthorities();

		Set<String> profileSet = new HashSet<String>();

		for (GrantedAuthority rol : roles) {
			profileSet.add(rol.getAuthority());
		}

		// --- retrieve all users
		final java.util.List<User> all = userRepository.findAll(SortUtils
				.createSort(User_.username));

		// --- now filter them

		java.util.Set<Integer> usersToRemove = new HashSet<Integer>();

        if (!findingShared && !profileSet.contains(Profile.Administrator.name())) {

			for (User user : all) {
				int userId = user.getId();
				Profile profile = user.getProfile();

				// TODO is this already equivalent to ID?
				if (user.getName().equals(
						SecurityContextHolder.getContext().getAuthentication()
								.getName())) {
                    // user is permitted to access his/her own user information
                    continue;
                }
				Set<Integer> userGroups = getGroups(userId, profile);
				// Is user belong to one of the current user admin group?
				boolean isInCurrentUserAdminGroups = false;
				for (Integer userGroup : userGroups) {
					if (hsMyGroups.contains(userGroup)) {
						isInCurrentUserAdminGroups = true;
						break;
					}
				}
				// if (!hsMyGroups.containsAll(userGroups))
				if (!isInCurrentUserAdminGroups) {
					usersToRemove.add(user.getId());
                }

				if (!profileSet.contains(profile)) {
					usersToRemove.add(user.getId());
                }
			}
		}
		UserList res = new UserList();

		for (User u : Collections.unmodifiableList(all)) {
			if (!usersToRemove.contains(u.getId())) {
				res.addUser(u);
            }
        }

		
		return res;
	}

    // GEOCAT
    static java.util.List<User> makeQuery(EntityManager entityManager, boolean sortByValidated, Type type, String name) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> cbQuery = cb.createQuery(User.class);
        final Root<User> from = cbQuery.from(User.class);

        if(sortByValidated) {
            Expression<String> nameSurname = cb.trim(cb.concat(from.get(User_.name), from.get(User_.surname)));
            Expression<Object> blankNameLast = cb.selectCase(nameSurname).when("", cb.literal("zz")).otherwise(cb.lower(nameSurname));
            Expression<Object> firstValidated = cb.selectCase(from.get(User_.geocatUserInfo).get(GeocatUserInfo_
                    .jpaWorkaround_validated)).when(Constants.YN_TRUE, cb.literal(2)).otherwise(cb.literal(1));
            cbQuery.orderBy(cb.asc(blankNameLast), cb.asc(firstValidated));
        } else {
            cbQuery.orderBy(cb.asc(from.get(User_.username)));
        }

        Specifications<User> specification;
        switch (type) {
            case NON_VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(false));
                break;
            case VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(true));
                break;
            case SHARED:
                specification = where(hasProfile(Profile.Shared));
                break;
            case NORMAL:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
            default:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
        }

        // TODO : Add organisation
        Predicate usernameMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate nameMatch = cb.like(cb.lower(from.get(User_.name)), cb.lower(cb.literal('%'+name+'%')));
        Predicate surnameMatch = cb.like(cb.lower(from.get(User_.surname)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgAcrMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));

        final Predicate or = cb.or(usernameMatch, nameMatch, surnameMatch, orgMatch, orgAcrMatch);
        Expression<Boolean> specPredicate = specification.toPredicate(from, cbQuery, cb);
        cbQuery.where(cb.and(specPredicate, or));

        return entityManager.createQuery(cbQuery).getResultList();
    }
    // END GEOCAT

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	private Set<Integer> getGroups(final int id, final Profile profile)
			throws Exception {
        Set<Integer> hs = new HashSet<Integer>();

        if (profile == Profile.Administrator) {
            hs.addAll(groupRepository.findIds());
        } else if (profile == Profile.UserAdmin) {
			hs.addAll(userGroupRepo.findGroupIds(Specifications.where(
					hasProfile(profile)).and(hasUserId(id))));
        } else {
            hs.addAll(userGroupRepo.findGroupIds(hasUserId(id)));
        }

		return hs;
	}

	@Autowired
	private ConfigurableApplicationContext jeevesApplicationContext;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserGroupRepository userGroupRepo;
	@Autowired
	private UserRepository userRepository;

}
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.fao.geonet.repository.geocat.specification.GeocatUserSpecs.isValidated;
import static org.fao.geonet.repository.specification.UserSpecs.hasProfile;
import static org.springframework.data.jpa.domain.Specifications.not;
import static org.springframework.data.jpa.domain.Specifications.where;
    private Type type;
	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
	    this.type = Type.valueOf(params.getValue("type", "NORMAL"));
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	@RequestMapping(value = "/{lang}/admin.user.list", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	UserList exec() throws Exception {
		User me = userRepository.findOneByUsername(SecurityContextHolder
				.getContext().getAuthentication().getName());

		Set<Integer> hsMyGroups = getGroups(me.getId(), me.getProfile());

		Collection<? extends GrantedAuthority> roles = SecurityContextHolder
				.getContext().getAuthentication().getAuthorities();

		Set<String> profileSet = new HashSet<String>();

		for (GrantedAuthority rol : roles) {
			profileSet.add(rol.getAuthority());
		}

		// --- retrieve all users
		final java.util.List<User> all = userRepository.findAll(SortUtils
				.createSort(User_.username));

		// --- now filter them

		java.util.Set<Integer> usersToRemove = new HashSet<Integer>();

        if (!findingShared && !profileSet.contains(Profile.Administrator.name())) {

			for (User user : all) {
				int userId = user.getId();
				Profile profile = user.getProfile();

				// TODO is this already equivalent to ID?
				if (user.getName().equals(
						SecurityContextHolder.getContext().getAuthentication()
								.getName())) {
                    // user is permitted to access his/her own user information
                    continue;
                }
				Set<Integer> userGroups = getGroups(userId, profile);
				// Is user belong to one of the current user admin group?
				boolean isInCurrentUserAdminGroups = false;
				for (Integer userGroup : userGroups) {
					if (hsMyGroups.contains(userGroup)) {
						isInCurrentUserAdminGroups = true;
						break;
					}
				}
				// if (!hsMyGroups.containsAll(userGroups))
				if (!isInCurrentUserAdminGroups) {
					usersToRemove.add(user.getId());
                }

				if (!profileSet.contains(profile)) {
					usersToRemove.add(user.getId());
                }
			}
		}
		UserList res = new UserList();

		for (User u : Collections.unmodifiableList(all)) {
			if (!usersToRemove.contains(u.getId())) {
				res.addUser(u);
            }
        }

		
		return res;
	}

    // GEOCAT
    static java.util.List<User> makeQuery(EntityManager entityManager, boolean sortByValidated, Type type, String name) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> cbQuery = cb.createQuery(User.class);
        final Root<User> from = cbQuery.from(User.class);

        if(sortByValidated) {
            Expression<String> nameSurname = cb.trim(cb.concat(from.get(User_.name), from.get(User_.surname)));
            Expression<Object> blankNameLast = cb.selectCase(nameSurname).when("", cb.literal("zz")).otherwise(cb.lower(nameSurname));
            Expression<Object> firstValidated = cb.selectCase(from.get(User_.geocatUserInfo).get(GeocatUserInfo_
                    .jpaWorkaround_validated)).when(Constants.YN_TRUE, cb.literal(2)).otherwise(cb.literal(1));
            cbQuery.orderBy(cb.asc(blankNameLast), cb.asc(firstValidated));
        } else {
            cbQuery.orderBy(cb.asc(from.get(User_.username)));
        }

        Specifications<User> specification;
        switch (type) {
            case NON_VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(false));
                break;
            case VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(true));
                break;
            case SHARED:
                specification = where(hasProfile(Profile.Shared));
                break;
            case NORMAL:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
            default:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
        }

        // TODO : Add organisation
        Predicate usernameMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate nameMatch = cb.like(cb.lower(from.get(User_.name)), cb.lower(cb.literal('%'+name+'%')));
        Predicate surnameMatch = cb.like(cb.lower(from.get(User_.surname)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgAcrMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));

        final Predicate or = cb.or(usernameMatch, nameMatch, surnameMatch, orgMatch, orgAcrMatch);
        Expression<Boolean> specPredicate = specification.toPredicate(from, cbQuery, cb);
        cbQuery.where(cb.and(specPredicate, or));

        return entityManager.createQuery(cbQuery).getResultList();
    }
    // END GEOCAT

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	private Set<Integer> getGroups(final int id, final Profile profile)
			throws Exception {
        Set<Integer> hs = new HashSet<Integer>();

        if (profile == Profile.Administrator) {
            hs.addAll(groupRepository.findIds());
        } else if (profile == Profile.UserAdmin) {
			hs.addAll(userGroupRepo.findGroupIds(Specifications.where(
					hasProfile(profile)).and(hasUserId(id))));
        } else {
            hs.addAll(userGroupRepo.findGroupIds(hasUserId(id)));
        }

		return hs;
	}

	@Autowired
	private ConfigurableApplicationContext jeevesApplicationContext;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserGroupRepository userGroupRepo;
	@Autowired
	private UserRepository userRepository;

}
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.fao.geonet.repository.geocat.specification.GeocatUserSpecs.isValidated;
import static org.fao.geonet.repository.specification.UserSpecs.hasProfile;
import static org.springframework.data.jpa.domain.Specifications.not;
import static org.springframework.data.jpa.domain.Specifications.where;
    private Type type;
	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
	    this.type = Type.valueOf(params.getValue("type", "NORMAL"));
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	@RequestMapping(value = "/{lang}/admin.user.list", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	UserList exec() throws Exception {
		User me = userRepository.findOneByUsername(SecurityContextHolder
				.getContext().getAuthentication().getName());

		Set<Integer> hsMyGroups = getGroups(me.getId(), me.getProfile());

		Collection<? extends GrantedAuthority> roles = SecurityContextHolder
				.getContext().getAuthentication().getAuthorities();

		Set<String> profileSet = new HashSet<String>();

		for (GrantedAuthority rol : roles) {
			profileSet.add(rol.getAuthority());
		}

		// --- retrieve all users
		final java.util.List<User> all = userRepository.findAll(SortUtils
				.createSort(User_.username));

		// --- now filter them

		java.util.Set<Integer> usersToRemove = new HashSet<Integer>();

        if (!findingShared && !profileSet.contains(Profile.Administrator.name())) {

			for (User user : all) {
				int userId = user.getId();
				Profile profile = user.getProfile();

				// TODO is this already equivalent to ID?
				if (user.getName().equals(
						SecurityContextHolder.getContext().getAuthentication()
								.getName())) {
                    // user is permitted to access his/her own user information
                    continue;
                }
				Set<Integer> userGroups = getGroups(userId, profile);
				// Is user belong to one of the current user admin group?
				boolean isInCurrentUserAdminGroups = false;
				for (Integer userGroup : userGroups) {
					if (hsMyGroups.contains(userGroup)) {
						isInCurrentUserAdminGroups = true;
						break;
					}
				}
				// if (!hsMyGroups.containsAll(userGroups))
				if (!isInCurrentUserAdminGroups) {
					usersToRemove.add(user.getId());
                }

				if (!profileSet.contains(profile)) {
					usersToRemove.add(user.getId());
                }
			}
		}
		UserList res = new UserList();

		for (User u : Collections.unmodifiableList(all)) {
			if (!usersToRemove.contains(u.getId())) {
				res.addUser(u);
            }
        }

		
		return res;
	}

    // GEOCAT
    static java.util.List<User> makeQuery(EntityManager entityManager, boolean sortByValidated, Type type, String name) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> cbQuery = cb.createQuery(User.class);
        final Root<User> from = cbQuery.from(User.class);

        if(sortByValidated) {
            Expression<String> nameSurname = cb.trim(cb.concat(from.get(User_.name), from.get(User_.surname)));
            Expression<Object> blankNameLast = cb.selectCase(nameSurname).when("", cb.literal("zz")).otherwise(cb.lower(nameSurname));
            Expression<Object> firstValidated = cb.selectCase(from.get(User_.geocatUserInfo).get(GeocatUserInfo_
                    .jpaWorkaround_validated)).when(Constants.YN_TRUE, cb.literal(2)).otherwise(cb.literal(1));
            cbQuery.orderBy(cb.asc(blankNameLast), cb.asc(firstValidated));
        } else {
            cbQuery.orderBy(cb.asc(from.get(User_.username)));
        }

        Specifications<User> specification;
        switch (type) {
            case NON_VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(false));
                break;
            case VALIDATED_SHARED:
                specification = where(hasProfile(Profile.Shared)).and(isValidated(true));
                break;
            case SHARED:
                specification = where(hasProfile(Profile.Shared));
                break;
            case NORMAL:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
            default:
                specification = where(not(hasProfile(Profile.Shared)));
                break;
        }

        // TODO : Add organisation
        Predicate usernameMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate nameMatch = cb.like(cb.lower(from.get(User_.name)), cb.lower(cb.literal('%'+name+'%')));
        Predicate surnameMatch = cb.like(cb.lower(from.get(User_.surname)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));
        Predicate orgAcrMatch = cb.like(cb.lower(from.get(User_.username)), cb.lower(cb.literal('%'+name+'%')));

        final Predicate or = cb.or(usernameMatch, nameMatch, surnameMatch, orgMatch, orgAcrMatch);
        Expression<Boolean> specPredicate = specification.toPredicate(from, cbQuery, cb);
        cbQuery.where(cb.and(specPredicate, or));

        return entityManager.createQuery(cbQuery).getResultList();
    }
    // END GEOCAT

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	private Set<Integer> getGroups(final int id, final Profile profile)
			throws Exception {
        Set<Integer> hs = new HashSet<Integer>();

        if (profile == Profile.Administrator) {
            hs.addAll(groupRepository.findIds());
        } else if (profile == Profile.UserAdmin) {
			hs.addAll(userGroupRepo.findGroupIds(Specifications.where(
					hasProfile(profile)).and(hasUserId(id))));
        } else {
            hs.addAll(userGroupRepo.findGroupIds(hasUserId(id)));
        }

		return hs;
	}

	@Autowired
	private ConfigurableApplicationContext jeevesApplicationContext;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserGroupRepository userGroupRepo;
	@Autowired
	private UserRepository userRepository;

}
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.fao.geonet.repository.geocat.specification.GeocatUserSpecs.isValidated;
import static org.fao.geonet.repository.specification.UserSpecs.hasProfile;
import static org.springframework.data.jpa.domain.Specifications.not;
import static org.springframework.data.jpa.domain.Specifications.where;

// =============================================================================

