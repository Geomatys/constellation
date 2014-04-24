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
