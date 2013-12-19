package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.SortedMap;

/**
 * Pojo which have coverage name & crs decomposed list (horizontal, vertical, time for example)
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class CRSCoverageList {

    private int length;

    private SortedMap<String, String> selectedEPSGCode;

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public SortedMap<String, String> getSelectedEPSGCode() {
        return selectedEPSGCode;
    }

    public void setSelectedEPSGCode(final SortedMap<String, String> selectedEPSGCode) {
        this.selectedEPSGCode = selectedEPSGCode;
    }
}
