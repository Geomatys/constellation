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

package org.constellation.generic.filter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "paramSql"
})
@XmlRootElement(name = "params_sql")
public class ParamsSql {

    @XmlElement(name = "param_sql")
    private List<ParamSql> paramSql;

    /**
     * Gets the value of the paramSql property.
     */
    public List<ParamSql> getParamSql() {
        if (paramSql == null) {
            paramSql = new ArrayList<ParamSql>();
        }
        return this.paramSql;
    }

     @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ParamsSql]:").append('\n');
        if (paramSql != null) {
            sb.append("paramsql: ").append('\n');
            for (ParamSql s : paramSql) {
                sb.append(s).append('\n');
            }
        }
        return sb.toString();
     }

}
