package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.constellation.engine.register.User;

@Entity
@Table(schema="`admin`", name="`user`")
public class UserEntity implements User {

    @Id
    @Column(name="`login`")
    private String login;
    
    @Column(name="`password`")
    private String password;
    
    @Column(name="`name`")
    private String name;

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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User [login=" + login + ", password=" + password + ", name=" + name + "]";
    }

    
    
}
