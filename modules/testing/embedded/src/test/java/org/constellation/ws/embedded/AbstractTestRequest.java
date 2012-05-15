/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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

package org.constellation.ws.embedded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.util.Util;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class AbstractTestRequest extends AbstractGrizzlyServer {

    protected static MarshallerPool pool;

    public String removeXmlns(String xml) {
        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns=\"[^\"]*\"", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");
        return s;
    }

    public void waitForStart() throws Exception {
        final URL u = new URL("http://localhost:9090/configuration?request=access");
        boolean ex = true;
        
        while (ex) {
            Thread.sleep(1 * 1000);
            ex = false;
            URLConnection conec = u.openConnection();
            try {
                conec.getInputStream();
            } catch (ConnectException e) {
                ex = true;
            }
        }
    }
    
    public void postRequestFile(URLConnection conec, String filePath) throws IOException {

        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final InputStream is = Util.getResourceAsStream(filePath);
        final StringWriter sw = new StringWriter();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char[] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        wr.write(sw.toString());
        wr.flush();
        in.close();

    }

    public void postRequestPlain(URLConnection conec, String request) throws IOException {

        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/plain");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        wr.write(request);
        wr.flush();
        
    }

    public void postRequestObject(URLConnection conec, Object request) throws IOException, JAXBException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringWriter sw = new StringWriter();
        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(request, sw);

        wr.write(sw.toString());
        wr.flush();
    }

    public Object unmarshallResponse(URLConnection conec) throws JAXBException, IOException {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object obj = unmarshaller.unmarshal(conec.getInputStream());

        pool.release(unmarshaller);

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    public String getStringResponse(URLConnection conec) throws UnsupportedEncodingException, IOException {
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(conec.getInputStream(), "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlResult = sw.toString();
        xmlResult = removeXmlns(xmlResult);
        xmlResult = xmlResult.replaceAll("xsi:schemaLocation=\"[^\"]*\" ", "");
        return xmlResult;
    }

    public String getStringFromFile(String filePath) throws UnsupportedEncodingException, IOException {
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(Util.getResourceAsStream(filePath), "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlExpResult = sw.toString();

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");
        xmlExpResult = removeXmlns(xmlExpResult);

        return xmlExpResult;
    }

    public Object unmarshallResponse(URL conec) throws JAXBException, IOException {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object obj = unmarshaller.unmarshal(conec.openStream());

        pool.release(unmarshaller);

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        return obj;
    }
}
