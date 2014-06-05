package org.constellation.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Constellation {

	@Autowired
	private ServiceBusiness serviceBusiness;

	@Autowired
	private DataBusiness dataBusiness;

	@Autowired
	private SensorBusiness sensorBusiness;

	@Autowired
	private StyleBusiness styleBusiness;

	@Autowired
	private MapContextBusiness mapContextBusiness;

	@Autowired
	private ProcessBusiness processBusiness;

	@Autowired
	private ConfigurationBusiness configurationBusiness;

	ServiceBusiness getServiceBusiness() {
		return serviceBusiness;
	}

	DataBusiness getDataBusiness() {
		return dataBusiness;
	}

	SensorBusiness getSensorBusiness() {
		return sensorBusiness;
	}

	StyleBusiness getStyleBusiness() {
		return styleBusiness;
	}

	MapContextBusiness getMapContextBusiness() {
		return mapContextBusiness;
	}

	ProcessBusiness getProcessBusiness() {
		return processBusiness;
	}

	ConfigurationBusiness getConfigurationBusiness() {
		return configurationBusiness;
	}

}
