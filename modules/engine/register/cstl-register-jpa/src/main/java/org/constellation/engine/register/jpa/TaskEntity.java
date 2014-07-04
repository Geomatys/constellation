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
package org.constellation.engine.register.jpa;

import org.constellation.engine.register.Task;
import org.constellation.engine.register.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;



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
