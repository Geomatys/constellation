package org.constellation.admin;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.constellation.configuration.ConfigDirectory;



public final class SessionListener implements HttpSessionListener {

    public SessionListener() {
    }

    public void sessionCreated(HttpSessionEvent sessionEvent) {

    }

    public void sessionDestroyed(HttpSessionEvent sessionEvent) {

        // Get the session that was invalidated
        HttpSession session = sessionEvent.getSession();
        ConfigDirectory.removeUploadDirectory(session.getId());
    }
}