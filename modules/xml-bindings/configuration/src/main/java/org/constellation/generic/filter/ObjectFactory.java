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

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the generated package. 
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding of schema
 * type definitions, element declarations and model groups.
 * Factory methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link In }
     * 
     */
    public In createIn() {
        return new In();
    }

    /**
     * Create an instance of {@link Where }
     * 
     */
    public Where createWhere() {
        return new Where();
    }

    /**
     * Create an instance of {@link Link }
     * 
     */
    public Link createLink() {
        return new Link();
    }

    /**
     * Create an instance of {@link Groupby }
     * 
     */
    public Groupby createGroupby() {
        return new Groupby();
    }

    /**
     * Create an instance of {@link NestedSelect }
     * 
     */
    public NestedSelect createNestedSelect() {
        return new NestedSelect();
    }

    /**
     * Create an instance of {@link Complement }
     * 
     */
    public Complement createComplement() {
        return new Complement();
    }

    /**
     * Create an instance of {@link ParamsSql }
     * 
     */
    public ParamsSql createParamsSql() {
        return new ParamsSql();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link ParamSql }
     * 
     */
    public ParamSql createParamSql() {
        return new ParamSql();
    }

    /**
     * Create an instance of {@link Union }
     * 
     */
    public Union createUnion() {
        return new Union();
    }

    /**
     * Create an instance of {@link Select }
     * 
     */
    public Select createSelect() {
        return new Select();
    }

    /**
     * Create an instance of {@link Subselect }
     * 
     */
    public Subselect createSubselect() {
        return new Subselect();
    }

    /**
     * Create an instance of {@link Subquery }
     * 
     */
    public Subquery createSubquery() {
        return new Subquery();
    }

    /**
     * Create an instance of {@link From }
     * 
     */
    public From createFrom() {
        return new From();
    }

    /**
     * Create an instance of {@link Orderby }
     * 
     */
    public Orderby createOrderby() {
        return new Orderby();
    }

}
