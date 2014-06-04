package org.constellation.admin;

import org.constellation.engine.register.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataAdmin {
	
	@Autowired
    private DataRepository dataRepository;
	

}
