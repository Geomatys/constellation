/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.generic.nerc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author guilhem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name ="whatListsResponse")
public class WhatsListsResponse {

    private List<CodeTableType> codeTableType;

    public List<CodeTableType> getCodeTableType() {
        if (codeTableType == null)
            codeTableType = new ArrayList<CodeTableType>();
        return codeTableType;
    }

    public void setCodeTableType(List<CodeTableType> codeTableType) {
        this.codeTableType = codeTableType;
    }
    
    public CodeTableType getCodeTableFromKey(String key) {
        for (CodeTableType ct: getCodeTableType()) {
            String listKey = ct.getListKey();
            if (listKey.contains(key)) {
                if (listKey.indexOf("list/") != -1) {
                    listKey = listKey.substring(listKey.indexOf("list/") + 5);
                    listKey = listKey.substring(0, listKey.indexOf("/"));
                    if (listKey.equals(key))
                        return ct;
                }
                
            }
        }
        return null;
    }
}
