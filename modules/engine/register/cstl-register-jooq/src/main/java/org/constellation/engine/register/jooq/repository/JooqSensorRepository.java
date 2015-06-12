package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Sensor;
import org.constellation.engine.register.jooq.tables.records.SensorRecord;
import org.constellation.engine.register.pojo.SensorReference;
import org.constellation.engine.register.repository.SensorRepository;
import org.jooq.Field;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.constellation.engine.register.jooq.Tables.DATA;
import static org.constellation.engine.register.jooq.Tables.SENSOR;
import static org.constellation.engine.register.jooq.Tables.SENSORED_DATA;

@Component
public class JooqSensorRepository extends AbstractJooqRespository<SensorRecord, Sensor> implements SensorRepository {

    public static final Field[] REFERENCE_FIELDS = new Field[]{
            SENSOR.ID.as("id"),
            SENSOR.IDENTIFIER.as("identifier")};


    public JooqSensorRepository() {
        super(Sensor.class, SENSOR);
    }

    @Override
    public List<String> getLinkedSensors(Data data) {
        return dsl.select(SENSOR.IDENTIFIER).from(SENSOR).join(SENSORED_DATA).onKey()
                .where(SENSORED_DATA.DATA.eq(data.getId())).fetch(SENSOR.IDENTIFIER);
    }
    
    @Override
    public List<Data> getLinkedDatas(Sensor sensor) {
        return dsl.select(DATA.fields()).from(DATA).join(SENSORED_DATA).onKey()
                .where(SENSORED_DATA.SENSOR.eq(sensor.getId())).fetchInto(Data.class);
    }

    @Override
    public Sensor findByIdentifier(String identifier) {
        return dsl.select().from(SENSOR).where(SENSOR.IDENTIFIER.eq(identifier)).fetchOneInto(Sensor.class);
    }

    @Override
    public List<Sensor> getChildren(Sensor sensor) {
        return dsl.select().from(SENSOR).where(SENSOR.PARENT.eq(sensor.getIdentifier())).fetchInto(Sensor.class);
    }

    @Override
    public List<Sensor> findAll() {
        return dsl.select().from(SENSOR).fetchInto(Sensor.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(String identifier) {
        dsl.delete(SENSOR).where(SENSOR.IDENTIFIER.eq(identifier)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void linkDataToSensor(Integer dataId , Integer sensorId) {
        dsl.insertInto(SENSORED_DATA).set(SENSORED_DATA.DATA, dataId).set(SENSORED_DATA.SENSOR, sensorId).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void unlinkDataToSensor(Integer dataId, Integer sensorId) {
        dsl.delete(SENSORED_DATA).where(SENSORED_DATA.DATA.eq(dataId)).and(SENSORED_DATA.SENSOR.eq(sensorId)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Sensor create(Sensor sensor) {
        SensorRecord sensorRecord = dsl.newRecord(SENSOR);
        sensorRecord.from(sensor);
        sensorRecord.store();
        return sensorRecord.into(Sensor.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(Sensor sensor) {
        dsl.update(SENSOR)
                .set(SENSOR.IDENTIFIER, sensor.getIdentifier())
                .set(SENSOR.METADATA, sensor.getMetadata())
                .set(SENSOR.OWNER, sensor.getOwner())
                .set(SENSOR.PARENT, sensor.getParent())
                .set(SENSOR.TYPE, sensor.getType())
                .set(SENSOR.DATE, sensor.getDate())
                .where(SENSOR.ID.eq(sensor.getId()))
                .execute();
    }

    @Override
    public boolean existsById(int sensorId) {
        return dsl.selectCount().from(SENSOR)
                .where(SENSOR.ID.eq(sensorId))
                .fetchOne(0, Integer.class) > 0;
    }

    @Override
    public boolean existsByIdentifier(String sensorId) {
        return dsl.selectCount().from(SENSOR)
                .where(SENSOR.IDENTIFIER.eq(sensorId))
                .fetchOne(0, Integer.class) > 0;
    }

    @Override
    public List<SensorReference> fetchByDataId(int dataId) {
        return dsl.select(REFERENCE_FIELDS).from(SENSOR)
                .join(SENSORED_DATA).on(SENSORED_DATA.SENSOR.eq(SENSOR.ID))
                .where(SENSORED_DATA.DATA.eq(dataId))
                .fetchInto(SensorReference.class);
    }
}
