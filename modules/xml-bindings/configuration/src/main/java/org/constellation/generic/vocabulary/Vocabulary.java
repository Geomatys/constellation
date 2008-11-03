/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.generic.vocabulary;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * XML binding for vocabulary list configuration file.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "vocabulary")
public class Vocabulary {

    /**
     * The SDN prefix for the codes of this vocabulary.
     * Pattern : 'SDN:' + 'vocabulary code' + ':' + 'version number' + ':'
     */ 
    @XmlAttribute
    private String list;
    
    /**
     * The version number of this vocabulary.
     */
    @XmlAttribute
    private String version;
    
    /**
     * The date of edition of the version.
     */
    @XmlAttribute
    private Date date;
    
    /**
     * The title of the vocabulary
     */
    @XmlAttribute
    private String title;
    
    private List<Keyword> keyword;
    
    @XmlTransient
    private Map<String, String> map;
    

    public Vocabulary() {
    }
    
    public Vocabulary(String list, String version, String title, Date date, List<Keyword> keyword) {
        this.date    = date;
        this.keyword = keyword;
        this.list    = list;
        this.version = version;
        this.title   = title;
        fillMap();
    }
    
    public void fillMap() {
        map = new HashMap<String, String>();
        for (Keyword kw : keyword) {
            String id = kw.getSDNIdent();
            id = id.substring(id.lastIndexOf(':'));
            getMap().put(id, kw.getValue());
        }
    }
    
    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Keyword> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<Keyword> keyword) {
        this.keyword = keyword;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("[Vocabulary]").append('\n');
        sb.append("title: ").append(title).append('\n');
        sb.append("list: ").append(list).append('\n');
        sb.append("version: ").append(version).append('\n');
        sb.append("date: ").append(date).append('\n');
        if (keyword != null) {
            sb.append("keywords: ").append('\n');
            for (Keyword kw : keyword)
                sb.append(kw).append('\n');
        }
        return sb.toString();    
            
    }

    public Map<String, String> getMap() {
        return map;
    }
}
