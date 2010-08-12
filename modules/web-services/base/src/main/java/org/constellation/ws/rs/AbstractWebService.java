/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.ws.rs;

import javax.xml.bind.JAXBException;
import org.constellation.jaxb.AnchoredMarshallerPool;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractWebService extends WebService {

    /**
     * A flag indicating if the JAXBContext is properly build.
     * @Deprecated MarshallerPool must be created elsewhere
     */
    @Deprecated
    private boolean workingContext = false;

    /**
     * A pool of JAXB unmarshaller used to create Java objects from XML files.
     */
    private MarshallerPool marshallerPool;

    /**
     * The xsd schema location for all the returned xml.
     * @Deprecated should be declared directly in the marshallerPool (or object writer).
     */
    @Deprecated
    protected String schemaLocation = null;

    /**
     * {@inheritDoc }
     */
    @Override
    protected synchronized MarshallerPool getMarshallerPool() {
        return marshallerPool;
    }

    /**
     *  A flag indicating if the JAXBContext is properly build.
     */
    @Deprecated
    protected synchronized boolean isJaxBContextValid(){
        return workingContext;
    }

    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     *
     * @param packagesName A list of package containing JAXB annoted classes.
     * @param rootNamespace The main namespace for all the document.
     *
     * @Deprecated use setXMLContext(final MarshallerPool pool)
     *
     * @throws JAXBException
     */
    @Deprecated
    protected synchronized void setXMLContext(final String packagesName, final String rootNamespace) throws JAXBException {
        setXMLContext(packagesName, rootNamespace, schemaLocation);
    }

    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     *
     * @param packagesName A list of package containing JAXB annoted classes.
     * @param rootNamespace The main namespace for all the document.
     * @param schemaLocation The main xsd schema location for all the returned xml.
     * @param exceptionSchemaLocation The xsd schema location for exception report.
     *
     * @throws JAXBException
     */
    @Deprecated
    protected synchronized void setXMLContext(final String packagesName, final String rootNamespace, final String schemaLocation) throws JAXBException {
        LOGGER.finer("SETTING XML CONTEXT: class " + this.getClass().getSimpleName() +
                "\n packages: " + packagesName);
        this.schemaLocation = schemaLocation;
        try{
            marshallerPool = new AnchoredMarshallerPool(rootNamespace, packagesName, schemaLocation);
            workingContext = true;
        }catch(JAXBException ex){
            workingContext = false;
            throw ex;
        }
    }

    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     *
     * @param classesName A list of JAXB annoted classes.
     * @param schemaLocation The main xsd schema location for all the returned xml.
     * @param exceptionSchemaLocation The xsd schema location for exception report.
     * @param rootNamespace The main namespace for all the document.
     *
     * @throws JAXBException
     */
    protected synchronized void setXMLContext(final MarshallerPool pool) {
        LOGGER.finer("SETTING XML CONTEXT: marshaller Pool version");
        marshallerPool = pool;
        workingContext = true;
    }

}
