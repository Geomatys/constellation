package org.constellation.setup;


import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.SecurityManagerAdapter;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.image.io.SetupBIL;
import org.geotoolkit.internal.image.io.SetupGeoTiff;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.lang.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

import javax.imageio.ImageIO;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import java.util.EnumSet;

/**
 * Use this installer to initialize Geotk and copy a file-system configuration into db config.
 *
 * @author Alexis Manin (Geomatys)
 */
public class Installer implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Installer.class);

    
    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
            Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

        LOGGER.info( "=== Starting GeotoolKit ===");

        try {
            ConfigurationEngine.setSecurityManager(new SecurityManagerAdapter() {
                @Override
                public String getCurrentUserLogin() {
                    return "admin";
                }
            });

            Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

            //Initialize geotoolkit
            Installation.allowSystemPreferences = false;
            ImageIO.scanForPlugins();
            Setup.initialize(null);

            try {
                Class.forName("javax.media.jai.JAI");
            } catch (ClassNotFoundException ex) {
                LOGGER.error("JAI libraries are not in the classpath. Please install it.\n "
                        + ex.getLocalizedMessage(), ex);
            }
            LOGGER.info( "=== GeotoolKit successfully started ===");
        } catch(Exception ex) {
            LOGGER.info("=== GeotoolKit failed to start ===\n"+ex.getLocalizedMessage(), ex);
        }

        try {
            ConfigurationUtilities.FileToDBConfig(null);
        } catch (Exception e) {
            LOGGER.info( "Failed to copy file-system configuration. "+e.getLocalizedMessage());
        }
        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

        initMetrics(servletContextEvent.getServletContext(), disps);
    }

    
    /**
     * Initializes Metrics.
     */
    private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
    	LOGGER.debug("Initializing Metrics registries");
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE,
                METRIC_REGISTRY);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
                METRIC_REGISTRY);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                HEALTH_CHECK_REGISTRY);

        LOGGER.debug("Registering Metrics Filter");
        FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("webappMetricsFilter",
                new InstrumentedFilter());

        metricsFilter.addMappingForUrlPatterns(disps, true, "/*");
        metricsFilter.setAsyncSupported(true);

        LOGGER.debug("Registering Metrics Admin Servlet");
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
        LOGGER.debug("Web application destroyed");
        LOGGER.info( "=== Stopping GeotoolKit ===");
        try{
            Setup.shutdown();
            LOGGER.info("=== GeotoolKit successfully stopped ===");
        }catch(Exception ex){
            LOGGER.error("=== GeotoolKit failed to stop ===\n"+ex.getLocalizedMessage(), ex);
        }
    }
}
