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
package org.constellation.configuration;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Cédric Briançon (Geomatys)
 */
@XmlRootElement(name="BriefNodeList")
@XmlAccessorType(XmlAccessType.FIELD)
public class BriefNodeList {
    @XmlElement(name = "BriefNode")
    private Collection<BriefNode> list;

    public BriefNodeList() {

    }

    public BriefNodeList(final Collection<BriefNode> list) {
        this.list = list;
    }

    public Collection<BriefNode> getList() {
        if(list == null){
            list = new ArrayList<>();
        }
        return list;
    }

    public void setList(final Collection<BriefNode> list) {
        this.list = list;
    }

    public boolean isEmpty() {
        return getList().isEmpty();
    }

    public int size() {
        return getList().size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[BriefNodeList]:\n");
        if (list != null) {
            for (BriefNode n : list) {
                sb.append(n).append(",");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BriefNodeList) {
            final BriefNodeList that = (BriefNodeList) obj;
            return Objects.equals(this.list, that.list);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.list != null ? this.list.hashCode() : 0);
        return hash;
    }
}
