package org.constellation.engine.register;

public class ConstellationPersistenceException extends RuntimeException {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ConstellationPersistenceException(ReflectiveOperationException e) {
        super(e);
    }
}
