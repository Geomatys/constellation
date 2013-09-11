package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@XmlRootElement
public class FileListBean {

    private List<FileBean> list;

    public List<FileBean> getList() {
        return list;
    }

    public void setList(final List<FileBean> list) {
        this.list = list;
    }
}
