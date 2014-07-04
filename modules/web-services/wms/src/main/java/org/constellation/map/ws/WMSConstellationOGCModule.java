package org.constellation.map.ws;

import javax.inject.Named;

import org.constellation.ws.ConstellationOGCModule;;

@Named
public class WMSConstellationOGCModule implements ConstellationOGCModule {

    @Override
    public String getName() {
        return "WMS";
    }

}
