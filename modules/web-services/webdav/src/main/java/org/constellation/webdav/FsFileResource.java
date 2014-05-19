/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.webdav;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.constellation.configuration.WebdavContext;


/**
 *
 */
public class FsFileResource extends FsResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, PropPatchableResource {

    /**
     *
     * @param host - the requested host. E.g. www.mycompany.com
     * @param factory
     * @param file
     */
    public FsFileResource(final String host, final File file, final WebdavContext context) {
        super(host, file, context);
    }

    @Override
    public Long getContentLength() {
        return file.length();
    }

    @Override
    public String getContentType(String preferredList) {
        final String mime = ContentTypeUtils.findContentTypes(this.file);
        final String s = ContentTypeUtils.findAcceptableContentType(mime, preferredList);
        LOGGER.log(Level.FINER, "getContentType: preferred: {} mime: {} selected: {}", new Object[]{preferredList, mime, s});
        return s;
    }

    @Override
    public String checkRedirect(Request arg0) {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotFoundException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            if (range != null) {
                LOGGER.log(Level.FINE, "sendContent: ranged content: {0}", file.getAbsolutePath());
                throw new IOException("not Supported");
                //PartialEntity.writeRange(in, range, out);
            } else {
                LOGGER.log(Level.FINE, "sendContent: send whole file {0}", file.getAbsolutePath());
                IOUtils.copy(in, out);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            throw new NotFoundException("Couldnt locate content");
        } catch (ReadingException e) {
            throw new IOException(e);
        } catch (WritingException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return maxAgeSecond;
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    protected void doCopy(File dest) {
        try {
            FileUtils.copyFile(file, dest);
        } catch (IOException ex) {
            throw new RuntimeException("Failed doing copy to: " + dest.getAbsolutePath(), ex);
        }
    }

    @Deprecated
    @Override
    public void setProperties(Fields fields) {
        // MIL-50
        // not implemented. Just to keep MS Office sweet
    }
}
