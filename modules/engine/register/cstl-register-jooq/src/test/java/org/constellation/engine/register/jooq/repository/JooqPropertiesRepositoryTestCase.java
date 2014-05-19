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
package org.constellation.engine.register.jooq.repository;

import java.util.ArrayList;
import java.util.List;

import org.constellation.engine.register.Property;
import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.PropertyRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqPropertiesRepositoryTestCase extends AbstractJooqTestTestCase {

    
    @Autowired
    private PropertyRepository propertyRepository;
    
    @Test
    public void all() {
        dump(propertyRepository.findAll());
        
        List<String> keys = new ArrayList<String>();
        keys.add("test");
        
        dump(propertyRepository.findIn(keys));
        
        
    }
    
    @Test
    public void getValue() {
        String value = propertyRepository.getValue("test.notfound.property", "blurp");
        Assert.assertEquals("Default value is not matching", "blurp", value);
    }
    
    @Test
    public void save() {
        Property dto = new Property();
        dto.setKey("test");
        dto.setValue("value");
        propertyRepository.save(dto);
    }

    @Test
    public void delete() {
        Property dto = new Property();
        dto.setKey("test");
        dto.setValue("value");
        propertyRepository.delete(dto);
    }

}
