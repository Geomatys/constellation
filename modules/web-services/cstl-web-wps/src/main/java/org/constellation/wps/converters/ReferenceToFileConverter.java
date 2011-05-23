/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
 */
package org.constellation.wps.converters;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;



/**
 * Implementation of ObjectConverter to convert a reference into a File.
 * Reference is define by a <code>Map<String,String></code> with entries keys :
 * <ul>
 * <li>href : Url to the data</li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * <li>method : GET or POST</li>
 * </ul>
 * @author Quentin Boileau
 * @module pending
 */
public class ReferenceToFileConverter extends SimpleConverter<Map<String,String>, File> {

    private static ReferenceToFileConverter INSTANCE;

    private ReferenceToFileConverter(){
    }

    public static ReferenceToFileConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToFileConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends File> getTargetClass() {
        return File.class ;
    }
 
    @Override
    public File convert(Map<String,String> source) throws NonconvertibleObjectException {
                    
        try {
                final URL u = new URL(source.get("href"));
                final URLConnection uc = u.openConnection();
                final String contentType = uc.getContentType();
                final int contentLength = uc.getContentLength();
              
                
                final InputStream raw = uc.getInputStream();
                final InputStream in = new BufferedInputStream(raw);
                
                
                // get filename from the path
                String filename = u.getFile();
                filename = filename.substring(filename.lastIndexOf('/') + 1);
                int dotPos = filename.lastIndexOf(".");
                int len = filename.length();
                String name = filename.substring(0, dotPos);
                String ext = filename.substring(dotPos+1,len) ;
                
                //Create a temp file
                File file = File.createTempFile(name, ext);
                file.deleteOnExit();
                final FileOutputStream out = new FileOutputStream(file);
                
                final byte[] data = new byte[contentLength];
                byte[] readData = new byte[1024];
                int i = in.read(readData);

                while (i != -1) {
                    out.write(readData, 0, i);
                    i = in.read(readData);
                }
               
                in.close();

                out.write(data);
                out.flush();
                out.close();
                
                return file;
            }catch (MalformedURLException ex) {
                throw new NonconvertibleObjectException("Reference file invalid input : Malformed url",ex);
            } catch (IOException ex) {
                throw new NonconvertibleObjectException("Reference file invalid input : IO",ex);
            } 
    }
}