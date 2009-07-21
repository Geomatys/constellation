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

import org.constellation.configuration.HarvestTasks;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.constellation.util.Util;

import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.geotoolkit.ebrim.xml.v300.RegistryObjectType;
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.wrs.xml.v100.ExtrinsicObjectType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public class CSWClassesContext {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");

    private CSWClassesContext() {}
    
    /**
     * List of classes for the french profile of metadata.
     */
    public static final List<Class> FRA_CLASSES = new ArrayList<Class>();

    static {
        FRA_CLASSES.addAll(Arrays.asList(
                org.geotoolkit.metadata.fra.FRA_Constraints.class,
                org.geotoolkit.metadata.fra.FRA_DataIdentification.class,
                org.geotoolkit.metadata.fra.FRA_DirectReferenceSystem.class,
                org.geotoolkit.metadata.fra.FRA_IndirectReferenceSystem.class,
                org.geotoolkit.metadata.fra.FRA_LegalConstraints.class,
                org.geotoolkit.metadata.fra.FRA_SecurityConstraints.class));
    }

    /**
     * Return the list of all the marshallable classes
     *
     * @return
     */
    public static Class[] getAllClasses() {
        final List<Class> classeList = new ArrayList<Class>();

        // configuration classes
        classeList.add(HarvestTasks.class);

        //ISO 19115 class
        classeList.add(DefaultMetaData.class);

        //ISO 19115 French profile class
        classeList.addAll(FRA_CLASSES);

        // Inspire classes
        classeList.add(org.geotoolkit.inspire.xml.ObjectFactory.class);
        //CSW 2.0.2 classes
        classeList.addAll(Arrays.asList(org.geotoolkit.csw.xml.v202.ObjectFactory.class,
                                        ExceptionReport.class,
                                        org.geotoolkit.ows.xml.v110.ExceptionReport.class,  // TODO remove
                                        org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.class));

           //CSW 2.0.0 classes
           classeList.addAll(Arrays.asList(org.geotoolkit.csw.xml.v200.ObjectFactory.class,
                                           org.geotoolkit.dublincore.xml.v1.terms.ObjectFactory.class));

           //Ebrim classes
           classeList.add(IdentifiableType.class);
           classeList.add(ExtrinsicObjectType.class);
           classeList.add(org.geotoolkit.ebrim.xml.v300.ObjectFactory.class);
           classeList.add(org.geotoolkit.wrs.xml.v100.ObjectFactory.class);

           classeList.add(RegistryObjectType.class);
           classeList.add(org.geotoolkit.ebrim.xml.v250.ObjectFactory.class);
           classeList.add(org.geotoolkit.wrs.xml.v090.ObjectFactory.class);

           // we add the extensions classes
           classeList.addAll(loadExtensionsClasses());



           return Util.toArray(classeList);
    }

    /**
     * Load some extensions classes (ISO 19119 and ISO 19110) if thay are present in the classPath.
     * Return a list of classes to add in the context of JAXB.
     */
    public static List<Class> loadExtensionsClasses() {
        final List<Class> extClasses = new ArrayList<Class>();

        // if they are present in the classPath we add the ISO 19119 classes
        Class c = null;
        try {
            c = Class.forName("org.geotools.service.ServiceIdentificationImpl");
        } catch (ClassNotFoundException e) {
            LOGGER.info("ISO 19119 classes not found (optional)") ;
        }
        if (c != null) {
            extClasses.add(c);
            LOGGER.info("extension ISO 19119 loaded");
        }

        // if they are present in the classPath we add the ISO 19110 classes

        try {
            c = Class.forName("org.geotools.feature.catalog.AssociationRoleImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.BindingImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.BoundFeatureAttributeImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.ConstraintImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.DefinitionReferenceImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.DefinitionSourceImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureAssociationImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureAttributeImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureCatalogueImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureOperationImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureTypeImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.InheritanceRelationImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.ListedValueImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.PropertyTypeImpl");
            if (c != null) {
                extClasses.add(c);
            }

            c = Class.forName("org.geotools.util.Multiplicity");
            if (c != null) {
                extClasses.add(c);
            }

            LOGGER.info("extension ISO 19110 loaded");
        } catch (ClassNotFoundException e) {
            LOGGER.info("ISO 19110 classes not found (optional).");
        }
        return extClasses;
    }


}
