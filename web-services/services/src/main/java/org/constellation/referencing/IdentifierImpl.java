/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package net.seagis.referencing;

import java.util.Map;

import javax.xml.bind.annotation.XmlType;
import org.geotools.metadata.iso.MetadataEntity;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import static org.opengis.referencing.IdentifiedObject.REMARKS_KEY;

import org.geotools.resources.Utilities;


/**
 */
@XmlType(name = "RS_Identifier")
public class IdentifierImpl extends MetadataEntity implements ReferenceIdentifier {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8474731565582774497L;

    /**
     * A pool of {@link LocalName} values for given {@link InternationalString}.
     * Will be constructed only when first needed.
     */
    private static Map<CharSequence,GenericName> SCOPES;

    /**
     * Identifier code or name, optionally from a controlled list or pattern
     * defined by a code space.
     */
    private String code;

    /**
     * Name or identifier of the person or organization responsible for namespace.
     */
    private String codespace;

    /**
     * Organization or party responsible for definition and maintenance of the
     * code space or code.
     */
    private Citation authority;

    /**
     * Identifier of the version of the associated code space or code, as specified
     * by the code space or code authority. This version is included only when the
     * {@linkplain #getCode code} uses versions. When appropriate, the edition is
     * identified by the effective date, coded using ISO 8601 date format.
     */
    private String version;

    /**
     * Comments on or information about this identifier, or {@code null} if none.
     */
    private final InternationalString remarks;

   

    /**
     * 
     */
    public IdentifierImpl() {
        code      = null;
        codespace = null;
        authority = null;
        version   = null;
        remarks   = null;
    }


    /**
     * Identifier code or name, optionally from a controlled list or pattern.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    /**
     * Name or identifier of the person or organization responsible for namespace.
     *
     * @return The codespace, or {@code null} if not available.
     */
    public String getCodeSpace() {
        return codespace;
    }
    
    public void setCodeSpace(String codespace) {
        this.codespace = codespace;
    }

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     *
     * @return The authority, or {@code null} if not available.
     */
    public Citation getAuthority() {
        return authority;
    }
    
    public void setAuthority(Citation authority) {
        this.authority = authority;
    }

    /**
     * Identifier of the version of the associated code space or code, as specified by the
     * code authority. This version is included only when the {@linkplain #getCode code}
     * uses versions. When appropriate, the edition is identified by the effective date,
     * coded using ISO 8601 date format.
     *
     * @return The version, or {@code null} if not available.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Comments on or information about this identifier, or {@code null} if none.
     */
    public InternationalString getRemarks() {
        return remarks;
    }

    /**
     * Compares this identifier with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final IdentifierImpl that = (IdentifierImpl) object;
            return Utilities.equals(this.code,      that.code     ) &&
                   Utilities.equals(this.codespace, that.codespace) &&
                   Utilities.equals(this.version,   that.version  ) &&
                   Utilities.equals(this.authority, that.authority) &&
                   Utilities.equals(this.remarks,   that.remarks  );
        }
        return false;
    }

    /**
     * Returns a hash code value for this identifier.
     */
    @Override
    public int hashCode() {
        int hash = (int) serialVersionUID;
        if (code != null) {
            hash ^= code.hashCode();
        }
        if (version != null) {
            hash = hash*37 + version.hashCode();
        }
        return hash;
    }
}

