/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.generic.database;

import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Queries {

    private HashMap<String, String> parameters;

    private QueryPropertyType main;

    private Static statique;
    
    private Single single;
    
    private MultiFixed multiFixed;

    public Queries() {

    }

    public Queries(QueryPropertyType main, Single single, MultiFixed multiFixed) {
        this.main       = main;
        this.single     = single;
        this.multiFixed = multiFixed;
    }

    public Queries(QueryPropertyType main, Single single, MultiFixed multiFixed, HashMap<String, String> parameters) {
        this.main       = main;
        this.single     = single;
        this.multiFixed = multiFixed;
        this.parameters = parameters;
    }

    public QueryPropertyType getMain() {
        return main;
    }
    
    public MultiFixed getMultiFixed() {
        return multiFixed;
    }
    
      public Single getSingle() {
        return single;
    }

    public void setSingle(Single single) {
        this.single = single;
    }

    /**
     * @return the parameters
     */
    public HashMap<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[Queries]");
        s.append("main: ").append(main).append('\n');
        if (parameters != null) {
            s.append("Parameters:\n");
            for (String paramName : parameters.keySet()) {
                s.append(paramName).append(':').append(parameters.get(paramName)).append('\n');
            }
        }
        s.append("main: ").append(main).append('\n');
        s.append("Single:").append(single).append('\n');
        s.append("Multifixed:").append(multiFixed).append('\n');
        return s.toString();
    }
    
    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Queries) {
            final Queries that = (Queries) object;

            return Utilities.equals(this.main, that.main)     &&
                   Utilities.equals(this.single, that.single) &&
                   Utilities.equals(this.multiFixed, that.multiFixed);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.main != null ? this.main.hashCode() : 0);
        hash = 53 * hash + (this.single != null ? this.single.hashCode() : 0);
        hash = 53 * hash + (this.multiFixed != null ? this.multiFixed.hashCode() : 0);
        return hash;
    }

    /**
     * @return the statique
     */
    public Static getStatique() {
        return statique;
    }

    /**
     * @param statique the statique to set
     */
    public void setStatique(Static statique) {
        this.statique = statique;
    }
}
