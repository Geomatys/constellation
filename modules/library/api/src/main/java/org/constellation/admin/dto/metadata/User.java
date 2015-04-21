package org.constellation.admin.dto.metadata;

import java.io.Serializable;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class User implements Serializable {
    private int id;
    private String login;
    private String email;
    private String lastname;
    private String firstname;
    private boolean active;

    private GroupBrief group;
    public User() {

    }

    public User(final int id,final String login,final String email,final String lastname,
                final String firstname,final boolean active, final GroupBrief group) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.lastname = lastname;
        this.firstname = firstname;
        this.active = active;
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public GroupBrief getGroup() {
        return group;
    }

    public void setGroup(GroupBrief group) {
        this.group = group;
    }
}
