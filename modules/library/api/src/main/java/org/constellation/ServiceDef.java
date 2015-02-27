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
package org.constellation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * All the services known by Constellation.
 * <p>
 * The contents of this class should be self explanatory.
 *
 * @author Adrian Custer (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public enum ServiceDef {
    // WMS service definitions

    WMS_1_0_0(Specification.WMS, Organization.OGC, "1.0.0", Profile.NONE, "1.1.0", false, false),
    WMS_1_1_1(Specification.WMS, Organization.OGC, "1.1.1", Profile.NONE, "1.1.1", false, true),
    WMS_1_3_0(Specification.WMS, Organization.OGC, "1.3.0", Profile.NONE, "1.3.0", false, true),
    // WMS with SLD profiles definitions
    WMS_1_0_0_SLD(Specification.WMS, Organization.OGC, "1.0.0", Profile.WMS_SLD, "1.1.0", false, false),
    WMS_1_1_1_SLD(Specification.WMS, Organization.OGC, "1.1.1", Profile.WMS_SLD, "1.1.1", false, true),
    WMS_1_3_0_SLD(Specification.WMS, Organization.OGC, "1.3.0", Profile.WMS_SLD, "1.3.0", false, true),
    // WMTS service definition
    WMTS_1_0_0(Specification.WMTS, Organization.OGC, "1.0.0", Profile.NONE, "1.1.0", true, true),
    // WCS service definitions
    WCS_1_0_0(Specification.WCS, Organization.OGC, "1.0.0", Profile.NONE, "1.2.0", false, true),
    WCS_1_1_0(Specification.WCS, Organization.OGC, "1.1.0", Profile.NONE, "1.1.0", true, true),
    WCS_1_1_1(Specification.WCS, Organization.OGC, "1.1.1", Profile.NONE, "1.1.0", true, true),
    WCS_1_1_2(Specification.WCS, Organization.OGC, "1.1.2", Profile.NONE, "1.1.0", true, false),
    WCS_2_0_0(Specification.WCS, Organization.OGC, "2.0.0", Profile.NONE, "2.0.0", true, true),
    // WFS service definitions
    WFS_1_0_0(Specification.WFS, Organization.OGC, "1.0.0", Profile.NONE, "1.0.0", true, false),
    WFS_1_1_0(Specification.WFS, Organization.OGC, "1.1.0", Profile.NONE, "1.0.0", true, true),
    WFS_2_0_0(Specification.WFS, Organization.OGC, "2.0.0", Profile.NONE, "2.0.0", true, true),
    // CSW service definition
    CSW_2_0_0(Specification.CSW, Organization.OGC, "2.0.0", Profile.CSW_ISO, "1.2.0", true, false),
    CSW_2_0_2(Specification.CSW, Organization.OGC, "2.0.2", Profile.CSW_ISO, "1.2.0", true, true),
    // Configuration service definition (custom service of Geomatys)
    CONFIG(Specification.NONE, Organization.NONE, null, Profile.NONE, "1.0", false, true),
    // SOS service definition
    SOS_1_0_0(Specification.SOS, Organization.OGC, "1.0.0", Profile.NONE, "1.1.0", true, true),
    SOS_2_0_0(Specification.SOS, Organization.OGC, "2.0.0", Profile.NONE, "1.1.0", true, true),
    // Security services definitions
    PEP(Specification.PEP, Organization.OASIS, null, Profile.NONE, null, false, true),
    PDP(Specification.PDP, Organization.OASIS, null, Profile.NONE, null, false, true),
    // MDWeb services definitions
    MDW(Specification.MDW, Organization.NONE, null, Profile.NONE, null, false, true),
    // Thesaurus services definitions
    THW(Specification.THW, Organization.NONE, null, Profile.NONE, null, false, true),

    //WPS services definitions
    WPS_1_0_0(Specification.WPS, Organization.OGC, "1.0.0", Profile.NONE, "1.1.0", true, true);


    /**
     * Name of the specification.
     */
    public final Specification specification;
    /**
     * Organization which owns the specification.
     */
    public final Organization organization;
    /**
     * Version of the specification.
     */
    public final Version version;
    /**
     * Defines the profile applied on a specification, or {@link Profile#NONE} if none.
     */
    public final Profile profile;
    /**
     * Version of the exception report.
     */
    public final Version exceptionVersion;

    /**
     * {@code true} if the service is a OWS service.
     */
    public final boolean owsCompliant;

    /**
     * {@code true} if the service is the service version in implemented in constellation.
     */
    public final boolean isImplemented;

    /**
     * Defines a web service by its name, organization owner, profile, version and version
     * of the exception type returned.
     *
     * @param spec      The name of the specification.
     * @param org       The organisation owner of the specification.
     * @param verStr    The version of the service. Can be {@code null}.
     * @param prof      The profile of this service, or {@link Profile#NONE} if none.
     * @param excVerStr The version of the exception report, or {@link Profile#NONE} if none.
     */
    private ServiceDef(Specification spec, Organization org, String verStr, Profile prof, String excVerStr, boolean ows, boolean isImplemented) {
        specification = spec;
        organization = org;
        version = (verStr == null) ? null : new Version(verStr);
        profile = prof;
        exceptionVersion = (excVerStr == null) ? null : new Version(excVerStr);
        this.owsCompliant = ows;
        this.isImplemented = isImplemented;
    }

    public static class Version extends org.apache.sis.util.Version {

        private static final long serialVersionUID = -1004484794380489333L;

        public Version(String versionDef) {
            super(versionDef);
        }
    }

    /**
     * Compare the current version with another version number.
     *
     * @param str A string containing a version number.
     *
     * @see org.apache.sis.util.Version#compareTo(org.apache.sis.util.Version)
     *
     * @return A negative value if this version is lower than the supplied version, a positive
     *         value if it is higher, or 0 if they are equal.
     */
    public int compareTo(String str) {
        return version.compareTo(new Version(str));
    }

    /**
     * Try to find a {@link ServiceDef} matching with the specification name and the version
     * specified. If there is no match, returns {@code null}.
     *
     * @param spec    The specification name.
     * @param version The version.
     * @return The {@link ServiceDef} for the spec and version chosen, or {@code null}.
     */
    public static ServiceDef getServiceDefinition(final String spec, final String version) {
        for (ServiceDef service : values()) {
            if (service.version == null || service.specification == null) {
                continue;
            }
            if (service.version.toString().equalsIgnoreCase(version)
                    && service.specification.toString().equalsIgnoreCase(spec)) {
                return service;
            }
        }
        return null;
    }

    public static ServiceDef getServiceDefinition(final Specification spec, final String version) {
        for (ServiceDef service : values()) {
            if (service.version == null || service.specification == null) {
                continue;
            }
            if (service.version.toString().equalsIgnoreCase(version)
                    && service.specification.equals(spec)) {
                return service;
            }
        }
        return null;
    }
    
    public static List<ServiceDef> getAllSupportedVersionForSpecification(final Specification spec) {
        final List<ServiceDef> results = new ArrayList<>();
        for (ServiceDef service : values()) {
            if (service.version == null || service.specification == null) {
                continue;
            }
            if (service.specification.equals(spec) && service.isImplemented) {
                results.add(service);
            }
        }
        Collections.sort(results, new ServiceDefVersionComparator());
        return results;
    }

    @Override
    public String toString() {
        return specification.name() + ", v." + version + ", profile (" + profile.name()
                + "), org. " + organization + ", exception version " + exceptionVersion;
    }

    public enum Specification {

        NONE("None"),
        CSW("Catalog Service for the Web"),
        SOS("Sensor Observation Service"),
        WCS("Web Coverage Service"),
        WPS("Web Processing Service"),
        WFS("Web Feature Service"),
        WMS("Web Map Service"),
        WMTS("Web Map Tile Service"),
        PEP("Policy Enforcement Point"),
        PDP("Policy Decision Point"),
        MDW("MDWeb"),
        THW("Thesaurus"),
        WEBDAV("Webdav");

        public final String fullName;

        private Specification(String full) {
            fullName = full;
        }

        public static Specification fromShortName(final String shortName) {
            if (NONE.name().equalsIgnoreCase(shortName)) {
                return NONE;
            } else if (CSW.name().equalsIgnoreCase(shortName)) {
                return CSW;
            } else if (SOS.name().equalsIgnoreCase(shortName)) {
                return SOS;
            } else if (WCS.name().equalsIgnoreCase(shortName)) {
                return WCS;
            } else if (WPS.name().equalsIgnoreCase(shortName)) {
                return WPS;
            } else if (WFS.name().equalsIgnoreCase(shortName)) {
                return WFS;
            } else if (WMS.name().equalsIgnoreCase(shortName)) {
                return WMS;
            } else if (WMTS.name().equalsIgnoreCase(shortName)) {
                return WMTS;
            } else if (PEP.name().equalsIgnoreCase(shortName)) {
                return PEP;
            } else if (PDP.name().equalsIgnoreCase(shortName)) {
                return PDP;
            } else if (MDW.name().equalsIgnoreCase(shortName)) {
                return MDW;
            } else if (THW.name().equalsIgnoreCase(shortName)) {
                return THW;
            } else if (WEBDAV.name().equalsIgnoreCase(shortName)) {
                return WEBDAV;
            }
            throw new IllegalArgumentException(shortName + " is not a valid service specification.");
        }

        public boolean supported() {
            return this.equals(Specification.WMS)  ||this.equals(Specification.WMTS)
                 ||this.equals(Specification.WFS)  ||this.equals(Specification.CSW)
                 ||this.equals(Specification.WCS)  ||this.equals(Specification.SOS)
                 ||this.equals(Specification.WPS)  ||this.equals(Specification.WEBDAV) ;
        }

        public boolean supportedWXS() {
            return this.equals(Specification.WMS)  ||this.equals(Specification.WMTS)
                 ||this.equals(Specification.WFS)  ||this.equals(Specification.WCS);
        }
    }

    public enum Organization {

        NONE("None"),
        OASIS("The Organization for the Advancement of Structured Information Standards"),
        OGC("The Open Geospatial Consortium"),
        W3C("The World Wide Web Consortium");
        public final String fullName;

        private Organization(String full) {
            fullName = full;
        }
    }

    public enum Profile {

        NONE("None", new Version(""), Organization.NONE),
        CSW_ISO("Catalog Services for the Web, ISO profile", new Version("1.0.0"), Organization.OGC),
        WMS_SLD("Styled Layer Descriptor profile of the Web Map Service",
        new Version("1.1.0"), Organization.OGC);
        public final String fullName;
        public final Version version;
        public final Organization organization;

        private Profile(String full, Version ver, Organization org) {
            fullName = full;
            version = ver;
            organization = org;
        }
    }

    public enum Query {
        WMS_ALL(Specification.WMS),
        WMS_GETMAP(Specification.WMS),
        WMS_GETINFO(Specification.WMS),

        WMTS_ALL(Specification.WMTS),

        WCS_ALL(Specification.WCS),

        WFS_ALL(Specification.WFS),

        CSW_ALL(Specification.CSW),

        MDW_ALL(Specification.MDW),

        NONE_ALL(Specification.NONE),

        PDP_ALL(Specification.PDP),

        PEP_ALL(Specification.PEP),

        SOS_ALL(Specification.SOS);


        public final Specification specification;

        private Query(Specification specification){
            this.specification = specification;
        }
    }


    private static class ServiceDefVersionComparator implements Comparator<ServiceDef> {

        @Override
        public int compare(ServiceDef o1, ServiceDef o2) {
            return o2.version.toString().compareTo(o1.version.toString());
        }

    }
}