/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ImportedData {
	
	private String dataFile;
	private String metadataFile;
    private String dataType;
    private String verifyCRS;

    private List<String> codes = new ArrayList<>();
	
	public String getMetadataFile() {
		return metadataFile;
	}
	public void setMetadataFile(String metadataFile) {
		this.metadataFile = metadataFile;
	}
	public String getDataFile() {
		return dataFile;
	}
	public void setDataFile(String datafile) {
		this.dataFile = datafile;
	}

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getVerifyCRS() {
        return verifyCRS;
    }

    public void setVerifyCRS(String verifyCRS) {
        this.verifyCRS = verifyCRS;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

}
