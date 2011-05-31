
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

CREATE SCHEMA "sos";
CREATE SCHEMA "observation";


SET search_path = observation, pg_catalog;


CREATE TABLE "observation"."Distributions" (
    "name" character varying(20) NOT NULL,
    "scale" real,
    "offset" real,
    "log" boolean
);


CREATE TABLE "observation"."any_results" (
    "id_result" integer DEFAULT nextval(('observation.any_results_id_result_seq'::text)::regclass) NOT NULL,
    "reference" character varying,
    "values" character varying,
    "definition" character varying(20)
);


CREATE SEQUENCE "any_results_id_result_seq"
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


CREATE TABLE "observation"."any_scalars" (
    "id_datablock" character varying NOT NULL,
    "id_datarecord" character varying NOT NULL,
    "name" character varying NOT NULL,
    "definition" character varying,
    "type" character varying,
    "uom_code" character varying,
    "uom_href" character varying,
    "value" boolean
);

CREATE TABLE "observation"."components" (
    "composite_phenomenon" character varying NOT NULL,
    "component" character varying NOT NULL
);


CREATE TABLE "observation"."composite_phenomenons" (
    "id" character varying NOT NULL,
    "name" character varying,
    "description" character varying,
    "dimension" integer
);


CREATE TABLE "observation"."data_array_definition" (
    "id_array_definition" character varying(20) NOT NULL,
    "element_count" smallint NOT NULL,
    "elementType" character varying,
    "encoding" character varying(20)
);


CREATE TABLE "observation"."data_block_definitions" (
    "id" character varying NOT NULL,
    "encoding" character varying
);


CREATE TABLE "observation"."measurements" (
    "name" character varying NOT NULL,
    "description" character varying,
    "feature_of_interest" character varying,
    "procedure" character varying,
    "sampling_time_begin" timestamp without time zone,
    "sampling_time_end" timestamp without time zone,
    "result_definition" character varying,
    "observed_property" character varying,
    "result" character varying,
    "distribution" character varying,
    "feature_of_interest_point" character varying,
    "observed_property_composite" character varying
);


CREATE TABLE "observation"."measures" (
    "name" character varying NOT NULL,
    "uom" character varying,
    "value" real
);


CREATE TABLE "observation"."observations" (
    "name" character varying NOT NULL,
    "description" character varying,
    "feature_of_interest" character varying,
    "procedure" character varying,
    "sampling_time_begin" timestamp without time zone,
    "sampling_time_end" timestamp without time zone,
    "result_definition" character varying,
    "observed_property" character varying,
    "result" character varying,
    "distribution" character varying,
    "feature_of_interest_point" character varying,
    "observed_property_composite" character varying
);


CREATE TABLE "observation"."phenomenons" (
    "id" character varying NOT NULL,
    "name" character varying,
    "description" character varying
);

CREATE TABLE "observation"."process" (
    "name" character varying NOT NULL,
    "description" character varying
);


CREATE TABLE "observation"."references" (
    "id_reference" character varying NOT NULL,
    "actuate" character varying,
    "arcrole" character varying,
    "href" character varying,
    "role" character varying,
    "show" character varying,
    "title" character varying,
    "type" character varying,
    "owns" boolean
);

CREATE TABLE "observation"."sampling_features" (
    "id" character varying NOT NULL,
    "description" character varying,
    "name" character varying,
    "sampled_feature" character varying
);


CREATE TABLE "observation"."sampling_points" (
    "id" character varying NOT NULL,
    "description" character varying,
    "name" character varying,
    "sampled_feature" character varying,
    "point_id" character varying,
    "point_srsname" character varying,
    "point_srsdimension" integer,
    "x_value" double precision,
    "y_value" double precision
);


CREATE TABLE "observation"."simple_data_records" (
    "id_datablock" character varying NOT NULL,
    "id_datarecord" character varying NOT NULL,
    "definition" character varying,
    "fixed" boolean
);


CREATE TABLE "observation"."text_block_encodings" (
    "id_encoding" character varying NOT NULL,
    "token_separator" character varying(3),
    "block_separator" character varying(3),
    "decimal_separator" "char"
);

CREATE TABLE "observation"."unit_of_measures" (
    "id" character varying NOT NULL,
    "name" character varying,
    "quantity_type" character varying,
    "unit_system" character varying
);



CREATE TABLE "sos"."envelopes" (
    "id" character varying NOT NULL,
    "srs_name" character varying,
    "lower_corner_x" double precision,
    "lower_corner_y" double precision,
    "upper_corner_x" double precision,
    "upper_corner_y" double precision
);

CREATE TABLE "sos"."geographic_localisations" (
    "id" character varying NOT NULL,
    "the_geom" postgis.geometry
);


