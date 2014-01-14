package org.constellation.dto;

import org.constellation.configuration.StyleBrief;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Garcia (Geomatys)
 * @author Fabien Bernard (Geomatys)
 */
@XmlRootElement
public class StyleListBrief {

    private List<StyleBrief> styles;

    public StyleListBrief() {
        this.styles = new ArrayList<>();
    }

    public StyleListBrief(final List<StyleBrief> styles) {
        this.styles = styles;
    }

    public List<StyleBrief> getStyles() {
        return styles;
    }

    public void setStyles(final List<StyleBrief> styles) {
        this.styles = styles;
    }
}
