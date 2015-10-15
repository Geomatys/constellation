package org.constellation.admin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.util.MetadataUtilities;
import org.constellation.business.IConfigurationBusiness;
import org.constellation.configuration.AppProperty;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.database.api.MetadataComplete;
import org.constellation.database.api.MetadataIOUtils;
import org.constellation.database.api.jooq.tables.pojos.Metadata;
import org.constellation.database.api.jooq.tables.pojos.MetadataBbox;
import org.constellation.database.api.jooq.tables.pojos.Property;
import org.constellation.database.api.jooq.tables.pojos.Service;
import org.constellation.database.api.repository.MetadataRepository;
import org.constellation.database.api.repository.PropertyRepository;
import org.constellation.database.api.repository.ServiceRepository;
import org.constellation.utils.CstlMetadatas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Primary
public class ConfigurationBusiness implements IConfigurationBusiness {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.admin");

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
        System.setProperty(key, value);
        // FIXME continue to save in database ?
        // create/update external configuration file to save preferences ?
        propertyRepository.save(new Property(key, value));
        // update metadata when service URL key is updated
        if (AppProperty.CSTL_SERVICE_URL.getKey().equals(key)) {
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
