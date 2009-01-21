/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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


package org.constellation.metadata.io;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.ws.WebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class FileMetadataWriter extends MetadataWriter {
    
    /**
     * A unMarshaller to get object from harvested resource.
     */
    private final Marshaller marshaller;
    
    /**
     * A directory in witch the metadata files are stored.
     */
    private final File dataDirectory;
    
    /**
     * 
     * @param index
     * @param marshaller
     * @throws java.sql.SQLException
     */
    public FileMetadataWriter(AbstractIndexer index, Marshaller marshaller, File dataDirectory) throws SQLException {
        super(index);
        this.marshaller    = marshaller;
        this.dataDirectory = dataDirectory;
        
    }

    @Override
    public boolean storeMetadata(Object obj) throws SQLException, WebServiceException {
        File f = null;
        try {
            //TODO find indentifier
            String identifier = "testID";
            f = new File(dataDirectory, identifier + ".xml");
            f.createNewFile();
            marshaller.marshal(obj, f);
            indexer.indexDocument(obj);
        } catch (JAXBException ex) {
            throw new WebServiceException("Unable to marshall the object: " + obj, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new WebServiceException("Unable to write the file: " + f.getPath(), NO_APPLICABLE_CODE);
        }
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
