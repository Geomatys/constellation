/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

import org.constellation.configuration.HarvestTasks;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.constellation.util.Util;

import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.geotoolkit.ebrim.xml.v300.RegistryObjectType;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.wrs.xml.v100.ExtrinsicObjectType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public final class CSWClassesContext {

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
    public static List<Class> getAllClassesList() {
        final List<Class> classeList = new ArrayList<Class>();

        // configuration classes
        classeList.add(HarvestTasks.class);

        //ISO 19115 class
        classeList.add(DefaultMetadata.class);

        //ISO 19115 French profile class
        classeList.addAll(FRA_CLASSES);

        // Inspire classes
        classeList.add(org.geotoolkit.inspire.xml.ObjectFactory.class);

        // xsd classes classes
        classeList.add(org.geotoolkit.xsd.xml.v2001.ObjectFactory.class);

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
           // we add the extensions classes
           classeList.add(org.geotoolkit.service.ServiceIdentificationImpl.class);
           classeList.addAll(Arrays.asList(org.geotoolkit.feature.catalog.AssociationRoleImpl.class,
                                           org.geotoolkit.feature.catalog.BindingImpl.class,
                                           org.geotoolkit.feature.catalog.BoundFeatureAttributeImpl.class,
                                           org.geotoolkit.feature.catalog.ConstraintImpl.class,
                                           org.geotoolkit.feature.catalog.DefinitionReferenceImpl.class,
                                           org.geotoolkit.feature.catalog.DefinitionSourceImpl.class,
                                           org.geotoolkit.feature.catalog.FeatureAssociationImpl.class,
                                           org.geotoolkit.feature.catalog.FeatureAttributeImpl.class,
                                           org.geotoolkit.feature.catalog.FeatureCatalogueImpl.class,
                                           org.geotoolkit.feature.catalog.FeatureOperationImpl.class,
                                           org.geotoolkit.feature.catalog.FeatureTypeImpl.class,
                                           org.geotoolkit.feature.catalog.InheritanceRelationImpl.class,
                                           org.geotoolkit.feature.catalog.ListedValueImpl.class,
                                           org.geotoolkit.feature.catalog.PropertyTypeImpl.class,
                                           org.geotoolkit.util.Multiplicity.class
                                           ));



           return classeList;
    }

    /**
     * Return the list of all the marshallable classes
     *
     * @return
     */
    public static Class[] getAllClasses() {
        List<Class> classes =getAllClassesList();
        return classes.toArray(new Class[classes.size()]);
    }

}
