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

package org.constellation.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.constellation.cat.wrs.v100.ExtrinsicObjectType;
import org.constellation.ebrim.v250.RegistryObjectType;
import org.constellation.ebrim.v300.IdentifiableType;
import org.constellation.ows.v100.ExceptionReport;
import org.constellation.util.Util;
import org.geotools.metadata.iso.MetaDataImpl;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public class CSWClassesContext {

    private static Logger LOGGER = Logger.getLogger("org.constellation.metadata");
    /**
     * Return the list of all the marshallable classes
     *
     * @return
     */
    public static Class[] getAllClasses() {
        List<Class> classeList = new ArrayList<Class>();
        //ISO 19115 class
        classeList.add(MetaDataImpl.class);

        //ISO 19115 French profile class
        classeList.add(org.constellation.metadata.fra.ObjectFactory.class);

        //CSW 2.0.2 classes
        classeList.addAll(Arrays.asList(org.constellation.cat.csw.v202.ObjectFactory.class,
                                        ExceptionReport.class,
                                        org.constellation.ows.v110.ExceptionReport.class,  // TODO remove
                                        org.constellation.dublincore.v2.terms.ObjectFactory.class));

           //CSW 2.0.0 classes
           classeList.addAll(Arrays.asList(org.constellation.cat.csw.v200.ObjectFactory.class,
                                           org.constellation.dublincore.v1.terms.ObjectFactory.class));

           //Ebrim classes
           classeList.add(IdentifiableType.class);
           classeList.add(ExtrinsicObjectType.class);
           classeList.add(org.constellation.ebrim.v300.ObjectFactory.class);
           classeList.add(org.constellation.cat.wrs.v100.ObjectFactory.class);

           classeList.add(RegistryObjectType.class);
           classeList.add(org.constellation.ebrim.v250.ObjectFactory.class);
           classeList.add(org.constellation.cat.wrs.v090.ObjectFactory.class);

           // we add the extensions classes
           classeList.addAll(loadExtensionsClasses());



           return Util.toArray(classeList);
    }

    /**
     * Load some extensions classes (ISO 19119 and ISO 19110) if thay are present in the classPath.
     * Return a list of classes to add in the context of JAXB.
     */
    public static List<Class> loadExtensionsClasses() {
        List<Class> ExtClasses = new ArrayList<Class>();

        // if they are present in the classPath we add the ISO 19119 classes
        Class c = null;
        try {
            c = Class.forName("org.geotools.service.ServiceIdentificationImpl");
        } catch (ClassNotFoundException e) {
            LOGGER.info("ISO 19119 classes not found (optional)") ;
        }
        if (c != null) {
            ExtClasses.add(c);
            LOGGER.info("extension ISO 19119 loaded");
        }

        // if they are present in the classPath we add the ISO 19110 classes

        try {
            c = Class.forName("org.geotools.feature.catalog.AssociationRoleImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.BindingImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.BoundFeatureAttributeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.ConstraintImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.DefinitionReferenceImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.DefinitionSourceImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureAssociationImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureAttributeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureCatalogueImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureOperationImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureTypeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.InheritanceRelationImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.ListedValueImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.PropertyTypeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.util.Multiplicity");
            if (c != null) {
                ExtClasses.add(c);
            }

            LOGGER.info("extension ISO 19110 loaded");
        } catch (ClassNotFoundException e) {
            LOGGER.info("ISO 19110 classes not found (optional).");
        }
        return ExtClasses;
    }


}
