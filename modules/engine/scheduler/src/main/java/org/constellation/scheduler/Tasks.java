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

package org.constellation.scheduler;

import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
