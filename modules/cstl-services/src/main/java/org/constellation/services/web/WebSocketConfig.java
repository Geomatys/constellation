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
package org.constellation.services.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The registerStompEndpoints() method registers the "/simplemessages"
        // endpoint, enabling SockJS fallback options so that alternative
        // messaging options may be used if WebSocket is not available. This
        // endpoint when prefixed with "/app", is the endpoint that the
        // WebSocketBroadcastController.processMessageFromClient() method is
        // mapped to handle.
        registry.addEndpoint("/ws/adminmessages").withSockJS();
    }

    /**
     * Configure message broker options.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // The configureMessageBroker() method overrides the default method in
        // WebSocketMessageBrokerConfigurer to configure the message broker. It
        // starts by calling enableSimpleBroker() to enable a simple
        // memory-based message broker to carry the greeting messages back to
        // the client on destinations prefixed with "/topic/". It also
        // designates the "/app" prefix for messages that are bound for
        // @MessageMapping-annotated methods.

        config.enableSimpleBroker("/topic/", "/queue/");
        config.setApplicationDestinationPrefixes("/app");
    }

    

    /**
     * Configure the {@link org.springframework.messaging.MessageChannel} used
     * for outgoing messages to WebSocket clients. By default the channel is
     * backed by a thread pool of size 1. It is recommended to customize thread
     * pool settings for production use.
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(4).maxPoolSize(10);
    }

}
