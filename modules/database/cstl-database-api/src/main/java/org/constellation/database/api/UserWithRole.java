package org.constellation.database.api;

import java.util.List;

import org.constellation.database.api.jooq.tables.pojos.CstlUser;

public class UserWithRole extends CstlUser {

	private List<String> roles;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	
	
}
