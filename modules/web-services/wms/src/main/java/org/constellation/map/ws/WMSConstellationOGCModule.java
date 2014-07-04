package org.constellation.map.ws;

import org.constellation.ws.ConstellationOGCModule;

import javax.inject.Named;

;

@Named
public class WMSConstellationOGCModule implements ConstellationOGCModule {

    @Override
    public String getName() {
        return "WMS";
    }

}
