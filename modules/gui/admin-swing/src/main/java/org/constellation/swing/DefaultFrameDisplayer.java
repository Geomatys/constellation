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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JDialog;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultFrameDisplayer implements FrameDisplayer {

    @Override
    public void display(final JComponent edit) {
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setContentPane(edit);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        final PropertyChangeListener cl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("update".equals(evt.getPropertyName())) {
                    dialog.dispose();
                }
            }
        };
        edit.addPropertyChangeListener(cl);

        dialog.setVisible(true);
    }
    
}
