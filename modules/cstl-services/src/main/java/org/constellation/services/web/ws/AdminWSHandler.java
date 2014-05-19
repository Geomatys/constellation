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
package org.constellation.services.web.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class AdminWSHandler implements WebSocketHandler {

    @Override
    public void afterConnectionClosed(WebSocketSession arg0, CloseStatus arg1) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession arg0) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleMessage(WebSocketSession arg0, WebSocketMessage<?> arg1) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleTransportError(WebSocketSession arg0, Throwable arg1) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean supportsPartialMessages() {
        // TODO Auto-generated method stub
        return false;
    }

}
