package org.constellation.engine.register;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.XML;
import org.constellation.utils.ISOMarshallerPool;

public class MetadataIOUtils {
    public static DefaultMetadata unmarshallMetadata(final String xml) throws JAXBException {
        final Unmarshaller um = ISOMarshallerPool.getInstance().acquireUnmarshaller();
        try (StringReader reader = new StringReader(xml)) {
            final DefaultMetadata meta = (DefaultMetadata) um.unmarshal(reader);
            ISOMarshallerPool.getInstance().recycle(um);
            return meta;
        }
    }

    public static DefaultMetadata unmarshallMetadata(final InputStream stream) throws JAXBException {
        final Unmarshaller um = ISOMarshallerPool.getInstance().acquireUnmarshaller();
        final DefaultMetadata meta = (DefaultMetadata) um.unmarshal(stream);
        ISOMarshallerPool.getInstance().recycle(um);
        return meta;
    }
    
    public static StringReader marshallMetadata(final DefaultMetadata meta) throws JAXBException {
        return new StringReader(marshallMetadataToString(meta));
    }

    
    public static String marshallMetadataToString(final DefaultMetadata meta) throws JAXBException {
        final StringWriter swIso = new StringWriter();
        final Marshaller mi = ISOMarshallerPool.getInstance().acquireMarshaller();
        mi.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        mi.marshal(meta, swIso);
        ISOMarshallerPool.getInstance().recycle(mi);
        return swIso.toString();
    }
}
