package org.constellation.dto;

import juzu.Mapped;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@Mapped
@XmlRootElement
public class Database {

    private String host = "";

    private String port = "";

    private String name = "";

    private String login = "";

    private String password = "";

    private String type = "";

    private String providerType = "";

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(final String providerType) {
        this.providerType = providerType;
    }

    @Override
    public String toString() {
        return "Database{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
