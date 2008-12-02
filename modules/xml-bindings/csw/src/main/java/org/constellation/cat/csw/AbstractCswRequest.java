/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.cat.csw;

/**
 * An interface containing the common methods to the different version of the operation GetRecords.
 *
 *  * @author Guilhem Legal
 */
public interface AbstractCswRequest {

    /**
     * Gets the value of the outputFormat property.
     * 
     */
    public String getOutputFormat();

    /**
     * Sets the value of the outputFormat property.
     * 
     */
    public void setOutputFormat(String value);
    
}
