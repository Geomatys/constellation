package org.constellation.metadata;

import javax.inject.Named;

import org.constellation.ws.ConstellationOGCModule;

@Named
public class CSWConstellationOGCModule implements ConstellationOGCModule {

    @Override
    public String getName() {
        return "CSW";
    }

}
