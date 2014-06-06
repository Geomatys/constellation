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
package org.constellation.swing;

import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.DataSourceType;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class JServiceEditionPane extends JPanel {

    protected static final Logger LOGGER = Logging.getLogger(JServiceEditionPane.class);

    //icones
    protected static final ImageIcon ICON_EDIT = new ImageIcon(JServiceMapEditPane.class.getResource("/org/constellation/swing/edit.png"));
    protected static final ImageIcon ICON_DELETE = new ImageIcon(JServiceMapEditPane.class.getResource("/org/constellation/swing/edit_remove.png"));
    
    public JServiceEditionPane() {
        setLayout(new BorderLayout());
    }
    
    /**
     * Return the configuration of the service.
     * @return 
     */
    public abstract AbstractConfigurationObject getConfiguration();
    
    public abstract DataSourceType getDatasourceType();
}
