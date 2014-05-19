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
package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Task;
import org.constellation.engine.register.User;



@Entity
@Table(schema = "`admin`", name = "`task`")
public class TaskEntity implements Task {

    @Id
    @Column(name = "`identifier`")
    private String identifier;

    @Column(name = "`state`")
    private String state;

    @Column(name = "`type`")
    private String type;

    @Column(name = "`title`")
    private int title;

    @Column(name = "`description`")
    private int description;

    @Column(name = "`start`")
    private long start;

    @Column(name = "`end`")
    private int end;
    
    @ManyToOne(targetEntity=UserEntity.class)
    @JoinColumn(name="`owner`")
    private User owner;

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getTitle() {
        return title;
    }

    @Override
    public void setTitle(int title) {
        this.title = title;
    }

    @Override
    public int getDescription() {
        return description;
    }

    @Override
    public void setDescription(int description) {
        this.description = description;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public void setStart(long start) {
        this.start = start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

}
