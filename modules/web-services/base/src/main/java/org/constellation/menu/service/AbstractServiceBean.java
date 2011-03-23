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

import java.util.ArrayList;
import java.util.List;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.service.ServiceAdministrator;
import org.constellation.bean.I18NBean;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.geotoolkit.util.ArgumentChecks;

/**
 * Abstract JSF Bean for service administration interface.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class AbstractServiceBean extends I18NBean{

    private final Specification specification;
    private String newServiceName = "default";

    public AbstractServiceBean(final Specification specification) {
        ArgumentChecks.ensureNonNull("specification", specification);
        this.specification = specification;
        addBundle("org.constellation.menu.service.service");
    }

    public List<ServiceInstance> getInstances(){
        final InstanceReport report = ServiceAdministrator.listInstance(specification.name());
        final List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        for(Instance instance : report.getInstances()){
            instances.add(new ServiceInstance(instance));
        }
        return instances;
    }

    public String getNewServiceName() {
        return newServiceName;
    }

    public void setNewServiceName(final String newServiceName) {
        this.newServiceName = newServiceName;
    }

    public void createInstance(){
        if(newServiceName == null || newServiceName.isEmpty()){
            //unvalid name
            return;
        }

        final InstanceReport report = ServiceAdministrator.listInstance(specification.name());
        for(Instance instance : report.getInstances()){
            if(newServiceName.equals(instance.getName())){
                //an instance with this already exist
                return;
            }
        }

        ServiceAdministrator.newInstance(specification.name(), newServiceName);
    }

    public class ServiceInstance{

        private final Instance instance;

        public ServiceInstance(final Instance instance) {
            this.instance = instance;
        }

        public String getName(){
            return instance.getName();
        }

        public String getPath(){
            return ServiceAdministrator.getInstanceURL(specification.name(), instance.getName());
        }

        public String getStatusIcon(){
            switch(instance.getStatus()){
                case WORKING:   return "org.constellation.menu.provider.smallgreen.png.mfRes";
                case ERROR:     return "org.constellation.menu.provider.smallred.png.mfRes";
                default:        return "org.constellation.menu.provider.smallgray.png.mfRes";
            }
        }

        public void delete(){
            ServiceAdministrator.deleteInstance(specification.name(), instance.getName());
        }

        public void restart(){
            ServiceAdministrator.restartInstance(specification.name(), instance.getName());
        }

        public void edit(){
            //todo
        }

    }

}