CREATE TABLE "sos"."observation_offerings" (
    "id" character varying NOT NULL,
    "name" character varying,
    "srs_name" character varying,
    "description" character varying,
    "event_time_begin" timestamp without time zone,
    "event_time_end" timestamp without time zone,
    "bounded_by" character varying,
    "response_format" character varying,
    "response_mode" character varying,
    "result_model_namespace" character varying,
    "result_model_localpart" character varying
);


CREATE TABLE "sos"."offering_phenomenons" (
    "id_offering" character varying,
    "phenomenon" character varying,
    "composite_phenomenon" character varying
);


CREATE TABLE "sos"."offering_procedures" (
    "id_offering" character varying NOT NULL,
    "procedure" character varying NOT NULL
);


CREATE TABLE "sos"."offering_response_modes" (
    "id_offering" character varying NOT NULL,
    "mode" character varying NOT NULL
);


CREATE TABLE "sos"."offering_sampling_features" (
    "id_offering" character varying NOT NULL,
    "sampling_feature" character varying NOT NULL
);


CREATE TABLE "sos"."projected_localisations" (
    "id" character varying NOT NULL,
    "the_geom" postgis.geometry
);


SET search_path = observation, pg_catalog;


ALTER TABLE ONLY "observation"."any_results" ADD CONSTRAINT any_pkey PRIMARY KEY (id_result);

ALTER TABLE ONLY "observation"."components" ADD CONSTRAINT composite_phenomenons_pk PRIMARY KEY (composite_phenomenon, component);

ALTER TABLE ONLY "observation"."composite_phenomenons" ADD CONSTRAINT composite_phenomenons_pkey PRIMARY KEY (id);

ALTER TABLE ONLY "observation"."data_array_definition" ADD CONSTRAINT data_array_pk PRIMARY KEY (id_array_definition);

ALTER TABLE ONLY "observation"."data_block_definitions" ADD CONSTRAINT data_block_definitions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY "observation"."any_scalars" ADD CONSTRAINT data_record_fields_pkey PRIMARY KEY (id_datarecord, name, id_datablock);

ALTER TABLE ONLY "observation".""Distributions"" ADD CONSTRAINT distributions_pkey PRIMARY KEY (name);

ALTER TABLE ONLY "observation"."measurements" ADD CONSTRAINT measurements_pkey PRIMARY KEY (name);

ALTER TABLE ONLY "observation"."measures" ADD CONSTRAINT measures_pkey PRIMARY KEY (name);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_pkey PRIMARY KEY (name);

ALTER TABLE ONLY "observation"."phenomenons" ADD CONSTRAINT phenomenons_pk PRIMARY KEY (id);

ALTER TABLE ONLY "observation"."process" ADD CONSTRAINT process_pkey PRIMARY KEY (name);

ALTER TABLE ONLY "observation".""references"" ADD CONSTRAINT references_pkey PRIMARY KEY (id_reference);

ALTER TABLE ONLY "observation"."sampling_features" ADD CONSTRAINT sampling_features_pk PRIMARY KEY (id);

ALTER TABLE ONLY "observation"."sampling_points" ADD CONSTRAINT sampling_points_pk PRIMARY KEY (id);

ALTER TABLE ONLY "observation"."simple_data_records" ADD CONSTRAINT simple_data_record_pkey PRIMARY KEY (id_datablock, id_datarecord);

ALTER TABLE ONLY "observation"."text_block_encodings" ADD CONSTRAINT text_block_encoding_pkey PRIMARY KEY (id_encoding);

ALTER TABLE ONLY "observation"."unit_of_measures" ADD CONSTRAINT unit_of_measures_pkey PRIMARY KEY (id);

SET search_path = sos, pg_catalog;

ALTER TABLE ONLY "sos"."envelopes" ADD CONSTRAINT envelopes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY "sos"."geographic_localisations" ADD CONSTRAINT geographic_pk PRIMARY KEY (id);

ALTER TABLE ONLY "sos"."observation_offerings" ADD CONSTRAINT observation_offerings_pkey PRIMARY KEY (id);

ALTER TABLE ONLY "sos"."offering_procedures" ADD CONSTRAINT offering_procedures_pkey PRIMARY KEY (id_offering, "procedure");

ALTER TABLE "sos"."offering_phenomenons" ADD CONSTRAINT offering_phenomenons_pk PRIMARY KEY(id_offering, phenomenon, composite_phenomenon);

ALTER TABLE ONLY "sos"."offering_response_modes" ADD CONSTRAINT offering_response_modes_pkey PRIMARY KEY (id_offering, "mode");

ALTER TABLE ONLY "sos"."offering_sampling_features" ADD CONSTRAINT offering_sampling_feature PRIMARY KEY (id_offering, sampling_feature);

ALTER TABLE ONLY "sos"."projected_localisations" ADD CONSTRAINT projected_pk PRIMARY KEY (id);


