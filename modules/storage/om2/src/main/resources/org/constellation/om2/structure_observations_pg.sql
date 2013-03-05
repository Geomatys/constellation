CREATE TABLE "version" (
    "number"   character varying(10) NOT NULL
);

INSERT INTO "version" VALUES ('1.0.0');
CREATE SCHEMA "om";


CREATE TABLE "om"."observations" (
    "id"                integer NOT NULL,
    "time_begin"        timestamp,
    "time_end"          timestamp,
    "observed_property" character varying(100),
    "procedure"         character varying(100),
    "foi"               character varying(100)
);

CREATE TABLE "om"."mesures" (
    "id_observation"    integer NOT NULL,
    "id"                integer NOT NULL,
    "time"              timestamp,
    "value"             double precision,
    "uom"               character varying(20),
    "field_type"        character varying(20),
    "field_name"        character varying(30),
    "field_definition"  character varying(100)
);

CREATE TABLE "om"."offerings" (
    "identifier"       character varying(100),
    "description"      character varying(100),
    "name"             character varying(100),
    "time_begin"       timestamp,
    "time_end"         timestamp,
    "procedure"        character varying(100)
);

CREATE TABLE "om"."offering_observed_properties" (
    "id_offering" character varying(100),
    "phenomenon"  character varying(100)
);

CREATE TABLE "om"."offering_foi" (
    "id_offering" character varying(100),
    "foi"         character varying(100)
);

CREATE TABLE "om"."observed_properties" (
    "id" character varying(100) NOT NULL
);

CREATE TABLE "om"."procedures" (
    "id"     character varying(100) NOT NULL,
    "shape"  bit varying(200)
);

CREATE TABLE "om"."sampling_features" (
    "id"               character varying(100) NOT NULL,
    "name"             character varying(100),
    "description"      character varying(100),
    "sampledfeature"   character varying(100),
    "shape"            bit varying(200),
    "crs"              integer
);


-- USED ONLY FOR V100 SOS --

CREATE TABLE "om"."components" (
    "phenomenon" character varying(100) NOT NULL,
    "component"  character varying(100) NOT NULL
);
