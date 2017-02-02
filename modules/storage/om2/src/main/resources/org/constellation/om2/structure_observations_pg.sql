CREATE SCHEMA "$SCHEMAom";

CREATE SCHEMA "$SCHEMAmesures";

CREATE TABLE "$SCHEMAom"."observations" (
    "identifier"        character varying(200) NOT NULL,
    "id"                integer NOT NULL,
    "time_begin"        timestamp,
    "time_end"          timestamp,
    "observed_property" character varying(200),
    "procedure"         character varying(200),
    "foi"               character varying(200)
);

CREATE TABLE "$SCHEMAom"."mesures" (
    "id_observation"    integer NOT NULL,
    "id"                integer NOT NULL,
    "time"              timestamp,
    "value"             character varying(100),
    "uom"               character varying(100),
    "field_type"        character varying(30),
    "field_name"        character varying(100),
    "field_definition"  character varying(200)
);

CREATE TABLE "$SCHEMAom"."offerings" (
    "identifier"       character varying(100) NOT NULL,
    "description"      character varying(200),
    "name"             character varying(200),
    "time_begin"       timestamp,
    "time_end"         timestamp,
    "procedure"        character varying(200)
);

CREATE TABLE "$SCHEMAom"."offering_observed_properties" (
    "id_offering" character varying(100) NOT NULL,
    "phenomenon"  character varying(200) NOT NULL
);

CREATE TABLE "$SCHEMAom"."offering_foi" (
    "id_offering" character varying(100) NOT NULL,
    "foi"         character varying(200) NOT NULL
);

CREATE TABLE "$SCHEMAom"."observed_properties" (
    "id" character varying(200) NOT NULL,
    "partial" boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE "$SCHEMAom"."procedures" (
    "id"     character varying(200) NOT NULL,
    "shape"  geometry,
    "crs"    integer,
    "pid"    integer NOT NULL
);

CREATE TABLE "$SCHEMAom"."procedure_descriptions" (
    "procedure"         character varying(200) NOT NULL,
    "order"             integer NOT NULL,
    "field_name"        character varying(30) NOT NULL,
    "field_type"        character varying(30),
    "field_definition"  character varying(200),
    "uom"               character varying(20)
);

CREATE TABLE "$SCHEMAom"."sampling_features" (
    "id"               character varying(200) NOT NULL,
    "name"             character varying(200),
    "description"      character varying(200),
    "sampledfeature"   character varying(200),
    "shape"            geometry,
    "crs"              integer
);


-- USED ONLY FOR V100 SOS --

CREATE TABLE "$SCHEMAom"."components" (
    "phenomenon" character varying(200) NOT NULL,
    "component"  character varying(200) NOT NULL
);


ALTER TABLE "$SCHEMAom"."observations" ADD CONSTRAINT observation_pk PRIMARY KEY ("id");

ALTER TABLE "$SCHEMAom"."mesures" ADD CONSTRAINT mesure_pk PRIMARY KEY ("id_observation", "id");

ALTER TABLE "$SCHEMAom"."offerings" ADD CONSTRAINT offering_pk PRIMARY KEY ("identifier");

ALTER TABLE "$SCHEMAom"."offering_observed_properties" ADD CONSTRAINT offering_op_pk PRIMARY KEY ("id_offering", "phenomenon");

ALTER TABLE "$SCHEMAom"."offering_foi" ADD CONSTRAINT offering_foi_pk PRIMARY KEY ("id_offering", "foi");

ALTER TABLE "$SCHEMAom"."observed_properties" ADD CONSTRAINT observed_properties_pk PRIMARY KEY ("id");

ALTER TABLE "$SCHEMAom"."procedures" ADD CONSTRAINT procedure_pk PRIMARY KEY ("id");

ALTER TABLE "$SCHEMAom"."procedure_descriptions" ADD CONSTRAINT procedure_descriptions_pk PRIMARY KEY ("procedure", "field_name");

ALTER TABLE "$SCHEMAom"."sampling_features" ADD CONSTRAINT sf_pk PRIMARY KEY ("id");

ALTER TABLE "$SCHEMAom"."components" ADD CONSTRAINT components_op_pk PRIMARY KEY ("phenomenon", "component");

ALTER TABLE "$SCHEMAom"."procedure_descriptions" ADD CONSTRAINT procedure_desc_fk FOREIGN KEY ("procedure") REFERENCES "$SCHEMAom"."procedures"("id");

ALTER TABLE "$SCHEMAom"."observations" ADD CONSTRAINT observation_op_fk FOREIGN KEY ("observed_property") REFERENCES "$SCHEMAom"."observed_properties"("id");

ALTER TABLE "$SCHEMAom"."observations" ADD CONSTRAINT observation_procedure_fk FOREIGN KEY ("procedure") REFERENCES "$SCHEMAom"."procedures"("id");

ALTER TABLE "$SCHEMAom"."observations" ADD CONSTRAINT observation_foi_fk FOREIGN KEY ("foi") REFERENCES "$SCHEMAom"."sampling_features"("id");

ALTER TABLE "$SCHEMAom"."mesures" ADD CONSTRAINT mesure_obs_fk FOREIGN KEY ("id_observation") REFERENCES "$SCHEMAom"."observations"("id");

ALTER TABLE "$SCHEMAom"."offerings" ADD CONSTRAINT offering_procedure_fk FOREIGN KEY ("procedure") REFERENCES "$SCHEMAom"."procedures"("id");

ALTER TABLE "$SCHEMAom"."offering_observed_properties" ADD CONSTRAINT offering_op_off_fk FOREIGN KEY ("id_offering") REFERENCES "$SCHEMAom"."offerings"("identifier");

ALTER TABLE "$SCHEMAom"."offering_observed_properties" ADD CONSTRAINT offering_op_op_fk FOREIGN KEY ("phenomenon") REFERENCES "$SCHEMAom"."observed_properties"("id");

ALTER TABLE "$SCHEMAom"."offering_foi" ADD CONSTRAINT offering_foi_off_fk FOREIGN KEY ("id_offering") REFERENCES "$SCHEMAom"."offerings"("identifier");

ALTER TABLE "$SCHEMAom"."offering_foi" ADD CONSTRAINT offering_foi_foi_fk FOREIGN KEY ("foi") REFERENCES "$SCHEMAom"."sampling_features"("id");

ALTER TABLE "$SCHEMAom"."components" ADD CONSTRAINT component_base_fk FOREIGN KEY ("phenomenon") REFERENCES "$SCHEMAom"."observed_properties"("id");

ALTER TABLE "$SCHEMAom"."components" ADD CONSTRAINT component_child_fk FOREIGN KEY ("component") REFERENCES "$SCHEMAom"."observed_properties"("id");