SET search_path = observation, pg_catalog;

CREATE INDEX fki_data_array_encoding_fk ON data_array_definition USING btree ("encoding");

ALTER TABLE ONLY "observation"."components" ADD CONSTRAINT components_component_fkey FOREIGN KEY (component) REFERENCES phenomenons(id);

ALTER TABLE ONLY "observation"."components" ADD CONSTRAINT components_composite_phenomenon_fkey FOREIGN KEY (composite_phenomenon) REFERENCES composite_phenomenons(id);

ALTER TABLE ONLY "observation"."data_array_definition" ADD CONSTRAINT data_array_encoding_fk FOREIGN KEY ("encoding") REFERENCES text_block_encodings(id_encoding);

ALTER TABLE ONLY "observation"."data_block_definitions" ADD CONSTRAINT data_block_definitions_encoding_fkey FOREIGN KEY ("encoding") REFERENCES text_block_encodings(id_encoding);

ALTER TABLE ONLY "observation"."any_scalars" ADD CONSTRAINT data_record_fields_id_datablock_fkey FOREIGN KEY (id_datablock, id_datarecord) REFERENCES simple_data_records(id_datablock, id_datarecord);

ALTER TABLE ONLY "observation"."measurements" ADD CONSTRAINT measurements_feature_of_interest_fkey FOREIGN KEY (feature_of_interest) REFERENCES sampling_features(id);

ALTER TABLE ONLY "observation"."measurements" ADD CONSTRAINT measurements_observed_property_fkey FOREIGN KEY (observed_property) REFERENCES phenomenons(id);

ALTER TABLE ONLY "observation"."measurements" ADD CONSTRAINT measurements_procedure_fkey FOREIGN KEY ("procedure") REFERENCES process(name);

ALTER TABLE ONLY "observation"."measurements" ADD CONSTRAINT measurements_result_fkey FOREIGN KEY (result) REFERENCES measures(name);

ALTER TABLE ONLY "observation"."measures" ADD CONSTRAINT measures_uom_fkey FOREIGN KEY (uom) REFERENCES unit_of_measures(id);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_distribution_fkey FOREIGN KEY (distribution) REFERENCES "Distributions"(name);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_feature_of_interest_fkey FOREIGN KEY (feature_of_interest) REFERENCES sampling_features(id);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_feature_of_interest_point_fkey FOREIGN KEY (feature_of_interest_point) REFERENCES sampling_points(id);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_observed_property_composite_fkey FOREIGN KEY (observed_property_composite) REFERENCES composite_phenomenons(id);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_observed_property_fkey FOREIGN KEY (observed_property) REFERENCES phenomenons(id);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_procedure_fkey FOREIGN KEY ("procedure") REFERENCES process(name);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_result_definition_fkey FOREIGN KEY (result_definition) REFERENCES data_block_definitions(id);

ALTER TABLE ONLY "observation"."observations" ADD CONSTRAINT observations_result_fkey FOREIGN KEY (result) REFERENCES any_results(id_result);

ALTER TABLE ONLY "observation"."any_results" ADD CONSTRAINT reference_pk FOREIGN KEY (reference) REFERENCES "references"(id_reference);

SET search_path = sos, pg_catalog;

ALTER TABLE ONLY "sos"."observation_offerings" ADD CONSTRAINT observation_offerings_bounded_by_fkey FOREIGN KEY (bounded_by) REFERENCES envelopes(id);

ALTER TABLE ONLY "sos"."offering_phenomenons" ADD CONSTRAINT offering_phenomenons_composite_phenomenon_fkey FOREIGN KEY (composite_phenomenon) REFERENCES observation.composite_phenomenons(id);

ALTER TABLE ONLY "sos"."offering_phenomenons" ADD CONSTRAINT offering_phenomenons_id_offering_fkey FOREIGN KEY (id_offering) REFERENCES observation_offerings(id);

ALTER TABLE ONLY "sos"."offering_phenomenons" ADD CONSTRAINT offering_phenomenons_phenomenon_fkey FOREIGN KEY (phenomenon) REFERENCES observation.phenomenons(id);

ALTER TABLE ONLY "sos"."offering_procedures" ADD CONSTRAINT offering_procedures_id_offering_fkey FOREIGN KEY (id_offering) REFERENCES observation_offerings(id);

ALTER TABLE ONLY "sos"."offering_sampling_features" ADD CONSTRAINT offering_sampling_features_id_offering_fkey FOREIGN KEY (id_offering) REFERENCES observation_offerings(id);



-- we insert the null phenomenons
INSERT INTO "observation"."phenomenons" VALUES ('', '', 'phenomenon null');
INSERT INTO "observation"."composite_phenomenons" VALUES ('', '', 'composite phenomenon null', 0);


