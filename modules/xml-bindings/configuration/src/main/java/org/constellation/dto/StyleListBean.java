package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@XmlRootElement
public class StyleListBean {

    private List<StyleBean> styles;

    public List<StyleBean> getStyles() {
        return styles;
    }

    public void setStyles(final List<StyleBean> styles) {
        this.styles = styles;
    }
}
