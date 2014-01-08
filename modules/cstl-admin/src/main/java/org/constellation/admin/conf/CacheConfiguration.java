package org.constellation.admin.conf;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.support.NoOpCacheManager; 
import javax.annotation.PreDestroy;
import java.util.SortedSet;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);
    
    private CacheManager cacheManager;
    
    @PreDestroy
    public void destroy() {
        log.info("Remove caching metrics");
        SortedSet<String> names = WebConfigurer.METRIC_REGISTRY.getNames();
        for (String name : names) {
            WebConfigurer.METRIC_REGISTRY.remove(name);
        }

        log.info("Closing Cache manager");
    }

    @Bean
    public CacheManager cacheManager() {
        log.debug("No cache");
        cacheManager = new NoOpCacheManager();
        return cacheManager;
    }

    
}
