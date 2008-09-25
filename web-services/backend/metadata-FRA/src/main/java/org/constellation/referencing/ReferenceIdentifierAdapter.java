/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.referencing;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.opengis.referencing.ReferenceIdentifier;

/**
 *
 * @author guilhem
 */
public class ReferenceIdentifierAdapter  extends XmlAdapter<ReferenceIdentifierAdapter,ReferenceIdentifier> {

    protected ReferenceIdentifier identifier;
    /**
     * Empty constructor for JAXB only.
     */
    protected ReferenceIdentifierAdapter() {
    }

    /**
     * Wraps an LocalName value with a {@code MD_LocalName} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ReferenceIdentifierAdapter(final ReferenceIdentifier id) {
        identifier = id;
    }

    /**
     * Returns the {@link LocalNameImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "RS_Identifier", namespace = "http://www.isotc211.org/2005/gmd")
    public IdentifierImpl getIdentifier() {
        return (identifier instanceof IdentifierImpl) ?
            (IdentifierImpl) identifier : new IdentifierImpl(identifier);
    }

    /**
     * Sets the value for the {@link LocalNameImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setIdentifier(final ReferenceIdentifier identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public ReferenceIdentifier unmarshal(ReferenceIdentifierAdapter value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.identifier;
    }

    @Override
    public ReferenceIdentifierAdapter marshal(ReferenceIdentifier value) throws Exception {
        if (value == null)
            return null;
        return new ReferenceIdentifierAdapter(value);
    }
}
