package org.constellation.sos.io.om2;

import java.util.List;

/**
 * Created by christophe mourette on 28/11/14 for Geomatys.
 */
public class OM2ResultEventDTO {
    private String procedureID;
    private String decimalSeparator;
    private String blockSeparator;
    private String tokenSeparator;
    private List<String> headers;
    private String values;


    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public String getBlockSeparator() {
        return blockSeparator;
    }

    public void setBlockSeparator(String blockSeparator) {
        this.blockSeparator = blockSeparator;
    }

    public String getTokenSeparator() {
        return tokenSeparator;
    }

    public void setTokenSeparator(String tokenSeparator) {
        this.tokenSeparator = tokenSeparator;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getProcedureID() {
        return procedureID;
    }

    public void setProcedureID(String procedureID) {
        this.procedureID = procedureID;
    }
}
