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
package org.constellation.process.provider.style;

import org.constellation.admin.dao.Session;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.sql.SQLException;
import java.util.logging.Level;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor.PROVIDER_ID;
import static org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor.STYLE_ID;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Remove a style from an existing style provider.
 *
 * @author Quentin Boileau (Geomatys).
 * @author Bernard Fabien (Geomatys).
 */
public class DeleteStyleToStyleProvider extends AbstractCstlProcess {

    public DeleteStyleToStyleProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * @throws ProcessException if the provider or the style can't be found
     */
    @Override
    protected void execute() throws ProcessException {
        final String providerID = value(PROVIDER_ID, inputParameters); // required
        final String styleName  = value(STYLE_ID,    inputParameters); // required

        if (isBlank(styleName)) {
            throw new ProcessException("Unable to delete the style named \"" + styleName + "\". Style name can't be empty/blank.", this, null);
        }

        // Retrieve or not the provider instance.
        final StyleProvider provider = StyleProviderProxy.getInstance().getProvider(providerID);
        if (provider == null) {
            throw new ProcessException("Unable to delete the style named \"" + styleName + "\". Provider with id \"" + providerID + "\" not found.", this, null);
        }

        // Remove style from provider.
        provider.remove(styleName);

        // Remove style from administration database.
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.deleteStyle(styleName, providerID);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating administration database after deleting the style named \"" + styleName + "\".", ex);
        } finally {
            if (session != null) session.close();
        }
    }
}
