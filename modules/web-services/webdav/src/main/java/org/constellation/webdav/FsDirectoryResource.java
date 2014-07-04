/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.constellation.configuration.WebdavContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents a directory in a physical file system.
 *
 */
public class FsDirectoryResource extends FsResource implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, MoveableResource, PropFindableResource, LockingCollectionResource, GetableResource {

    private static final DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
    
    private final String defaultPage;
    
    private final File root;
    
    private final String contextPath;
    
    private final boolean hideDotFile;
    
    private final String id;
    
    public FsDirectoryResource(final String host, final File dir, final WebdavContext context) {
        super(host, dir, context);
        this.defaultPage = context.getDefaultPage();
        this.root        = context.getRootFile();
        this.contextPath = context.getContextPath();
        this.hideDotFile = context.isHideDotFile();
        this.id          = context.getId();
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + dir.getAbsolutePath());
        }
    }

    @Override
    public CollectionResource createCollection(final String name) {
        final File fnew = new File(file, name);
        if (!fnew.mkdir()) {
            throw new RuntimeException("Failed to create: " + fnew.getAbsolutePath());
        }
        return new FsDirectoryResource(host, fnew, context);
    }

    @Override
    public Resource child(final String name) {
        final File fchild = new File(file, name);
        return resolveFile(this.host, fchild);

    }

    @Override
    public List<? extends Resource> getChildren() {
        final List<FsResource> list = new ArrayList<FsResource>();
        for (File fchild : this.file.listFiles()) {
            final FsResource res = resolveFile(this.host, fchild);
            if (res != null) {
                list.add(res);
            } else {
                LOGGER.log(Level.SEVERE, "Couldnt resolve file {}", fchild.getAbsolutePath());
            }
        }
        return list;
    }

    /**
     * Will redirect if a default page has been specified on the factory
     *
     * @param request
     * @return
     */
    @Override
    public String checkRedirect(final Request request) {
        if (defaultPage != null) {
            return request.getAbsoluteUrl() + "/" + defaultPage;
        } else {
            return null;
        }
    }

    @Override
    public Resource createNew(final String name, final InputStream in, final Long length, final String contentType) throws IOException {
        final File dest = new File(this.getFile(), name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
        // todo: ignores contentType
        return resolveFile(this.host, dest);

    }

    @Override
    protected void doCopy(final File dest) {
        try {
            FileUtils.copyDirectory(this.getFile(), dest);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to copy to:" + dest.getAbsolutePath(), ex);
        }
    }

    @Override
    public LockToken createAndLock(final String name, final LockTimeout timeout, final LockInfo lockInfo) throws NotAuthorizedException {
        final File dest = new File(this.getFile(), name);
        createEmptyFile(dest);
        final FsFileResource newRes = new FsFileResource(host, dest, context);
        final LockResult res = newRes.lock(timeout, lockInfo);
        return res.getLockToken();
    }

    private void createEmptyFile(File file) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(fout);
        }
    }

    /**
     * Will generate a listing of the contents of this directory, unless the
     * factory's allowDirectoryBrowsing has been set to false.
     *
     * If so it will just output a message saying that access has been disabled.
     *
     * @param out
     * @param range
     * @param params
     * @param contentType
     * @throws IOException
     * @throws NotAuthorizedException
     */
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException {
        final File currentFile = getFile();
        final String subpath = currentFile.getCanonicalPath().substring(root.getCanonicalPath().length()).replace('\\', '/');
        final String baseURL = getURL();
                
        //String uri = "/" + factory.getContextPath() + subpath;
        XmlWriter w = new XmlWriter(out);
        w.open("html");
        w.open("head");
        w.open("title");
        w.writeText("Constellation webdav repository");
        w.close("title");        
        w.close("head");
        w.open("body");
        w.begin("h1").open().writeText("Constellation webdav repository").close();
        w.open("table");
        
        //<tr><th><img src="/icons/blank.gif" alt="[ICO]"></th><th><a href="?C=N;O=D">Name</a></th><th><a href="?C=M;O=A">Last modified</a></th></tr>
        w.open("tr");
        w.open("th");
        w.begin("img").writeAtt("src", baseURL + "/icons/blank.gif").writeAtt("alt", "[ICO]").close();
        w.close("th");
        
        w.open("th");
        w.begin("a").writeAtt("href", "#").open(); //?C=N;O=D
        w.writeText("Name");
        w.close("a");
        w.close("th");
        
        w.open("th");
        w.begin("a").writeAtt("href", "#").open(); //?C=M;O=A
        w.writeText("Last modified");
        w.close("a");
        w.close("th");
        
        w.close("tr");
        
        // separator <tr><th colspan="5"><hr></th></tr>
        w.open("tr");
        w.begin("th").writeAtt("colspan", "5").open();
        w.begin("hr").open().close();
        w.close("th");
        w.close("tr");
        
        //if not root we write the ".." link
        if (!root.getPath().equals(currentFile.getPath())) {
            w.open("tr");

            w.begin("td").writeAtt("valign", "top").open();
            w.begin("img").writeAtt("src", baseURL + "/icons/folder.gif").writeAtt("alt", "[DIR]").close();
            w.close("td");
             
            w.open("td");
            final String path = buildHref(subpath, null);
            w.begin("a").writeAtt("href", path).open().writeText("Parent Directory").close();

            w.close("td");

            w.begin("td").open().writeText("-").close();
            w.close("tr");
        }
        
        
        for (Resource r : getChildren()) {
            if (!r.getName().startsWith(".") || !hideDotFile) {
                w.open("tr");
                
                // icons <td valign="top"><img src="/icons/folder.gif" alt="[DIR]"></td>
                if (r instanceof FsDirectoryResource) {
                    w.begin("td").writeAtt("valign", "top").open();
                    w.begin("img").writeAtt("src", baseURL + "/icons/folder.gif").writeAtt("alt", "[DIR]").close();
                    w.close("td");
                } else {
                    w.begin("td").writeAtt("valign", "top").open();
                    w.begin("img").writeAtt("src", baseURL + "/icons/unknown.gif").writeAtt("alt", "     ").close();
                    w.close("td");
                }
                w.open("td");
                final String path = buildHref(subpath, r.getName());
                w.begin("a").writeAtt("href", path).open().writeText(r.getName()).close();

                w.close("td");

                w.begin("td").open().writeText(formatter.format(r.getModifiedDate())).close();
                w.close("tr");
            }
        }
        
        // separator <tr><th colspan="5"><hr></th></tr>
        w.open("tr");
        w.begin("th").writeAtt("colspan", "5").open();
        w.begin("hr").open().close();
        w.close("th");
        w.close("tr");
        
        w.close("table");
        w.close("body");
        w.close("html");
        w.flush();
    }

    @Override
    public Long getMaxAgeSeconds(final Auth auth) {
        return null;
    }

    @Override
    public String getContentType(final String accepts) {
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    private String getURL() {
        final String abUrl = HttpManager.request().getAbsoluteUrl();
        final int i = abUrl.indexOf(contextPath);
        if (i != -1) {
            return abUrl.substring(0, i);
        }
        throw new IllegalArgumentException("Error in getURL");
    }
    
    private String buildHref(final String subPath, final String name) {
        
        final String abUrl = getURL() + contextPath + '/'+ id + subPath;

        if (ssoPrefix == null) {
            if (name == null) {
                final String s = abUrl.substring(0, abUrl.lastIndexOf(getName()));
                return s;
            } else {
                return abUrl + '/' + name;
            }
        } else {
            // This is to match up with the prefix set on SimpleSSOSessionProvider in MyCompanyDavServlet
            String s = insertSsoPrefix(abUrl, ssoPrefix);
            return s += name;
        }
    }

    public static String insertSsoPrefix(final String abUrl, final String prefix) {
        // need to insert the ssoPrefix immediately after the host and port
        int pos = abUrl.indexOf("/", 8);
        String s = abUrl.substring(0, pos) + "/" + prefix;
        s += abUrl.substring(pos);
        return s;
    }
    
    public FsResource resolveFile(String host, File file) {
        FsResource r;
        if (!file.exists()) {
            LOGGER.log(Level.INFO, "file not found: {0}", file.getAbsolutePath());
            return null;
        } else if (file.isDirectory()) {
            r = new FsDirectoryResource(host, file, context);
        } else {
            r = new FsFileResource(host, file, context);
        }
        return r;
    }
}
