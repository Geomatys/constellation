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

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.fs.LockManager;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

/**
 *
 */
public class FsMemoryLockManager implements LockManager {

    private static final Logger LOGGER = Logging.getLogger(FsMemoryLockManager.class);
    /**
     * maps current locks by the file associated with the resource
     */
    Map<File, CurrentLock> locksByFile;
    Map<String, CurrentLock> locksByToken;

    public FsMemoryLockManager() {
        locksByFile = new HashMap<File, CurrentLock>();
        locksByToken = new HashMap<String, CurrentLock>();
    }

    @Override
    public synchronized LockResult lock( LockTimeout timeout, LockInfo lockInfo, LockableResource r ) {
        FsResource resource = (FsResource) r;
        LockToken currentLock = currentLock( resource );
        if( currentLock != null ) {
            return LockResult.failed( LockResult.FailureReason.ALREADY_LOCKED );
        }

        LockToken newToken = new LockToken( UUID.randomUUID().toString(), lockInfo, timeout );
        CurrentLock newLock = new CurrentLock( resource.getFile(), newToken, lockInfo.lockedByUser );
        locksByFile.put( resource.getFile(), newLock );
        locksByToken.put( newToken.tokenId, newLock );
        return LockResult.success( newToken );
    }

    @Override
    public synchronized LockResult refresh( String tokenId, LockableResource resource ) {
        CurrentLock curLock = locksByToken.get( tokenId );
        if( curLock == null ) {
            LOGGER.finer("can't refresh because no lock");
            return LockResult.failed( LockResult.FailureReason.PRECONDITION_FAILED );
        } else {
            curLock.token.setFrom( new Date() );
            return LockResult.success( curLock.token );
        }
    }

    @Override
    public synchronized void unlock( String tokenId, LockableResource r ) throws NotAuthorizedException {
        FsResource resource = (FsResource) r;
        LockToken lockToken = currentLock( resource );
        if( lockToken == null ) {
            LOGGER.finer("not locked");
            return;
        }
        if( lockToken.tokenId.equals( tokenId ) ) {
            removeLock( lockToken );
        } else {
            throw new NotAuthorizedException( resource );
        }
    }

    private LockToken currentLock( FsResource resource ) {
        CurrentLock curLock = locksByFile.get( resource.getFile() );
        if( curLock == null ) return null;
        LockToken token = curLock.token;
        if( token.isExpired() ) {
            removeLock( token );
            return null;
        } else {
            return token;
        }
    }

    private void removeLock( LockToken token ) {
        LOGGER.log(Level.FINER, "removeLock: {0}", token.tokenId);
        CurrentLock currentLock = locksByToken.get( token.tokenId );
        if( currentLock != null ) {
            locksByFile.remove( currentLock.file );
            locksByToken.remove( currentLock.token.tokenId );
        } else {
            LOGGER.log(Level.WARNING, "couldnt find lock: {0}", token.tokenId);
        }
    }

    @Override
    public LockToken getCurrentToken( LockableResource r ) {
        FsResource resource = (FsResource) r;
        CurrentLock lock = locksByFile.get( resource.getFile() );
        if( lock == null ) return null;
        LockToken token = new LockToken();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.lockedByUser, LockInfo.LockDepth.ZERO );
        token.info.lockedByUser = lock.lockedByUser;
        token.timeout = lock.token.timeout;
        token.tokenId = lock.token.tokenId;
        return token;
    }

    class CurrentLock {

        final File file;
        final LockToken token;
        final String lockedByUser;

        public CurrentLock( File file, LockToken token, String lockedByUser ) {
            this.file = file;
            this.token = token;
            this.lockedByUser = lockedByUser;
        }
    }
}
