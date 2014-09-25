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
package org.constellation.json.metadata;

import org.geotoolkit.metadata.MetadataFactory;
import org.apache.sis.metadata.MetadataStandard;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.AbstractProcess;
import org.geotoolkit.sml.xml.SMLMember;
import org.geotoolkit.sml.xml.v101.SensorML;
import org.geotoolkit.sml.xml.v101.SystemType;
import org.geotoolkit.sml.xml.v101.ComponentType;
import org.apache.sis.metadata.iso.citation.DefaultCitation;


/**
 * A metadata standard for the {@link org.geotoolkit.sml.xml} package.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class SensorMLStandard extends MetadataStandard {
    /**
     * The singleton instance for system and component Sensor ML.
     */
    static final MetadataStandard SYSTEM, COMPONENT;
    static {
        final MetadataStandard swe = new SensorMLStandard("SWE", // A dependency of SensorML.
                org.geotoolkit.swe.xml.Position.class.getPackage(), false);
        final Package pck = AbstractSensorML.class.getPackage();
        SYSTEM    = new SensorMLStandard("System SML",    pck, true,  swe);
        COMPONENT = new SensorMLStandard("Component SML", pck, false, swe);
    }

    /**
     * The package name for the sensor ML version to implement.
     */
    private static final String VERSION = "v101";

    /**
     * The prefix to omit from interface name.
     */
    private static final String PREFIX = "Abstract";

    /**
     * {@code true} for system Sensor ML, or {@code false} for component Sensor ML.
     */
    private final boolean system;

    /**
     * The metadata factory to use for creating new instances.
     */
    final MetadataFactory factory;

    /**
     * Constructor for the singleton instance.
     */
    private SensorMLStandard(final String name, final Package pck, final boolean system,
            final MetadataStandard... dependencies)
    {
        super(new DefaultCitation(name), pck, dependencies);
        this.system = system;
        factory = new MetadataFactory(this);
    }

    /**
     * Returns the implementation class for the given interface, or {@code null} if none.
     *
     * @param  <T>  The compile-time {@code type}.
     * @param  type The interface from the {@code org.geotoolkit.sml.xml} package.
     * @return The implementation class, or {@code null} if none.
     */
    @Override
    public <T> Class<? extends T> getImplementation(final Class<T> type) {
        if (!type.isInterface()) {
            return null;
        }
        Class<?> impl;
        if (type == SMLMember.class) {
            impl = SensorML.Member.class;
        } else if (type == AbstractProcess.class) {
            impl = system ? SystemType.class : ComponentType.class;
        } else {
            final String interfaceName = type.getName();
            final int s = interfaceName.lastIndexOf('.') + 1;
            final StringBuilder implName = new StringBuilder(interfaceName);
            if (interfaceName.regionMatches(s, PREFIX, 0, PREFIX.length())) {
                implName.delete(s, s + PREFIX.length());
            }
            implName.insert(s, VERSION + '.');
            try {
                impl = Class.forName(implName.toString());
            } catch (ClassNotFoundException e) {
                implName.append("PropertyType");
                try {
                    impl = Class.forName(implName.toString());
                } catch (ClassNotFoundException e2) {
                    return null;
                }
            }
        }
        return impl.asSubclass(type);
    }
}
