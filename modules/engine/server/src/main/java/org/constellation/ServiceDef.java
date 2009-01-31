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
 * <b>WARNING</b><br/>
 * This class duplicates versionning information which is held elsewhere; this 
 * will probably die a sudden death in the near future once we can review the 
 * various systems in place.
 * <p>
 * The contents of this class should be self explanatory.
 * <p>
 * TODO: Explore the versioning system and its uses.
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public enum ServiceDef {
	
	WMS_1_0_0(Specification.WMS,Organization.OGC,new Version("1.0.0"), Profile.NONE),
	WMS_1_3_0(Specification.WMS,Organization.OGC,new Version("1.3.0"), Profile.NONE),
	
	WMS_1_0_0_SLD(Specification.WMS,Organization.OGC,new Version("1.0.0"), Profile.WMS_SLD),
	WMS_1_1_1_SLD(Specification.WMS,Organization.OGC,new Version("1.1.1"), Profile.WMS_SLD),
	WMS_1_3_0_SLD(Specification.WMS,Organization.OGC,new Version("1.3.0"), Profile.WMS_SLD),
	
	WCS_1_0_0(Specification.WCS,Organization.OGC,new Version("1.0.0"), Profile.NONE),
	WCS_1_1_0(Specification.WCS,Organization.OGC,new Version("1.1.0"), Profile.NONE),
	WCS_1_1_1(Specification.WCS,Organization.OGC,new Version("1.1.1"), Profile.NONE),
	WCS_1_1_2(Specification.WCS,Organization.OGC,new Version("1.1.2"), Profile.NONE),
	
	PEP(Specification.PEP,Organization.OASIS,new Version(""), Profile.NONE),
	PDP(Specification.PDP,Organization.OASIS,new Version(""), Profile.NONE);
	
	public final Specification specification;
	public final Organization  organization;
	public final Version       version;
	public final Profile       profile;
	
	private ServiceDef(Specification spec, Organization org, Version ver, Profile prof){
		specification = spec;
		organization = org;
		version = ver;
		profile = prof;
	}
	
	public static class Version extends org.geotools.util.Version {
		
		private static final long serialVersionUID = -1004484794380489333L;
		
		public Version(String versionDef) {
			super(versionDef);
		}
	}
	
	public String toString(){
		return specification.abbreviation + " v." + version + " profile (" + profile.abbreviation + ")";
	}
	
	public enum Specification {
		
		NONE("",   "None"),
		CSW("CSW", "Catalog Service for the Web"),
		SOS("SOS", "Sensor Observation Service"),
		WCS("WCS", "Web Coverage Service"),
		WMS("WMS", "Web Map Service"), 
		PEP("PEP", "Policy Enforcement Point"), 
		PDP("PDP", "Policy Decision Point");
		
		public final String abbreviation;
		public final String fullName;
		
		private Specification(String abbrev, String full){
			abbreviation = abbrev;
			fullName     = full;
		}
	}
	
	public enum Organization {

		NONE  ("",      "None"),
		OASIS ("OASIS", "The Organization for the Advancement of Structured Information Standards"),
		OGC   ("OGC",   "The Open Geospatial Consortium"),
		W3C   ("W3C",   "The World Wide Web Consortium");
		
		public final String abbreviation;
		public final String fullName;
		
		private Organization(String abbrev, String full){
			abbreviation = abbrev;
			fullName     = full;
		}
	}
	
	public enum Profile {
		
		NONE    ("",     "None", new Version(""), Organization.NONE),
		WMS_SLD ("SLD",  "Styled Layer Descriptor profile of the Web Map Service", 
				 new Version("1.1.0"), Organization.OGC);
		
		public final String abbreviation;
		public final String fullName;
		public final Version version;
		public final Organization organization;
		
		private Profile(String abbrev, String full, Version ver, Organization org){
			abbreviation = abbrev;
			fullName     = full;
			version      = ver;
			organization = org;
		}
	}
}
