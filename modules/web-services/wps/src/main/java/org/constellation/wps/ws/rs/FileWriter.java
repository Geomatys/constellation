package org.constellation.wps.ws.rs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import java.io.File;

/**
 * A class to manage the file writing operation into request response messages.
 *
 * @author Alexis Manin (Geomatys)
 * Date : 05/02/13
 */
public class FileWriter<T extends File> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.wps.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return File.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return t.length();
    }

    @Override
    public void writeTo(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {

        FileInputStream in = new FileInputStream(t);
        try {
            final byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead); // write
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            }
        }
    }
}
