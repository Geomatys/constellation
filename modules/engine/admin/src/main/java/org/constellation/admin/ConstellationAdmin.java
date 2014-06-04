package org.constellation.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;




@Component
public class ConstellationAdmin {
	
	@Autowired
	private ServiceAdmin serviceAdmin;
	
	@Autowired
	private DataAdmin dataAdmin;
	
    @Autowired
    private SensorAdmin sensorAdmin;
    
    @Autowired
    private StyleAdmin styleAdmin;
    
    @Autowired
    private MapContextAdmin mapContextAdmin;
    
    @Autowired
    private ProcessAdmin processAdmin;
    
    @Autowired
    private ConfigurationAdmin configurationAdmin;
	
	ServiceAdmin getServiceAdmin() {
		return serviceAdmin;
	}
	
	DataAdmin getDataAdmin() {
		return dataAdmin;
	}
	
	SensorAdmin getSensorAdmin(){
		return sensorAdmin;
	}
	
	StyleAdmin getStyleAdmin(){
		return styleAdmin;
	}
	
	MapContextAdmin getMapContextAdmin(){
		return mapContextAdmin;
	}
	
	ProcessAdmin getProcessAdmin(){
		return processAdmin;
	}
	
	ConfigurationAdmin getConfigurationAdmin(){
		return configurationAdmin;
	}
	

}
