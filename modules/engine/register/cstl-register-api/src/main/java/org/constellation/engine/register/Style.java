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
package org.constellation.engine.register;

public class Style {

    private String owner;
    private String body;
    private long date;
    private String type;
    private int provider;
    private String name;
    private int id;

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
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


    public void setProvider(int provider) {
        this.provider = provider;
    }


    public int getProvider() {
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
