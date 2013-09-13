package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Garcia (Geomatys)
 * @author Fabien Bernard (Geomatys)
 */
@XmlRootElement
public class StyleListBean {

    private List<StyleBean> styles;

    public StyleListBean() {
        this.styles = new ArrayList<>();
    }

    public StyleListBean(final List<StyleBean> styles) {
        this.styles = styles;
    }

    public List<StyleBean> getStyles() {
        return styles;
    }

    public void setStyles(final List<StyleBean> styles) {
        this.styles = styles;
    }
}
