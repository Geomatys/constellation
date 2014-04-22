/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011-2014, Geomatys
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

package org.constellation.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;
import org.opengis.feature.type.Name;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class Tasks {
    
    private Tasks(){}
    
    
    /**
     * The returned list is a subset of what can be found with ProcessFinder.
     * But only process with simple types arguments are preserved.
     * 
     * @return List of all available process.
     */
    public static List<Name> listProcess(){
        final List<Name> names = new ArrayList<>();
        
        final Iterator<ProcessingRegistry> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessingRegistry factory = ite.next();            
            final String authorityCode = factory.getIdentification().getCitation()
                              .getIdentifiers().iterator().next().getCode();
            
            for(String processCode : factory.getNames()){
                names.add(new DefaultName(authorityCode, processCode));
            }            
        }
        
        return names;
    }

    /**
     * The returned list is a subset of what can be found with ProcessFinder.
     * But only process with simple types arguments are preserved.
     *
     * @param authorityCode
     * @return List of all available process.
     */
    public static List<String> listProcessForFactory(final String authorityCode){
        final List<String> names = new ArrayList<>();

        final Iterator<ProcessingRegistry> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessingRegistry factory = ite.next();
            final String currentAuthorityCode = factory.getIdentification().getCitation()
                              .getIdentifiers().iterator().next().getCode();
            if (currentAuthorityCode.equals(authorityCode)) {
                for(String processCode : factory.getNames()){
                    names.add(processCode);
                }
            }
        }
        return names;
    }

    public static List<String> listProcessFactory(){
        final List<String> names = new ArrayList<>();

        final Iterator<ProcessingRegistry> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessingRegistry factory = ite.next();
            names.add(factory.getIdentification().getCitation()
                              .getIdentifiers().iterator().next().getCode());
        }
        return names;
    }
    
}
