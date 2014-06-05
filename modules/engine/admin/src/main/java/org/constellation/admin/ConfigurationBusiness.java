package org.constellation.admin;

import java.io.File;

import org.constellation.configuration.ConfigDirectory;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationBusiness {

	File getConfigurationDirectory() {
		return ConfigDirectory.getConfigDirectory();
	}

	File getDataDirectory() {
		return ConfigDirectory.getDataDirectory();
	}

}
