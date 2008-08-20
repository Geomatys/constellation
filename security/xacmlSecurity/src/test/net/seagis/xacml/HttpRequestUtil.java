/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package net.seagis.xacml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *  Utility class for the web binding
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 10, 2007 
 *  @version $Revision$
 */
public class HttpRequestUtil
{
     public HttpServletRequest createRequest(final Principal gp, final String uri) {
        return new HttpServletRequest() {

            public String getAuthType() {
                return null;
            }

            public String getContextPath() {
                return null;
            }

            public Cookie[] getCookies() {
                return null;
            }

            public long getDateHeader(String arg0) {
                return 0;
            }

            public String getHeader(String arg0) {
                return null;
            }

            @SuppressWarnings("unchecked")
            public Enumeration getHeaderNames() {
                return null;
            }

            @SuppressWarnings("unchecked")
            public Enumeration getHeaders(String arg0) {
                return null;
            }

            public int getIntHeader(String arg0) {
                return 0;
            }

            public String getMethod() {
                return "GET";
            }

            public String getPathInfo() {
                return null;
            }

            public String getPathTranslated() {
                return null;
            }

            public String getQueryString() {
                return null;
            }

            public String getRemoteUser() {
                return null;
            }

            public String getRequestURI() {
                return uri;
            }

            public StringBuffer getRequestURL() {
                return null;
            }

            public String getRequestedSessionId() {
                return null;
            }

            public String getServletPath() {
                return null;
            }

            public HttpSession getSession() {
                return null;
            }

            public HttpSession getSession(boolean arg0) {
                return null;
            }

            public Principal getUserPrincipal() {
                return gp;
            }

            public boolean isRequestedSessionIdFromCookie() {
                return false;
            }

            public boolean isRequestedSessionIdFromURL() {
                return false;
            }

            public boolean isRequestedSessionIdFromUrl() {
                return false;
            }

            public boolean isRequestedSessionIdValid() {
                return false;
            }

            public boolean isUserInRole(String arg0) {
                return false;
            }

            public Object getAttribute(String arg0) {
                return null;
            }

            @SuppressWarnings("unchecked")
            public Enumeration getAttributeNames() {
                return null;
            }

            public String getCharacterEncoding() {
                return null;
            }

            public int getContentLength() {
                return 0;
            }

            public String getContentType() {
                return null;
            }

            public ServletInputStream getInputStream() throws IOException {
                return null;
            }

            public String getLocalAddr() {
                return null;
            }

            public String getLocalName() {
                return null;
            }

            public int getLocalPort() {
                return 0;
            }

            public Locale getLocale() {
                return null;
            }

            @SuppressWarnings("unchecked")
            public Enumeration getLocales() {
                return null;
            }

            public String getParameter(String arg0) {
                return null;
            }

            @SuppressWarnings("unchecked")
            public Map getParameterMap() {
                return null;
            }

            @SuppressWarnings("unchecked")
            public Enumeration getParameterNames() {
                return null;
            }

            public String[] getParameterValues(String arg0) {
                return null;
            }

            public String getProtocol() {
                return null;
            }

            public BufferedReader getReader() throws IOException {
                return null;
            }

            public String getRealPath(String arg0) {
                return null;
            }

            public String getRemoteAddr() {
                return null;
            }

            public String getRemoteHost() {
                return null;
            }

            public int getRemotePort() {
                return 0;
            }

            public RequestDispatcher getRequestDispatcher(String arg0) {
                return null;
            }

            public String getScheme() {
                return null;
            }

            public String getServerName() {
                return null;
            }

            public int getServerPort() {
                return 0;
            }

            public boolean isSecure() {
                return false;
            }

            public void removeAttribute(String arg0) {
            }

            public void setAttribute(String arg0, Object arg1) {
            }

            public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
            }
        };
    }
}
