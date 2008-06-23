/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.seagis.bean;

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
            UploadedFile uploadedFile = (UploadedFile) event.getNewValue();

            //used when the <t:inputFileUpload> tag doesn't have a storage attribute or when the value of this attribute is memory.
            UploadedFileDefaultMemoryImpl _memory = (UploadedFileDefaultMemoryImpl) uploadedFile;

            //if the value of the attribute storage is file.
            //UploadedFileDefaultFileImpl _file = (UploadedFileDefaultFileImpl) uploadedFile;
            InputStream inputStream = _memory.getInputStream();
            
            // do something with uploadedFile
        } catch (IOException ex) {
            Logger.getLogger(UploadListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
