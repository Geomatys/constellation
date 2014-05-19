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


package org.constellation.generic.database;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each Java content interface and Java element interface 
 * generated in the generated package. 
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. 
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding of schema 
 * type definitions, element declarations and model groups.  
 * Factory methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.constellation.generic.database
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@Automatic Automatic }
     * 
     */
    public Automatic createAutomatic() {
        return new Automatic();
    }
    
    /**
     * Create an instance of {@BDD BDD }
     * 
     */
    public BDD createBDD() {
        return new BDD();
    }
    
    /**
     * Create an instance of {@link Where }
     * 
     */
    public Where createWhere() {
        return new Where();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }
    
    /**
     * Create an instance of {@link Queries }
     * 
     */
    public Queries createQueries() {
        return new Queries();
    }

    /**
     * Create an instance of {@link Select }
     * 
     */
    public Select createSelect() {
        return new Select();
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

    /**
     * Create an instance of {@link Groupby }
     *
     */
    public Groupby createGroupby() {
        return new Groupby();
    }
}
