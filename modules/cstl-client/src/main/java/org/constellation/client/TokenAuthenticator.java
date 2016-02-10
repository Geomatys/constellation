/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2016 Geomatys.
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
package org.constellation.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import org.geotoolkit.util.FileUtilities;

/**
 * Constellations token authentication request filter.
 *
 * @author Johann Sorel (Geomatys)
 */
public class TokenAuthenticator implements ClientRequestFilter {

    private final String token;

    public TokenAuthenticator(String token) {
        this.token = token;
    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.putSingle("access_token", token);
    }

    /**
     * Request an authentication token from constellation.
     * 
     * @param serverUrl
     * @param login
     * @param password
     * @return
     */
    public static String requestToken(String serverUrl, String login, String password) throws MalformedURLException, IOException{
        final String loginUrl = serverUrl+ (serverUrl.endsWith("/") ? "spring/login" : "/spring/login");
        final byte[] message = ("{\"username\":\""+login+"\",\"password\":\""+password+"\"}").getBytes(Charset.forName("US-ASCII"));

        final URL url = new URL(loginUrl);
        final HttpURLConnection cnx = (HttpURLConnection) url.openConnection();
        cnx.setRequestMethod("POST");
        cnx.setRequestProperty("User-Agent", "Mozilla/5.0");
        cnx.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        cnx.setRequestProperty("Accept", "application/json, text/plain, */*");
        
        cnx.setDoOutput(true);

        //send authentication infos
        final OutputStream out = cnx.getOutputStream();
        out.write(message);
        out.flush();
        out.close();

        //get response
        final int responseCode = cnx.getResponseCode();
        if(responseCode!=200){
            throw new IOException("Failed to aquiere authentication token");
        }

        //parse response, extract token
        final InputStream in = cnx.getInputStream();
        String content = FileUtilities.getStringFromStream(in);
        in.close();
        content = content.replace('{', ' ').replace('}', ' ').trim();
        String token = null;
        for(String part : content.split(",")){
            final String[] keyValue = part.split(":");
            if("\"token\"".equals(keyValue[0])){
                token = keyValue[1].replace("\"", "");
                break;
            }
        }

        if(token==null){
            throw new IOException("Missing token in server authentication response.");
        }

        return token;
    }

}
