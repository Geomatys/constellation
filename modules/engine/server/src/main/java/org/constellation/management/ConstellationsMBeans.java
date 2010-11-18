/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.geotoolkit.util.logging.Logging;

/**
 * Entry class to access or register Constellation MBeans.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ConstellationsMBeans {

    private static final Logger LOGGER = Logging.getLogger(ConstellationsMBeans.class);
    private static boolean REGISTERED = false;

    private ConstellationsMBeans(){}

    /**
     * Register the ConstellationServerMBean.
     * Should be called once when constellation start.
     */
    public static synchronized void register(){
        if(REGISTERED) return;

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            //Providers MBean
            final Providers mbean = Providers.INSTANCE;
            final ObjectName name = new ObjectName(Providers.OBJECT_NAME);
            mbs.registerMBean(mbean, name);

            //Other MBean, later

        } catch (MalformedObjectNameException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstanceAlreadyExistsException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (MBeanRegistrationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (NotCompliantMBeanException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        //even if it fails we don't when to retry this, it won't be any better unless
        //the container has been reconfigured and restarted.
        REGISTERED = true;
    }

}
