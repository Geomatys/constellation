/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.constellation.admin.service.ConstellationServer;
import org.jdesktop.swingx.JXHeader;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class JProvidersPanel extends JPanel{
    
    private final ConstellationServer server;

    public JProvidersPanel(final ConstellationServer server, final String providerId) {
        super(new BorderLayout());
        this.server = server;
        
        add(BorderLayout.NORTH, new JXHeader(providerId, ""));
                        
        final ParameterDescriptorGroup serviceDesc = (ParameterDescriptorGroup) server.providers.getServiceDescriptor(providerId);
        final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) serviceDesc.descriptor("source");
        
        
        ParameterDescriptorGroup choiceDesc;
        try{
            choiceDesc = (ParameterDescriptorGroup) sourceDesc.descriptor("choice");
        }catch(ParameterNotFoundException ex){
            return;
        }
        
        //convention : the different types of in the choice parameter
        final List<GeneralParameterDescriptor> types = choiceDesc.descriptors();
        
        
        
        // compose the panel ---------------------------------------------------
        final GridBagLayout layout = new GridBagLayout();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        
        final JPanel content = new JPanel(layout);
        final JScrollPane scroller = new JScrollPane(content);
        
        int i=0;
        for(GeneralParameterDescriptor type : types){
            constraints.gridy = i;
            content.add(new JProviderPanel(server, providerId, sourceDesc, type.getName().getCode()), constraints);
            i++;
        }
        
        //dummy component to fill space
        constraints.gridy = i;
        constraints.weighty = 1;
        content.add(new JComponent() {}, constraints);
        
        
        
        add(BorderLayout.CENTER, scroller);
    }
    
}
