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

package org.constellation;

/**
 * All the services known by Constellation.
 * <p>
 * The contents of this class should be self explanatory.
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public enum ServiceDef {
	
	WMS_1_0_0(Specification.WMS, Organization.OGC, "1.0.0", Profile.NONE),
	WMS_1_1_1(Specification.WMS, Organization.OGC, "1.1.1", Profile.NONE),
	WMS_1_3_0(Specification.WMS, Organization.OGC, "1.3.0", Profile.NONE),
	
	WMS_1_0_0_SLD(Specification.WMS, Organization.OGC, "1.0.0", Profile.WMS_SLD),
	WMS_1_1_1_SLD(Specification.WMS, Organization.OGC, "1.1.1", Profile.WMS_SLD),
	WMS_1_3_0_SLD(Specification.WMS, Organization.OGC, "1.3.0", Profile.WMS_SLD),
	
	WCS_1_0_0(Specification.WCS, Organization.OGC, "1.0.0", Profile.NONE),
	WCS_1_1_0(Specification.WCS, Organization.OGC, "1.1.0", Profile.NONE),
	WCS_1_1_1(Specification.WCS, Organization.OGC, "1.1.1", Profile.NONE),
	WCS_1_1_2(Specification.WCS, Organization.OGC, "1.1.2", Profile.NONE),
	
	CSW_2_0_2(Specification.CSW, Organization.OGC, "2.0.2", Profile.CSW_ISO),

    SOS_1_0_0(Specification.SOS, Organization.OGC, "1.0.0", Profile.NONE),
	
	PEP(Specification.PEP, Organization.OASIS, null, Profile.NONE),
	PDP(Specification.PDP, Organization.OASIS, null, Profile.NONE);
	
	
	
	public final Specification specification;
	public final Organization  organization;
	public final Version       version;
	public final Profile       profile;
	
	private ServiceDef(Specification spec, Organization org, String verStr, Profile prof){
		specification = spec;
		organization = org;
		version = (verStr == null) ? null : new Version(verStr);
		profile = prof;
	}
	
	public static class Version extends org.geotools.util.Version {
		
		private static final long serialVersionUID = -1004484794380489333L;
		
		public Version(String versionDef) {
			super(versionDef);
		}
	}
	
	public int compareTo(String str){
		return version.compareTo(new Version(str));
	}
	
    @Override
	public String toString(){
		return specification.name() + ", v." + version + ", profile (" + profile.name() + ")";
	}
	
	public enum Specification {
		
		NONE("None"),
		CSW("Catalog Service for the Web"),
		SOS("Sensor Observation Service"),
		WCS("Web Coverage Service"),
		WMS("Web Map Service"), 
		PEP("Policy Enforcement Point"), 
		PDP("Policy Decision Point");
		
		public final String fullName;
		
		private Specification(String full){
			fullName     = full;
		}
	}
	
	public enum Organization {

		NONE  ("None"),
		OASIS ("The Organization for the Advancement of Structured Information Standards"),
		OGC   ("The Open Geospatial Consortium"),
		W3C   ("The World Wide Web Consortium");
		
		public final String fullName;
		
		private Organization(String full){
			fullName     = full;
		}
	}
	
	public enum Profile {
		
		NONE    ("None", new Version(""), Organization.NONE),
        CSW_ISO ("Catalog Services for the Web, ISO profile", new Version("1.0.0"), Organization.OGC),
		WMS_SLD ("Styled Layer Descriptor profile of the Web Map Service", 
				   new Version("1.1.0"), Organization.OGC);
		
		public final String fullName;
		public final Version version;
		public final Organization organization;
		
		private Profile(String full, Version ver, Organization org){
			fullName     = full;
			version      = ver;
			organization = org;
		}
	}
}
