package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.SENSOR;
import static org.constellation.engine.register.jooq.Tables.SENSORED_DATA;

import java.util.List;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Sensor;
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

}
