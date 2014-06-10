package org.constellation.admin;

import org.springframework.context.ApplicationContext;


public class SpringHelper {

	private static ApplicationContext applicationContext;
	
	public static void setApplicationContext(
			ApplicationContext applicationContext) {
		SpringHelper.applicationContext = applicationContext;
	}
	
	public static void injectDependencies(
			Object object) {
		SpringHelper.applicationContext.getAutowireCapableBeanFactory().autowireBean(object);
	}
	

}
