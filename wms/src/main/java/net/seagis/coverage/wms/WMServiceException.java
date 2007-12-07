package net.seagis.coverage.wms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import net.opengis.ogc.ServiceExceptionReport;
import net.opengis.ogc.ServiceExceptionType;
/**
 *
 * @author legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WMServiceException", namespace="http://wms.geomatys.fr/")
public class WMServiceException extends Exception {

    /**
     * An OGC Web Service exception report
     */
    private ServiceExceptionReport exception;
    
    public WMServiceException() {
        super();
        exception = new ServiceExceptionReport();
        this.setStackTrace(new StackTraceElement[0]);
    }
            
    public WMServiceException(String message, WMSExceptionCode code) {
        super(message);
        this.exception = new ServiceExceptionReport();
        ServiceExceptionType et = new ServiceExceptionType(message, code.toString());
        exception.getServiceException().add(et);
        this.setStackTrace(new StackTraceElement[0]);
    }
    
    public ServiceExceptionReport getException() {
        return exception;
    }
    
    /**
     * adds a coded exception to this exception with code, locator and messages as parameters
     * 
     * @param code
     *        WMSExceptionCode of the added exception
     * @param locator
     *        String locator of this exception
     * @param messages
     *        String[] messages of this exception
     */
    public void addCodedException(WMSExceptionCode code, String locator, String message) {
        ServiceExceptionType et = new ServiceExceptionType(message, code.toString());
        if (locator != null)
            et.setLocator(locator);
       
        exception.getServiceException().add(et);
    }

}
