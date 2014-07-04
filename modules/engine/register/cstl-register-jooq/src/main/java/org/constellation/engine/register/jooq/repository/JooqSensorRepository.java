package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.SENSOR;
import static org.constellation.engine.register.jooq.Tables.DATA;
import static org.constellation.engine.register.jooq.Tables.SENSORED_DATA;

import java.util.List;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Sensor;
import org.constellation.engine.register.helper.SensorHelper;
import org.constellation.engine.register.jooq.tables.records.SensorRecord;
import org.constellation.engine.register.repository.SensorRepository;
import org.springframework.stereotype.Component;

@Component
public class JooqSensorRepository extends AbstractJooqRespository<SensorRecord, Sensor> implements SensorRepository {

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
        return dsl.select().from(DATA).join(SENSORED_DATA).onKey()
                .where(SENSORED_DATA.SENSOR.eq(sensor.getId())).fetch().into(Data.class);
    }

    @Override
    public Sensor findByIdentifier(String identifier) {
        return dsl.select().from(SENSOR).where(SENSOR.IDENTIFIER.eq(identifier)).fetchOne().into(Sensor.class);
    }

    @Override
    public List<Sensor> getChildren(Sensor sensor) {
        return dsl.select().from(SENSOR).where(SENSOR.PARENT.eq(sensor.getIdentifier())).fetch().into(Sensor.class);
    }

    @Override
    public List<Sensor> findAll() {
        return dsl.select().from(SENSOR).fetch().into(Sensor.class);
    }

    @Override
    public void delete(String identifier) {
        dsl.delete(SENSOR).where(SENSOR.IDENTIFIER.eq(identifier)).execute();
    }

    @Override
    public void linkDataToSensor(Integer dataId , Integer sensorId) {
        dsl.insertInto(SENSORED_DATA).set(SENSORED_DATA.DATA, dataId).set(SENSORED_DATA.SENSOR, sensorId).execute();
    }

    @Override
    public void unlinkDataToSensor(Integer dataId, Integer sensorId) {
        dsl.delete(SENSORED_DATA).where(SENSORED_DATA.DATA.eq(dataId)).and(SENSORED_DATA.SENSOR.eq(sensorId)).execute();
    }

    @Override
    public Sensor create(Sensor sensor) {
        SensorRecord sensorRecord = dsl.newRecord(SENSOR);
        SensorHelper.copy(sensor,sensorRecord);
        sensorRecord.store();
        return sensorRecord.into(Sensor.class);
    }

    @Override
    public void update(Sensor sensor) {
        dsl.update(SENSOR)
                .set(SENSOR.IDENTIFIER, sensor.getIdentifier())
                .set(SENSOR.METADATA, sensor.getMetadata())
                .set(SENSOR.OWNER, sensor.getOwner())
                .set(SENSOR.PARENT, sensor.getParent())
                .set(SENSOR.TYPE, sensor.getType()).execute();
    }
}
