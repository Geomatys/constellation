/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.myfaces.custom.fileupload.UploadedFileDefaultMemoryImpl;

/**
 *
 * @author Mehdi Sidhoum.
 */
public class UploadListener implements ValueChangeListener {

    public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
        try {
            final UploadedFile uploadedFile = (UploadedFile) event.getNewValue();

            //used when the <t:inputFileUpload> tag doesn't have a storage attribute or when the value of this attribute is memory.
            final UploadedFileDefaultMemoryImpl memory = (UploadedFileDefaultMemoryImpl) uploadedFile;

            //if the value of the attribute storage is file.
            //UploadedFileDefaultFileImpl _file = (UploadedFileDefaultFileImpl) uploadedFile;
            final InputStream inputStream = memory.getInputStream();
            
            // do something with uploadedFile
        } catch (IOException ex) {
            Logger.getLogger(UploadListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
