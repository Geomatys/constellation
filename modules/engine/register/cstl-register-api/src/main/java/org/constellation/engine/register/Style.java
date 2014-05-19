/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.engine.register;

public class Style {

    private User owner;
    private String body;
    private int description;
    private int title;
    private long date;
    private String type;
    private Provider provider;
    private String name;
    private int id;

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }


    public void setDescription(int description) {
        this.description = description;
    }


    public int getDescription() {
        return description;
    }


    public void setTitle(int title) {
        this.title = title;
    }


    public int getTitle() {
        return title;
    }


    public void setDate(long date) {
        this.date = date;
    }


    public long getDate() {
        return date;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }


    public void setProvider(Provider provider) {
        this.provider = provider;
    }


    public Provider getProvider() {
        return provider;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return this.name;
    }


    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return this.id;
    }

}
