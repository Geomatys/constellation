/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.management;

import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.geotoolkit.internal.SetupService;
import org.geotoolkit.util.logging.Logging;

/**
 * Registers and unregister constellation managed beans.
 *
 * @author Johann Sorel (Geomatys)
 */
public class SetupMBean implements SetupService{

    private static final Logger LOGGER = Logging.getLogger(SetupMBean.class);
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    @Override
    public synchronized void initialize(Properties properties, boolean reinit) {
        if(REGISTERED.get()) return;

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            final Providers mbean = Providers.INSTANCE;
            final ObjectName name = new ObjectName(Providers.OBJECT_NAME);
            mbs.registerMBean(mbean, name);

        } catch (MalformedObjectNameException ex) {
            LOGGER.log(Level.WARNING, "Malformed MBean name.",ex);
        } catch (InstanceAlreadyExistsException ex) {
            LOGGER.log(Level.WARNING, "MBean with given name already exists.");
        } catch (MBeanRegistrationException ex) {
            LOGGER.log(Level.WARNING, "Failed to register MBean.",ex);
        } catch (NotCompliantMBeanException ex) {
            LOGGER.log(Level.WARNING, "MBean is not valid.",ex);
        }

        //even if it fails we don't when to retry this, it won't be any better unless
        //the container has been reconfigured and restarted.
        REGISTERED.set(true);
    }

    @Override
    public synchronized void shutdown() {
        if(!REGISTERED.get()) return;

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName name = new ObjectName(Providers.OBJECT_NAME);
            mbs.unregisterMBean(name);

        } catch (MalformedObjectNameException ex) {
            LOGGER.log(Level.WARNING, "Malformed MBean name.",ex);
        } catch (InstanceNotFoundException ex) {
            LOGGER.log(Level.WARNING, "MBean with given name does not exist.");
        } catch (MBeanRegistrationException ex) {
            LOGGER.log(Level.WARNING, "Failed to unregister MBean.",ex);
        }

        //even if it fails we don't when to retry this, it won't be any better unless
        //the container has been reconfigured and restarted.
        REGISTERED.set(false);
    }

}
