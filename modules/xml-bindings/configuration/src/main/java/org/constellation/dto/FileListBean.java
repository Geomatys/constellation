/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
