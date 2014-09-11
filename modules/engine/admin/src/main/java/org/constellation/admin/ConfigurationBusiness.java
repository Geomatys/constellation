package org.constellation.admin;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.business.IConfigurationBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.engine.register.MetadataIOUtils;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.utils.CstlMetadatas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Primary
public class ConfigurationBusiness implements IConfigurationBusiness {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationBusiness.class);

    public static final String SERVICES_URL_KEY = "services.url";
     
    @Autowired
    private PropertyRepository propertyRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;

    public File getConfigurationDirectory() {
        return ConfigDirectory.getConfigDirectory();
    }

    public File getDataDirectory() {
        return ConfigDirectory.getDataDirectory();
    }

    public String getProperty(final String key) {
        return propertyRepository.getValue(key, null);
    }
    
    public void setProperty(final String key, final String value) {
        propertyRepository.save(new Property(key, value));
        // update metadata when service URL key is updated
            if (SERVICES_URL_KEY.equals(key)) {
                updateServiceUrlForMetadata(value);
            }
    }
    
    private void updateServiceUrlForMetadata(final String url) {
        try {
            final List<Service> records = serviceRepository.findAll();
            for (Service record : records) {
                if (record.getMetadataIso() != null) {
                    final DefaultMetadata servMeta = MetadataIOUtils.unmarshallMetadata(record.getMetadataIso());
                    CstlMetadatas.updateServiceMetadataURL(record.getIdentifier(), record.getType(), url, servMeta);
                    final String xml = MetadataIOUtils.marshallMetadataToString(servMeta);
                    record.setMetadataId(servMeta.getFileIdentifier());
                    record.setMetadataIso(xml);
                    serviceRepository.update(record);
                }
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred updating service URL", ex);
        } 
    }
    
    public static boolean setConfigPath(final String path) {
        // Set the new user directory
        if (path != null && !path.isEmpty()) {
            final File userDirectory = new File(path);
            if (!userDirectory.isDirectory()) {
                userDirectory.mkdir();
            }
            ConfigDirectory.setConfigDirectory(userDirectory);
            return true;
        }
        return false;
    }
    
    public static String getConfigPath() {
        return ConfigDirectory.getConfigDirectory().getPath();
    }

    public static Properties getMetadataTemplateProperties() {
        final File cstlDir = ConfigDirectory.getConfigDirectory();
        final File propFile = new File(cstlDir, "metadataTemplate.properties");
        final Properties prop = new Properties();
        if (propFile.exists()) {
            try {
                prop.load(new FileReader(propFile));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "IOException while loading metadata template properties file", ex);
            }
        }
        return prop;
    }
}
