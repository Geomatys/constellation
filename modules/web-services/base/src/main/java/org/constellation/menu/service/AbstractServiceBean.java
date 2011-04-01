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

package org.constellation.menu.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.service.ServiceAdministrator;
import org.constellation.bean.I18NBean;
import org.constellation.bean.MenuBean;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.event.CloseEvent;

/**
 * Abstract JSF Bean for service administration interface.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class AbstractServiceBean extends I18NBean{

    private static final Logger LOGGER = Logging.getLogger("org.constellation.bean");

    private final Specification specification;
    private final String configPage;
    private final String mainPage;
    private String newServiceName = "default";
    private ServiceInstance configuredInstance = null;
    private Object configurationObject = null;

    public AbstractServiceBean(final Specification specification, final String mainPage, final String configPage) {
        ArgumentChecks.ensureNonNull("specification", specification);
        ArgumentChecks.ensureNonNull("main page", mainPage);
        this.specification = specification;
        this.mainPage = MenuBean.toApplicationPath(mainPage);
        this.configPage = (configPage != null) ? MenuBean.toApplicationPath(configPage) : null;
        addBundle("org.constellation.menu.service.service");
    }

    public final String getSpecificationName(){
        return specification.name();
    }

    /**
     * @return List of all service instance of this specification.
     *      This list include both started and stopped instances.
     */
    public final List<ServiceInstance> getInstances(){
        final InstanceReport report = ServiceAdministrator.listInstance(getSpecificationName());
        final List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        for(Instance instance : report.getInstances()){
            instances.add(new ServiceInstance(instance));
        }
        Collections.sort(instances);
        return instances;
    }

    /**
     * Subclass may override this method to extend the ServiceInstance object.
     */
    protected ServiceInstance toServiceInstance(Instance inst){
        return new ServiceInstance(inst);
    }

    ////////////////////////////////////////////////////////////////////////////
    // CREATING NEW INSTANCE ///////////////////////////////////////////////////

    /**
     * @return String : name of the new service name to create.
     */
    public String getNewServiceName() {
        return newServiceName;
    }

    /**
     * Set the name of the new service to create.
     */
    public void setNewServiceName(final String newServiceName) {
        this.newServiceName = newServiceName;
    }

    /**
     * Create a new instance of this service.
     */
    public void createInstance(){
        if(newServiceName == null || newServiceName.isEmpty()){
            //unvalid name
            return;
        }

        final InstanceReport report = ServiceAdministrator.listInstance(getSpecificationName());
        for(Instance instance : report.getInstances()){
            if(newServiceName.equals(instance.getName())){
                //an instance with this already exist
                return;
            }
        }

        ServiceAdministrator.newInstance(getSpecificationName(), newServiceName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // CONFIGURE CURRENT INSTANCE //////////////////////////////////////////////

    /**
     * @return the main service page.
     * this is used to return from the configuration page.
     */
    public String getMainPage(){
        return mainPage;
    }

    /**
     * @return the currently configured instance.
     */
    public ServiceInstance getConfiguredInstance(){
        return configuredInstance;
    }

    /**
     * @return the configuration object of the edited instance.
     * Subclass should override this method to ensure this object is never null.
     */
    public Object getConfigurationObject(){
        return configurationObject;
    }

    /**
     * Called when the configuration dialog is closed.
     */
    public void configurationClosed(final CloseEvent event){
        //reset configured instance
        configuredInstance = null;
        configurationObject = null;
    }

    /**
     * Save the currently edited instance.
     * Subclass should override this method to make the proper save.
     */
    public void saveConfiguration(){
        if(configuredInstance != null){
            ServiceAdministrator.configureInstance(getSpecificationName(), configuredInstance.getName(), configurationObject);
            configuredInstance.restart();
        }
    }

    public class ServiceInstance implements Comparable<ServiceInstance>{

        protected Instance instance;

        public ServiceInstance(final Instance instance) {
            this.instance = instance;
        }

        public String getName(){
            return instance.getName();
        }

        /**
         * @return URL path to the running service.
         */
        public String getPath(){
            return ServiceAdministrator.getInstanceURL(getSpecificationName(), instance.getName());
        }

        public String getStatusIcon(){
            switch(instance.getStatus()){
                case WORKING:   return "org.constellation.menu.provider.smallgreen.png.mfRes";
                case ERROR:     return "org.constellation.menu.provider.smallred.png.mfRes";
                default:        return "org.constellation.menu.provider.smallgray.png.mfRes";
            }
        }

        /**
         * Set this instance as the currently configured one in for the property dialog.
         */
        public void config(){
            configuredInstance = this;
            configurationObject = ServiceAdministrator.getInstanceconfiguration(getSpecificationName(), instance.getName());

            if(configPage != null){
                final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                try {
                    //the session is not logged, redirect him to the authentication page
                    context.redirect(configPage);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }

        }

        public void start(){
            ServiceAdministrator.startInstance(getSpecificationName(), instance.getName());
            refresh();
        }
        public void stop(){
            ServiceAdministrator.stopInstance(getSpecificationName(), instance.getName());
            refresh();
        }

        public void delete(){
            ServiceAdministrator.deleteInstance(getSpecificationName(), instance.getName());
            refresh();
        }

        public void restart(){
            ServiceAdministrator.restartInstance(getSpecificationName(), instance.getName());
            refresh();
        }

        /**
         * Refresh this instance.
         */
        private void refresh(){
            final InstanceReport report = ServiceAdministrator.listInstance(getSpecificationName());
            for(final Instance inst : report.getInstances()){
                if(instance.getName().equals(inst.getName())){
                    instance = inst;
                    return;
                }
            }
        }

        @Override
        public int compareTo(final ServiceInstance other) {
            return getName().compareTo(other.getName());
        }

    }

}
