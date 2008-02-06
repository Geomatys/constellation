/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.swe;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlSeeAlso({AbstractDataRecordEntry.class, TimeType.class, BooleanType.class, QuantityType.class})
@XmlType(name="AbstractDataComponent")
public class AbstractDataComponentEntry extends Entry implements AbstractDataComponent{
    
    /**
     * The identifier of the component (override from abstractGML Type).
     */
    @XmlAttribute
    private String id;
    
    @XmlTransient //@XmlAttribute
    private boolean fixed;
    
    /**
     * definition of the record.
     */
    protected String definition;
    
    /**
     * Constructor used by jaxb.
     */
    AbstractDataComponentEntry() {}
    
    /**
     * a simple constructor used by the sub classes to initialize l'Entry.
     */
    public AbstractDataComponentEntry(String id, String definition, boolean fixed) {
        super(id);
        this.id         = id;
        this.definition = definition;
        this.fixed      = fixed;
    }
    
    /**
     * Return the identifier of this data record.
     */
    public String getId() {
        return id;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefinition() {
        return definition;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Return the numeric code identifiyng this entry.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final AbstractDataComponentEntry that = (AbstractDataComponentEntry) object;
        return Utilities.equals(this.id,         that.id)         &&
               Utilities.equals(this.definition, that.definition) &&
               Utilities.equals(this.fixed,      that.fixed);
    }
    
    @Override
    public String toString() {
        return "id=" + id + " definition=" + definition + " fixed=" + fixed;
    }
    
}
