package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.PROPERTIES;

import java.util.List;

import org.constellation.engine.register.Property;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.PropertiesRecord;
import org.constellation.engine.register.repository.PropertyRepository;
import org.jooq.DeleteConditionStep;
import org.jooq.Record1;
import org.jooq.SelectQuery;
import org.springframework.stereotype.Component;

@Component
public class JooqPropertiesRepository extends AbstractJooqRespository<PropertiesRecord, Property> implements
        PropertyRepository {

    
    public JooqPropertiesRepository() {
        super(Property.class, PROPERTIES);
    }
    

    @Override
    public Property findOne(String key) {
        SelectQuery<PropertiesRecord> selectQuery = dsl.selectQuery(PROPERTIES);
        selectQuery.addConditions(PROPERTIES.KEY.equal(key));
        PropertiesRecord fetchOne = dsl.fetchOne(selectQuery);
        return maptoDTO(fetchOne);
    }

    @Override
    public List<? extends Property> findIn(List<String> keys) {
        SelectQuery<PropertiesRecord> selectQuery = dsl.selectQuery(PROPERTIES);
        selectQuery.addConditions(PROPERTIES.KEY.in(keys));
        return mapResult(selectQuery);
    }

    @Override
    public void save(Property prop) {
        PropertiesRecord newRecord = dsl.newRecord(PROPERTIES, prop);
        newRecord.store();
    }

    @Override
    public List<? extends Property> startWith(String string) {
        SelectQuery<PropertiesRecord> selectQuery = dsl.selectQuery(Tables.PROPERTIES);
        selectQuery.addConditions(Tables.PROPERTIES.KEY.like((string)));
        return selectQuery.fetch().into(Property.class);
    }

    @Override
    public void delete(Property property) {
        DeleteConditionStep<PropertiesRecord> deleteConditionStep = dsl.delete(PROPERTIES).where(
                PROPERTIES.KEY.eq(property.getKey()));
        deleteConditionStep.execute();

    }

    private Property maptoDTO(PropertiesRecord propertiesRecord) {
        if (propertiesRecord == null)
            return null;
        Property Property = new Property();
        Property.setKey(propertiesRecord.getKey());
        Property.setValue(propertiesRecord.getValue());
        return Property;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        Record1<String> fetchOne = dsl.select(PROPERTIES.VALUE).from(PROPERTIES).where(PROPERTIES.KEY.eq(key)).fetchOne();
        if (fetchOne == null)
            return defaultValue;
        return fetchOne.value1();
    }

}