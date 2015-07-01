package org.constellation.admin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.util.MetadataUtilities;
import org.constellation.api.PropertyConstants;
import org.constellation.business.IConfigurationBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.engine.register.MetadataComplete;
import org.constellation.engine.register.MetadataIOUtils;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataBbox;
import org.constellation.engine.register.jooq.tables.pojos.Property;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.engine.register.repository.MetadataRepository;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.utils.CstlMetadatas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Primary
public class ConfigurationBusiness implements IConfigurationBusiness {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationBusiness.class);

    @Autowired
    private PropertyRepository propertyRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private MetadataRepository metadataRepository;

    @Override
    public File getConfigurationDirectory() {
        return ConfigDirectory.getConfigDirectory();
    }

    @Override
    public File getDataDirectory() {
        return ConfigDirectory.getDataDirectory();
    }

    @Override
    public String getProperty(final String key) {
        return propertyRepository.getValue(key, null);
    }

    @Override
    @Transactional
    public void setProperty(final String key, final String value) {
        propertyRepository.save(new Property(key, value));
        // update metadata when service URL key is updated
            if (PropertyConstants.SERVICES_URL_KEY.equals(key)) {
                updateServiceUrlForMetadata(value);
            }
    }
    
    private void updateServiceUrlForMetadata(final String url) {
        try {
            final List<Service> records = serviceRepository.findAll();
            for (Service record : records) {
                final Metadata metadata = serviceRepository.getMetadata(record.getId());
                if (metadata != null) {
                    final DefaultMetadata servMeta = MetadataIOUtils.unmarshallMetadata(metadata.getMetadataIso());
                    CstlMetadatas.updateServiceMetadataURL(record.getIdentifier(), record.getType(), url, servMeta);
                    final String xml = MetadataIOUtils.marshallMetadataToString(servMeta);
                    final List<MetadataBbox> bboxes = MetadataUtilities.extractBbox(servMeta);
                    metadata.setMetadataId(servMeta.getFileIdentifier());
                    metadata.setMetadataIso(xml);
                    metadataRepository.update(new MetadataComplete(metadata, bboxes));
                }
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred updating service URL", ex);
        } 
    }

}
