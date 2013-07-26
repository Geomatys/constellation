/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.gui.binding.Style;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.gui.util.StyleFactories.SLDF;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class GetMapServlet extends HttpServlet {

    /**
     * Use for debugging purpose.
     */
    private static final Logger LOGGER = Logging.getLogger(GetMapServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        doGetMap(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        doGetMap(req, resp);
    }

    public void doGetMap(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Get original request parameters.
        String wms         = req.getParameter("WMS");
        String layers      = req.getParameter("LAYERS");
        String styles      = req.getParameter("STYLES");
        String version     = req.getParameter("VERSION");
        String crs         = req.getParameter("CRS");
        String bbox        = req.getParameter("BBOX");
        String width       = req.getParameter("WIDTH");
        String height      = req.getParameter("HEIGHT");
        String format      = req.getParameter("FORMAT");
        String transparent = req.getParameter("TRANSPARENT");
        String exceptions  = req.getParameter("EXCEPTIONS");
        String sldBody     = req.getParameter("SLD_BODY");
        String sldVersion  = req.getParameter("SLD_VERSION");

        // Handle style JSON body.
        if (sldBody != null && !sldBody.isEmpty()) {
            try {
                // JSON binding.
                final Style style = new ObjectMapper().readValue(sldBody, Style.class);

                // XML binding.
                final MutableNamedLayer layer = SLDF.createNamedLayer();
                layer.setName(layers);
                layer.styles().add(style.toType());
                final MutableStyledLayerDescriptor sld = SLDF.createSLD();
                sld.layers().add(layer);

                // Write XML body.
                final StringWriter writer = new StringWriter();
                new StyleXmlIO().writeSLD(writer, sld, Specification.StyledLayerDescriptor.V_1_1_0);
                sldBody = writer.toString();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Invalid style JSON body.", ex);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "The style marshalling has failed.", ex);
            }
        }

        // Prepare GetMap POST request.
        final HttpPost getMap = new HttpPost(wms);
        final List<NameValuePair> params = new ArrayList<NameValuePair>(0);
        params.add(new BasicNameValuePair("REQUEST", "GetMap"));
        params.add(new BasicNameValuePair("LAYERS", layers));
        params.add(new BasicNameValuePair("STYLES", styles));
        params.add(new BasicNameValuePair("VERSION", version));
        params.add(new BasicNameValuePair("CRS", crs));
        params.add(new BasicNameValuePair("BBOX", bbox));
        params.add(new BasicNameValuePair("WIDTH", width));
        params.add(new BasicNameValuePair("HEIGHT", height));
        params.add(new BasicNameValuePair("FORMAT", format));
        params.add(new BasicNameValuePair("TRANSPARENT", transparent));
        params.add(new BasicNameValuePair("EXCEPTIONS", exceptions));
        params.add(new BasicNameValuePair("SLD_BODY", sldBody));
        params.add(new BasicNameValuePair("SLD_VERSION", sldVersion));
        getMap.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        // Prepare GetMap execution.
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        // Execute GetMap and return result.
        final HttpResponse response = httpClient.execute(getMap);
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            final OutputStream os = resp.getOutputStream();
            try {
                os.write(EntityUtils.toByteArray(entity));
            } finally {
                os.flush();
                os.close();
            }
        }
        HttpClientUtils.closeQuietly(httpClient);
    }
}
