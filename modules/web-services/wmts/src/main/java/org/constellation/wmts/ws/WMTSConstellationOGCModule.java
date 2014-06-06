package org.constellation.wmts.ws;

import javax.inject.Named;

import org.constellation.ws.ConstellationOGCModule;

@Named
public class WMTSConstellationOGCModule implements ConstellationOGCModule {

    @Override
    public String getName() {
        return "WMTS";
    }

}
