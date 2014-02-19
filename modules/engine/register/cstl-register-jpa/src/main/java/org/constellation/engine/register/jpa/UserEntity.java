package org.constellation.engine.register.jpa;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.constellation.engine.register.Role;
import org.constellation.engine.register.User;

@Entity
@Table(schema = "`admin`", name = "`user`")
public class UserEntity implements User {

    @Id
    @Column(name = "`login`")
    private String login;

    @Column(name = "`password`")
    private String password;

    @Column(name = "`lastname`")
    private String lastname;

    @Column(name = "`firstname`")
    private String firstname;

    @Column(name = "`email`")
    private String email;

    
    @ManyToMany(targetEntity = RoleEntity.class)
    @JoinTable(schema="`admin`", name = "`user_x_role`", joinColumns = { @JoinColumn(name = "`login`", referencedColumnName = "`login`"),

    }, inverseJoinColumns = { @JoinColumn(name = "`role`", referencedColumnName = "`name`") })
    private List<Role> roles;

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserEntity [login=" + login + ", password=" + password + ", lastname=" + lastname + ", firstname="
                + firstname + ", email=" + email + "]";
    }
    
    
    

}
