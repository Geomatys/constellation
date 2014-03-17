package org.constellation.ws;

/**
 * API to allow DI instantiation of OGC Servcie workers.
 * @author Olivier NOUGUIER
 *
 */
public interface DIEnhancer {

	/**
	 * Instantiate a new worker
	 * @param worker class of the worker
	 * @param id of the worker
	 * @return newly instantiated worker
	 */
    Worker enhance(Class<? extends Worker> worker, String id);
    
}
