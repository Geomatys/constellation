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

package org.constellation.management;

import org.apache.sis.util.logging.Logging;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registers and unregister constellation managed beans.
 *
 * @author Johann Sorel (Geomatys)
 */
public class SetupMBean implements ServletContextListener {

    private static final Logger LOGGER = Logging.getLogger(SetupMBean.class);
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
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
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
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
