/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.setup;


import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.SecurityManagerAdapter;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.geotoolkit.factory.Hints;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

/**
 * Use this installer to initialize Geotk and copy a file-system configuration into db config.
 *
 * @author Alexis Manin (Geomatys)
 */
public class CstlInstaller implements ServletContextListener {

    private static final Logger LOGGER = Logging.getLogger(CstlInstaller.class);

    
    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        LOGGER.log(Level.INFO, "=== Configuring Constellation ===");

        try {
            ConfigurationEngine.setSecurityManager(new SecurityManagerAdapter() {
                @Override
                public String getCurrentUserLogin() {
                    return "admin";
                }
            });

            ConfigurationUtilities.FileToDBConfig(null);

        } catch (Exception e) {
            LOGGER.info( "Failed to copy file-system configuration. "+e.getLocalizedMessage());
        }
        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

        initMetrics(servletContextEvent.getServletContext(), disps);

        LOGGER.log(Level.INFO, "=== Configuration ended ===");
    }

    
    /**
     * Initializes Metrics.
     */
    private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
    	LOGGER.log(Level.FINE, "Initializing Metrics registries");
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE,
                METRIC_REGISTRY);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
                METRIC_REGISTRY);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                HEALTH_CHECK_REGISTRY);

        LOGGER.log(Level.FINE, "Registering Metrics Filter");
        FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("webappMetricsFilter",
                new InstrumentedFilter());

        metricsFilter.addMappingForUrlPatterns(disps, true, "/*");
        metricsFilter.setAsyncSupported(true);

        LOGGER.log(Level.FINE, "Registering Metrics Admin Servlet");
        ServletRegistration.Dynamic metricsAdminServlet =
                servletContext.addServlet("metricsAdminServlet", new AdminServlet());

        metricsAdminServlet.addMapping("/metrics/*");
        metricsAdminServlet.setLoadOnStartup(2);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    	LOGGER.info("Destroying Web application");
        WebApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContextEvent.getServletContext());
        ConfigurableApplicationContext gwac = (ConfigurableApplicationContext) ac;
        gwac.close();
        LOGGER.log(Level.FINE, "Web application destroyed");
    }
}
