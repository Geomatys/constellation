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
package org.constellation.process.provider;

import org.constellation.admin.AdminDatabase;
import org.constellation.admin.AdminSession;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.sql.SQLException;
import java.util.logging.Level;

import static org.constellation.process.provider.DeleteProviderDescriptor.DELETE_DATA;
import static org.constellation.process.provider.DeleteProviderDescriptor.PROVIDER_ID;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Remove a provider from constellation.
 *
 * @author Quentin Boileau (Geomatys).
 * @author Fabien Bernard (Geomatys).
 */
public final class DeleteProvider extends AbstractCstlProcess{

    public DeleteProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * @throws ProcessException if the provider can't be found
     */
    @Override
    protected void execute() throws ProcessException {
        final String providerID  = value(PROVIDER_ID, inputParameters); // required
        final Boolean deleteData = value(DELETE_DATA, inputParameters); // optional

        // Retrieve or not the provider instance.
        Provider provider = LayerProviderProxy.getInstance().getProvider(providerID);
        if (provider == null) {
            provider = StyleProviderProxy.getInstance().getProvider(providerID);
        }
        if (provider == null) {
            throw new ProcessException("Unable to delete the provider with id \"" + providerID + "\". Not found.", this, null);
        }

        // Remove provider from its registry.
        if (provider instanceof LayerProvider) {
            if (deleteData != null && deleteData) {
                provider.removeAll();
            }
            LayerProviderProxy.getInstance().removeProvider((LayerProvider) provider);
        } else {
            StyleProviderProxy.getInstance().removeProvider((StyleProvider) provider);
        }

        // Remove provider from administration database.
        AdminSession session = null;
        try {
            session = AdminDatabase.createSession();
            session.deleteProvider(providerID);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating administration database after deleting the provider with id \"" + providerID + "\".", ex);
        } finally {
            if (session != null) session.close();
        }
    }

}
