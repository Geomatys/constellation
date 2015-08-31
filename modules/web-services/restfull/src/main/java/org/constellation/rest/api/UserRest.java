/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.rest.api;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.constellation.admin.dto.metadata.Page;
import org.constellation.admin.dto.metadata.PagedSearch;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.database.api.UserWithRole;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.repository.UserRepository;
import org.constellation.security.*;
import org.geotoolkit.util.StringUtilities;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

/**
 * RestFull user configuration service
 * 
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Component
@Named
@Path("/1/user/")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class UserRest {

	@Inject
	private UserRepository userRepository;

	@Inject
	private org.constellation.security.SecurityManager securityManager;

	@GET
	@RolesAllowed("cstl-admin")
	public Response findAll(@QueryParam("withRoles") boolean withRole) {
		return Response.ok(userRepository.findActivesWithRole()).build();
	}

	/**
	 * search pageable on user
	 * @param pagedSearch
	 * @return
	 */
	@POST
	@Path("/search")
	@RolesAllowed("cstl-admin")
	public Page<UserWithRole> search(PagedSearch pagedSearch){
		String text = pagedSearch.getText(), sortFieldName = null, sortOrder = null;
		int page = pagedSearch.getPage(), size = pagedSearch.getSize();
		if(pagedSearch.getSort() != null) {
			sortFieldName = pagedSearch.getSort().getField();
			sortOrder = pagedSearch.getSort().getOrder().name();
		}

		return new Page<UserWithRole>()
				.setNumber(pagedSearch.getPage())
				.setSize(pagedSearch.getSize())
				.setContent(userRepository.search(text, size, page, sortFieldName, sortOrder))
				.setTotal(userRepository.searchCount(pagedSearch.getText()));
	}

	@PUT
	@Path("/updateValidation/{id}")
	@Transactional
	public Response updateValidation(@PathParam("id") int id){
		Optional<CstlUser> user = userRepository.findById(id);
		if(user.isPresent()){
			boolean active = user.get().getActive();
			if(active){
				if (userRepository.isLastAdmin(id)){
					return Response.serverError().entity("admin.user.last.admin").build();
				} else {
					userRepository.desactivate(id);
				}
			} else {
				userRepository.activate(id);
			}
		}
		return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
	}

	@GET
	@Path("{id}")
	public Response findOne(@PathParam("id") int id) {
		return Response.ok(userRepository.findById(id)).build();
	}

	@GET
	@Path("{id}/withRole")
	public Response findOneWithRole(@PathParam("id") int id) {
		return userRepository.findOneWithRole(id).transform(new Function<UserWithRole, Response>() {
			@Override
			public Response apply(UserWithRole userWithRole) {
				return Response.ok(userWithRole).build();
			}
		}).or(Response.status(404).build());
	}

	@GET
	@Path("/myAccount")
	public Response myAccount() {
		String currentUserLogin = securityManager.getCurrentUserLogin();
		return userRepository.findOneWithRole(currentUserLogin).transform(new Function<UserWithRole, Response>() {
			@Override
			public Response apply(UserWithRole userWithRole) {
				return Response.ok(userWithRole).build();
			}
		}).or(Response.status(404).build());
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed("cstl-admin")
	@Transactional
	public Response delete(@PathParam("id") int id) {
		if (userRepository.isLastAdmin(id))
			return Response.serverError().entity("admin.user.last.admin")
					.build();
		userRepository.desactivate(id);
		return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
	}

	/**
	 * Add user,
	 * this method take FormDataParam to support upload (logo or avatar)
	 * @param userId
	 * @param login
	 * @param email
	 * @param lastname
	 * @param firstname
	 * @param address
	 * @param additionalAddress
	 * @param zip
	 * @param city
	 * @param country
	 * @param phone
	 * @param password
	 * @return
	 */
	@POST
	@Path("/add")
	@RolesAllowed("cstl-admin")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional(rollbackFor = Exception.class)
	public Response add(@FormDataParam("userId") Integer userId,
						@FormDataParam("login") String login,
						@FormDataParam("email") String email,
						@FormDataParam("lastname") String lastname,
						@FormDataParam("firstname") String firstname,
						@FormDataParam("address") String address,
						@FormDataParam("additionalAddress") String additionalAddress,
						@FormDataParam("zip") String zip,
						@FormDataParam("city") String city,
						@FormDataParam("country") String country,
						@FormDataParam("phone") String phone,
						@FormDataParam("password") String password,
						@FormDataParam("role") String role,
						@FormDataParam("locale") String locale){

		//add user
		CstlUser user = new CstlUser();
		user.setId(null);
		user.setLogin(login);
		user.setFirstname(firstname);
		user.setLastname(lastname);
		user.setEmail(email);
		user.setActive(true);
		user.setAddress(address);
		user.setAdditionalAddress(additionalAddress);
		user.setZip(zip);
		user.setCity(city);
		user.setCountry(country);
		user.setPhone(phone);
		user.setPassword(StringUtilities.MD5encode(password));
		user.setLocale(locale);

		user = userRepository.insert(user);

		//add user to role
		userRepository.addUserToRole(user.getId(), role);

		return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
	}

	@POST
	@Path("/edit")
	@RolesAllowed("cstl-admin")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional(rollbackFor = Exception.class)
	public Response edit(@FormDataParam("userId") Integer userId,
						 @FormDataParam("login") String login,
						 @FormDataParam("email") String email,
						 @FormDataParam("lastname") String lastname,
						 @FormDataParam("firstname") String firstname,
						 @FormDataParam("address") String address,
						 @FormDataParam("additionalAddress") String additionalAddress,
						 @FormDataParam("zip") String zip,
						 @FormDataParam("city") String city,
						 @FormDataParam("country") String country,
						 @FormDataParam("phone") String phone,
						 @FormDataParam("password") String password,
						 @FormDataParam("group") Integer group,
						 @FormDataParam("role") String role,
						 @FormDataParam("locale") String locale) {
		Optional<CstlUser> optionalUser = userRepository.findById(userId);
		if(optionalUser.isPresent()){
			CstlUser user = optionalUser.get();
			user.setLogin(login);
			user.setFirstname(firstname);
			user.setLastname(lastname);
			user.setEmail(email);
			user.setAddress(address);
			user.setAdditionalAddress(additionalAddress);
			user.setZip(zip);
			user.setCity(city);
			user.setCountry(country);
			user.setPhone(phone);
			user.setLocale(locale);

			//check password update
			String newPassword = StringUtilities.MD5encode(password);
			if(password != null
					&& !password.isEmpty()
					&& !newPassword.equals(user.getPassword())){
				user.setPassword(newPassword);
			}

			userRepository.update(user);

			//add user to role
			userRepository.addUserToRole(user.getId(), role);

			return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).status(Response.Status.NOT_FOUND).build();
	}

	@POST
	@Path("/my_account")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional(rollbackFor = Exception.class)
	public Response myAccount(@FormDataParam("userId") Integer userId,
						 @FormDataParam("login") String login,
						 @FormDataParam("email") String email,
						 @FormDataParam("lastname") String lastname,
						 @FormDataParam("firstname") String firstname,
						 @FormDataParam("address") String address,
						 @FormDataParam("additionalAddress") String additionalAddress,
						 @FormDataParam("zip") String zip,
						 @FormDataParam("city") String city,
						 @FormDataParam("country") String country,
						 @FormDataParam("phone") String phone,
						 @FormDataParam("password") String password,
						 @FormDataParam("group") Integer group,
						 @FormDataParam("role") String role,
						 @FormDataParam("locale") String locale) {
		String currentUserLogin = securityManager.getCurrentUserLogin();
		if(currentUserLogin != null){
			Optional<CstlUser> optionalUser = userRepository.findOne(currentUserLogin);
			if(optionalUser.isPresent()){
				CstlUser user = optionalUser.get();
				user.setLogin(login);
				user.setFirstname(firstname);
				user.setLastname(lastname);
				user.setEmail(email);
				user.setAddress(address);
				user.setAdditionalAddress(additionalAddress);
				user.setZip(zip);
				user.setCity(city);
				user.setCountry(country);
				user.setPhone(phone);
				user.setLocale(locale);

				//check password update
				String newPassword = StringUtilities.MD5encode(password);
				if(password != null
						&& !password.isEmpty()
						&& !newPassword.equals(user.getPassword())){
					user.setPassword(newPassword);
				}

				userRepository.update(user);

				return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
			}
		}

		return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).status(Response.Status.NOT_FOUND).build();
	}

	/**
	 * Called on login. To know if login is granted to access to server
	 * 
	 * @return an {@link AcknowlegementType} on {@link Response} to know
	 *         operation state
	 */
	@GET
	@Path("access")
	public Response access() {
		final AcknowlegementType response = new AcknowlegementType("Success",
				"You have access to the configuration service");
		return Response.ok(response).build();
	}

}
