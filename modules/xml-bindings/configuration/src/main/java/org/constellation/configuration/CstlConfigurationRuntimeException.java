package org.constellation.configuration;



public class CstlConfigurationRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String errorCode = "constellation.configuration.error";
    
    public CstlConfigurationRuntimeException(String message) {
        super(message);
    }

    public CstlConfigurationRuntimeException(Throwable throwable) {
        super(throwable);
    }
    
    public CstlConfigurationRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public String getErrorCode() {
        return errorCode;
    }
    
    public CstlConfigurationRuntimeException withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }
}
