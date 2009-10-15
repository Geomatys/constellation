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
package org.constellation.metadata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.constellation.jaxb.AnchoredMarshallerPool;

/**
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class AnchorPool extends AnchoredMarshallerPool {
    public AnchorPool(final List<Class> classes) throws JAXBException, URISyntaxException {
        super(classes.toArray(new Class[]{}));
        addAnchor("Common Data Index record", new URI("SDN:L231:3:CDI"));
        addAnchor("France", new URI("SDN:C320:2:FR"));
        addAnchor("EPSG:4326", new URI("SDN:L101:2:4326"));
        addAnchor("2", new URI("SDN:C371:1:2"));
        addAnchor("35", new URI("SDN:C371:1:35"));
        addAnchor("Transmittance and attenuance of the water column", new URI("SDN:P021:35:ATTN"));
        addAnchor("Electrical conductivity of the water column", new URI("SDN:P021:35:CNDC"));
        addAnchor("Dissolved oxygen parameters in the water column", new URI("SDN:P021:35:DOXY"));
        addAnchor("Light extinction and diffusion coefficients", new URI("SDN:P021:35:EXCO"));
        addAnchor("Dissolved noble gas concentration parameters in the water column", new URI("SDN:P021:35:HEXC"));
        addAnchor("Optical backscatter", new URI("SDN:P021:35:OPBS"));
        addAnchor("Salinity of the water column", new URI("SDN:P021:35:PSAL"));
        addAnchor("Dissolved concentration parameters for 'other' gases in the water column", new URI("SDN:P021:35:SCOX"));
        addAnchor("Temperature of the water column", new URI("SDN:P021:35:TEMP"));
        addAnchor("Visible waveband radiance and irradiance measurements in the atmosphere", new URI("SDN:P021:35:VSRA"));
        addAnchor("Visible waveband radiance and irradiance measurements in the water column", new URI("SDN:P021:35:VSRW"));
        addAnchor("MEDATLAS ASCII", new URI("SDN:L241:1:MEDATLAS"));
    }
}
