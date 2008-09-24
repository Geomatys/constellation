/**
 * An explanation
 * for this package is provided in the {@linkplain org.opengis.service OpenGIS&reg; javadoc}.
 * The remaining discussion on this page is specific to the Geotools implementation.
 */
@XmlSchema(elementFormDefault= XmlNsForm.QUALIFIED,
namespace="http://www.isotc211.org/2005/gmd",
xmlns = {
   @XmlNs(prefix = "gmd", namespaceURI = "http://www.isotc211.org/2005/gmd"),
    @XmlNs(prefix = "gco", namespaceURI = "http://www.isotc211.org/2005/gco"),
    @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance")
})
@XmlAccessorType(XmlAccessType.NONE)
@XmlJavaTypeAdapters({
    // ISO 19115 adapter (metadata module)
    @XmlJavaTypeAdapter(LocalNameAdapter.class),
    @XmlJavaTypeAdapter(GenericNameAdapter.class),
    @XmlJavaTypeAdapter(ConstraintsAdapter.class),
    @XmlJavaTypeAdapter(KeywordsAdapter.class),
    @XmlJavaTypeAdapter(ExtentAdapter.class),
    @XmlJavaTypeAdapter(OnLineResourceAdapter.class),
    @XmlJavaTypeAdapter(DataIdentificationAdapter.class),
    @XmlJavaTypeAdapter(StandardOrderProcessAdapter.class),
    @XmlJavaTypeAdapter(ResponsiblePartyAdapter.class),
    
    // Primitive type handling
    @XmlJavaTypeAdapter(DoubleAdapter.class),
    @XmlJavaTypeAdapter(type=double.class, value=DoubleAdapter.class),
    @XmlJavaTypeAdapter(FloatAdapter.class),
    @XmlJavaTypeAdapter(type=float.class, value=FloatAdapter.class),
    @XmlJavaTypeAdapter(IntegerAdapter.class),
    @XmlJavaTypeAdapter(type=int.class, value=IntegerAdapter.class),
    @XmlJavaTypeAdapter(LongAdapter.class),
    @XmlJavaTypeAdapter(type=long.class, value=LongAdapter.class),
    @XmlJavaTypeAdapter(BooleanAdapter.class),
    @XmlJavaTypeAdapter(type=boolean.class, value=BooleanAdapter.class)
})
package org.constellation.referencing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import org.geotools.resources.jaxb.metadata.*;
import org.geotools.resources.jaxb.code.*;
import org.geotools.resources.jaxb.primitive.*;


