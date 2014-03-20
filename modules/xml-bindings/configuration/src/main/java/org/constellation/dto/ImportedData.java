package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ImportedData {
	
	private String dataFile;
	private String metadataFile;
	
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

}
