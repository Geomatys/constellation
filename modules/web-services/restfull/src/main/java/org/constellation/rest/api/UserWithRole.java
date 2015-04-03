package org.constellation.rest.api;

import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.CstlUser;

public class UserWithRole extends CstlUser{

	private List<String> roles;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	
	
}
