package org.constellation.ws;

public interface DIEnhancer {

    Worker enhance(Class<? extends Worker> worker, String id);
    
}
