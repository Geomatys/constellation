package org.constellation.ws.component;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.constellation.ws.ConstellationOGCModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Named
public class ConstellationOGCRegistry {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired(required = false)
    private Map<String,ConstellationOGCModule> constellationOGCModules = new HashMap<>();

    @PostConstruct
    public void init() {
        LOGGER.info(constellationOGCModules.size() + " Constellation OGC module"
                + (constellationOGCModules.size() > 1 ? "s" : "") + " detected.");
        
        for (Entry<String, ? extends ConstellationOGCModule> moduleEntry : constellationOGCModules.entrySet()) {
            LOGGER.info(String.format("\t* %-5s (%s)", moduleEntry.getValue().getName() , moduleEntry.getKey()));
        }
    }

}
