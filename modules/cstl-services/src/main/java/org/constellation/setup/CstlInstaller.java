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
package org.constellation.setup;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.apache.sis.util.logging.Logging;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
