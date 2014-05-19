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
