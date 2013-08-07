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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.dto.PortrayalContext;
import org.constellation.gui.binding.Style;
import org.constellation.gui.util.StyleUtilities;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;
import org.opengis.sld.StyledLayerDescriptor;

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
public final class OverviewServlet extends HttpServlet {

    /**
     * Use for debugging purpose.
     */
    private static final Logger LOGGER = Logging.getLogger(OverviewServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        render(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        render(req, resp);
    }

    public void render(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Get original request parameters.
        final String request = req.getParameter("REQUEST");
        final String layers  = req.getParameter("LAYERS");
        final String crs     = req.getParameter("CRS");
        final String bbox    = req.getParameter("BBOX");
        final String width   = req.getParameter("WIDTH");
        final String height  = req.getParameter("HEIGHT");
        final String format  = req.getParameter("FORMAT");
        final String sldBody = req.getParameter("SLD_BODY");
        final String sldVersion  = req.getParameter("SLD_VERSION");

        // Perform a GetMap.
        if ("GetMap".equalsIgnoreCase(request)) {
            final String wms         = req.getParameter("WMS");
            final String styles      = req.getParameter("STYLES");
            final String version     = req.getParameter("VERSION");
            final String transparent = req.getParameter("TRANSPARENT");
            final String exceptions  = req.getParameter("EXCEPTIONS");

            // Handle style JSON body.
            String sldXml = null;
            if (sldBody != null && !sldBody.isEmpty()) {
                try {
                    final Style style = StyleUtilities.readJson(sldBody, Style.class);

                    final MutableNamedLayer layer = SLDF.createNamedLayer();
                    layer.setName(layers);
                    layer.styles().add(style.toType());
                    final MutableStyledLayerDescriptor sld = SLDF.createSLD();
                    sld.layers().add(layer);

                    final StringWriter writer = new StringWriter();
                    if ("1.1.0".equals(sldVersion)) {
                        new StyleXmlIO().writeSLD(writer, sld, Specification.StyledLayerDescriptor.V_1_1_0);
                    } else {
                        new StyleXmlIO().writeSLD(writer, sld, Specification.StyledLayerDescriptor.V_1_0_0);
                    }
                    sldXml = writer.toString();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Invalid style JSON body.", ex);
                } catch (JAXBException ex) {
                    LOGGER.log(Level.WARNING, "The style marshalling has failed.", ex);
                }
            }

            // Prepare request.
            final HttpPost httpPost = new HttpPost(wms);
            final List<NameValuePair> params = new ArrayList<NameValuePair>(0);
            params.add(new BasicNameValuePair("SERVICE", "WMS"));
            params.add(new BasicNameValuePair("REQUEST", request));
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
            params.add(new BasicNameValuePair("SLD_BODY", sldXml));
            params.add(new BasicNameValuePair("SLD_VERSION", sldVersion));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // Perform request.
            execute(httpPost, resp.getOutputStream());
        }

        // Perform a portrayal.
        if ("Portray".equalsIgnoreCase(request)) {
            final String method      = req.getParameter("METHOD");
            final String providerId  = req.getParameter("PROVIDER");
            final String dataName    = req.getParameter("DATA");
            final String[] coords    = bbox.split(",");

            // Handle style JSON body.
            String styleXml = null;
            if (sldBody != null && !sldBody.isEmpty()) {
                try {
                    final Style style = StyleUtilities.readJson(sldBody, Style.class);
                    final StringWriter writer = new StringWriter();
                    if ("1.1.0".equals(sldVersion)) {
                        new StyleXmlIO().writeStyle(writer, style.toType(), Specification.StyledLayerDescriptor.V_1_1_0);
                    } else {
                        new StyleXmlIO().writeStyle(writer, style.toType(), Specification.StyledLayerDescriptor.V_1_0_0);
                    }
                    styleXml = writer.toString();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Invalid style JSON body.", ex);
                } catch (JAXBException ex) {
                    LOGGER.log(Level.WARNING, "The style marshalling has failed.", ex);
                }
            }

            // Prepare portrayal context.
            final PortrayalContext context = new PortrayalContext();
            context.setProviderId(providerId);
            context.setDataName(dataName);
            context.setProjection(crs);
            context.setFormat(format);
            context.setStyleBody(styleXml);
            context.setSldVersion(sldVersion);
            context.setWidth(Integer.parseInt(width));
            context.setHeight(Integer.parseInt(height));
            context.setWest(Double.parseDouble(coords[0]));
            context.setSouth(Double.parseDouble(coords[1]));
            context.setEast(Double.parseDouble(coords[2]));
            context.setNorth(Double.parseDouble(coords[3]));
            context.setLonFirstOutput(true);

            // Prepare request.
            final HttpPost httpPost = new HttpPost(method);
            final byte[] entity = StyleUtilities.writeJson(context).getBytes("UTF-8");
            httpPost.setEntity(new ByteArrayEntity(entity));
            httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader(HttpHeaders.CONTENT_ENCODING, "UTF-8");

            // Perform request.
            execute(httpPost, resp.getOutputStream());
        }

        resp.setContentType(format);
        resp.addHeader(HttpHeaders.PRAGMA, "no-cache");
        resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache,no-store");
        resp.addHeader(HttpHeaders.EXPIRES, "0");
    }

    public static MutableStyle getStyleXml(final String json) throws IOException {
        return StyleUtilities.readJson(json, Style.class).toType();
    }

    public static StyledLayerDescriptor getSldXml(final String json, final String layerName) throws IOException {
        final MutableNamedLayer layer = SLDF.createNamedLayer();
        layer.setName(layerName);
        layer.styles().add(getStyleXml(json));
        final MutableStyledLayerDescriptor sld = SLDF.createSLD();
        sld.layers().add(layer);
        return sld;
    }

    public static void execute(final HttpPost post, final OutputStream out) throws IOException {
        // Prepare execution.
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        // Request execution.
        final HttpResponse response = httpClient.execute(post);
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                out.write(EntityUtils.toByteArray(entity));
            } finally {
                out.flush();
                out.close();
            }
            HttpClientUtils.closeQuietly(httpClient);
        }
    }
}
