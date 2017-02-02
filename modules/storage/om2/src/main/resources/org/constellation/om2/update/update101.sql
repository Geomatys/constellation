ALTER TABLE "$SCHEMAom"."procedures" ADD "pid" INTEGER;

CREATE SCHEMA "mesures";

CREATE TABLE "$SCHEMAom"."procedure_descriptions" (
    "procedure"         character varying(100) NOT NULL,
    "order"             integer NOT NULL,
    "field_name"        character varying(30) NOT NULL,
    "field_type"        character varying(30),
    "field_definition"  character varying(200),
    "uom"               character varying(20)
);

ALTER TABLE "$SCHEMAom"."procedure_descriptions" ADD CONSTRAINT procedure_descriptions_pk PRIMARY KEY ("procedure", "field_name");

ALTER TABLE "$SCHEMAom"."procedure_descriptions" ADD CONSTRAINT procedure_desc_fk FOREIGN KEY ("procedure") REFERENCES "$SCHEMAom"."procedures"("id");