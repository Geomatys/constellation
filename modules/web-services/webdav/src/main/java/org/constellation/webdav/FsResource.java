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

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.http.fs.LockManager;
import com.ettrema.http.fs.NullSecurityManager;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.configuration.WebdavContext;
import org.apache.sis.util.logging.Logging;

/**
 *
 */
public abstract class FsResource implements Resource, MoveableResource, CopyableResource, LockableResource, DigestResource {

    protected static final Logger LOGGER = Logging.getLogger(FsResource.class);
    
    protected File file;
    protected final String host;
    protected String ssoPrefix;
    protected final boolean isDigestAllowed;
    private final LockManager lockManager;
    private final SecurityManager securityManager;
    protected final long maxAgeSecond;
    protected final WebdavContext context;
    

    protected abstract void doCopy(File dest);

    public FsResource(final String host, final File file, final WebdavContext context) {
        this.host            = host;
        this.file            = file;
        this.context         = context;
        this.maxAgeSecond    = context.getMaxAgeSeconds();
        this.isDigestAllowed = context.isDigestAllowed();
        this.ssoPrefix       = context.getSsoPrefix();
        this.lockManager     = new FsMemoryLockManager();
        this.securityManager = new NullSecurityManager();
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getUniqueId() {
        String s = file.lastModified() + "_" + file.length() + "_" + file.getAbsolutePath();
        return s.hashCode() + "";
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return securityManager.authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return isDigestAllowed;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return securityManager.authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        return new Date(file.lastModified());
    }

    public Date getCreateDate() {
        return null;
    }

    public int compareTo(Resource o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public void moveTo(CollectionResource newParent, String newName) {
        if (newParent instanceof FsDirectoryResource) {
            FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
            File dest = new File(newFsParent.getFile(), newName);
            boolean ok = this.file.renameTo(dest);
            if (!ok) {
                throw new RuntimeException("Failed to move to: " + dest.getAbsolutePath());
            }
            this.file = dest;
        } else {
            throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
        }
    }

    @Override
    public void copyTo(CollectionResource newParent, String newName) {
        if (newParent instanceof FsDirectoryResource) {
            FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
            File dest = new File(newFsParent.getFile(), newName);
            doCopy(dest);
        } else {
            throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
        }
    }

    public void delete() {
        boolean ok = file.delete();
        if (!ok) {
            throw new RuntimeException("Failed to delete");
        }
    }

    @Override
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
        return lockManager.lock(timeout, lockInfo, this);
    }

    @Override
    public LockResult refreshLock(String token) throws NotAuthorizedException {
        return lockManager.refresh(token, this);
    }

    @Override
    public void unlock(String tokenId) throws NotAuthorizedException {
        lockManager.unlock(tokenId, this);
    }

    @Override
    public LockToken getCurrentLock() {
        if (lockManager != null) {
            return lockManager.getCurrentToken(this);
        } else {
            LOGGER.log(Level.WARNING, "getCurrentLock called, but no lock manager: file: {0}", file.getAbsolutePath());
            return null;
        }
    }
}
