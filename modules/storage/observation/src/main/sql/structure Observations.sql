--
-- PostgreSQL database dump
--

-- Started on 2008-04-21 15:22:39 CEST

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 7 (class 2615 OID 73611)
-- Name: observation; Type: SCHEMA; Schema: -; Owner: -
--
CREATE SCHEMA sos;
CREATE SCHEMA observation;





SET search_path = observation, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1804 (class 1259 OID 73748)
-- Dependencies: 7
-- Name: Distributions; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE "Distributions" (
    name character varying(20) NOT NULL,
    scale real,
    "offset" real,
    log boolean
);


--
-- TOC entry 1816 (class 1259 OID 73833)
-- Dependencies: 2163 7
-- Name: any_results; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE any_results (
    id_result integer DEFAULT nextval(('observation.any_results_id_result_seq'::text)::regclass) NOT NULL,
    reference character varying,
    "values" character varying,
    definition character varying(20)
);


--
-- TOC entry 1817 (class 1259 OID 73900)
-- Dependencies: 1816 7
-- Name: any_results_id_result_seq; Type: SEQUENCE; Schema: observation; Owner: -
--

CREATE SEQUENCE any_results_id_result_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1800 (class 1259 OID 73728)
-- Dependencies: 7
-- Name: any_scalars; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE any_scalars (
    id_datablock character varying NOT NULL,
    id_datarecord character varying NOT NULL,
    name character varying NOT NULL,
    definition character varying,
    "type" character varying,
    uom_code character varying,
    uom_href character varying,
    value boolean
);


--
-- TOC entry 1801 (class 1259 OID 73733)
-- Dependencies: 7
-- Name: components; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE components (
    composite_phenomenon character varying NOT NULL,
    component character varying NOT NULL
);


--
-- TOC entry 1802 (class 1259 OID 73738)
-- Dependencies: 7
-- Name: composite_phenomenons; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE composite_phenomenons (
    id character varying NOT NULL,
    name character varying,
    description character varying,
    dimension integer
);


--
-- TOC entry 1818 (class 1259 OID 82894)
-- Dependencies: 7
-- Name: data_array_definition; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE data_array_definition (
    id_array_definition character varying(20) NOT NULL,
    element_count smallint NOT NULL,
    "elementType" character varying,
    "encoding" character varying(20)
);


--
-- TOC entry 1803 (class 1259 OID 73743)
-- Dependencies: 7
-- Name: data_block_definitions; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE data_block_definitions (
    id character varying NOT NULL,
    "encoding" character varying
);


--
-- TOC entry 1805 (class 1259 OID 73750)
-- Dependencies: 7
-- Name: measurements; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE measurements (
    name character varying NOT NULL,
    description character varying,
    feature_of_interest character varying,
    "procedure" character varying,
    sampling_time_begin timestamp without time zone,
    sampling_time_end timestamp without time zone,
    result_definition character varying,
    observed_property character varying,
    result character varying,
    distribution character varying,
    feature_of_interest_point character varying,
    observed_property_composite character varying
);


--
-- TOC entry 1806 (class 1259 OID 73755)
-- Dependencies: 7
-- Name: measures; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE measures (
    name character varying NOT NULL,
    uom character varying,
    value real
);


--
-- TOC entry 1815 (class 1259 OID 73828)
-- Dependencies: 7
-- Name: observations; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE observations (
    name character varying NOT NULL,
    description character varying,
    feature_of_interest character varying,
    "procedure" character varying,
    sampling_time_begin timestamp without time zone,
    sampling_time_end timestamp without time zone,
    result_definition character varying,
    observed_property character varying,
    result character varying,
    distribution character varying,
    feature_of_interest_point character varying,
    observed_property_composite character varying
);


--
-- TOC entry 1807 (class 1259 OID 73760)
-- Dependencies: 7
-- Name: phenomenons; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE phenomenons (
    id character varying NOT NULL,
    name character varying,
    description character varying
);


--
-- TOC entry 1808 (class 1259 OID 73765)
-- Dependencies: 7
-- Name: process; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE process (
    name character varying NOT NULL,
    description character varying
);


--
-- TOC entry 1809 (class 1259 OID 73770)
-- Dependencies: 7
-- Name: references; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE "references" (
    id_reference character varying NOT NULL,
    actuate character varying,
    arcrole character varying,
    href character varying,
    "role" character varying,
    "show" character varying,
    title character varying,
    "type" character varying,
    owns boolean
);


--
-- TOC entry 1810 (class 1259 OID 73775)
-- Dependencies: 7
-- Name: sampling_features; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE sampling_features (
    id character varying NOT NULL,
    description character varying,
    name character varying,
    sampled_feature character varying
);


--
-- TOC entry 1811 (class 1259 OID 73780)
-- Dependencies: 7
-- Name: sampling_points; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE sampling_points (
    id character varying NOT NULL,
    description character varying,
    name character varying,
    sampled_feature character varying,
    point_id character varying,
    point_srsname character varying,
    point_srsdimension integer,
    x_value double precision,
    y_value double precision
);


--
-- TOC entry 1812 (class 1259 OID 73785)
-- Dependencies: 7
-- Name: simple_data_records; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE simple_data_records (
    id_datablock character varying NOT NULL,
    id_datarecord character varying NOT NULL,
    definition character varying,
    fixed boolean
);


--
-- TOC entry 1813 (class 1259 OID 73790)
-- Dependencies: 7
-- Name: text_block_encodings; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE text_block_encodings (
    id_encoding character varying NOT NULL,
    token_separator character varying(3),
    block_separator character varying(3),
    decimal_separator "char"
);


--
-- TOC entry 1814 (class 1259 OID 73795)
-- Dependencies: 7
-- Name: unit_of_measures; Type: TABLE; Schema: observation; Owner: -; Tablespace: 
--

CREATE TABLE unit_of_measures (
    id character varying NOT NULL,
    name character varying,
    quantity_type character varying,
    unit_system character varying
);



SET search_path = sos, pg_catalog;

--
-- TOC entry 1793 (class 1259 OID 73693)
-- Dependencies: 8
-- Name: envelopes; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE envelopes (
    id character varying NOT NULL,
    srs_name character varying,
    lower_corner_x double precision,
    lower_corner_y double precision,
    upper_corner_x double precision,
    upper_corner_y double precision
);


--
-- TOC entry 1799 (class 1259 OID 73723)
-- Dependencies: 8 687
-- Name: geographic_localisations; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE geographic_localisations (
    id character varying NOT NULL,
    the_geom postgis.geometry
);


--
-- TOC entry 1792 (class 1259 OID 73688)
-- Dependencies: 8
-- Name: observation_offerings; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE observation_offerings (
    id character varying NOT NULL,
    name character varying,
    srs_name character varying,
    description character varying,
    event_time_begin timestamp without time zone,
    event_time_end timestamp without time zone,
    bounded_by character varying,
    response_format character varying,
    response_mode character varying,
    result_model_namespace character varying,
    result_model_localpart character varying
);


--
-- TOC entry 1794 (class 1259 OID 73698)
-- Dependencies: 8
-- Name: offering_phenomenons; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE offering_phenomenons (
    id_offering character varying,
    phenomenon character varying,
    composite_phenomenon character varying
);


--
-- TOC entry 1795 (class 1259 OID 73703)
-- Dependencies: 8
-- Name: offering_procedures; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE offering_procedures (
    id_offering character varying NOT NULL,
    "procedure" character varying NOT NULL
);


--
-- TOC entry 1796 (class 1259 OID 73708)
-- Dependencies: 8
-- Name: offering_response_modes; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE offering_response_modes (
    id_offering character varying NOT NULL,
    "mode" character varying NOT NULL
);


--
-- TOC entry 1797 (class 1259 OID 73713)
-- Dependencies: 8
-- Name: offering_sampling_features; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE offering_sampling_features (
    id_offering character varying NOT NULL,
    sampling_feature character varying NOT NULL
);


--
-- TOC entry 1798 (class 1259 OID 73718)
-- Dependencies: 687 8
-- Name: projected_localisations; Type: TABLE; Schema: sos; Owner: -; Tablespace: 
--

CREATE TABLE projected_localisations (
    id character varying NOT NULL,
    the_geom postgis.geometry
);


SET search_path = observation, pg_catalog;

--
-- TOC entry 2215 (class 2606 OID 73909)
-- Dependencies: 1816 1816
-- Name: any_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY any_results
    ADD CONSTRAINT any_pkey PRIMARY KEY (id_result);


--
-- TOC entry 2185 (class 2606 OID 73911)
-- Dependencies: 1801 1801 1801
-- Name: composite_phenomenons_pk; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY components
    ADD CONSTRAINT composite_phenomenons_pk PRIMARY KEY (composite_phenomenon, component);


--
-- TOC entry 2187 (class 2606 OID 73913)
-- Dependencies: 1802 1802
-- Name: composite_phenomenons_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY composite_phenomenons
    ADD CONSTRAINT composite_phenomenons_pkey PRIMARY KEY (id);


--
-- TOC entry 2217 (class 2606 OID 82900)
-- Dependencies: 1818 1818
-- Name: data_array_pk; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_array_definition
    ADD CONSTRAINT data_array_pk PRIMARY KEY (id_array_definition);


--
-- TOC entry 2189 (class 2606 OID 73915)
-- Dependencies: 1803 1803
-- Name: data_block_definitions_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_block_definitions
    ADD CONSTRAINT data_block_definitions_pkey PRIMARY KEY (id);


--
-- TOC entry 2183 (class 2606 OID 73917)
-- Dependencies: 1800 1800 1800 1800
-- Name: data_record_fields_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY any_scalars
    ADD CONSTRAINT data_record_fields_pkey PRIMARY KEY (id_datarecord, name, id_datablock);


--
-- TOC entry 2191 (class 2606 OID 73919)
-- Dependencies: 1804 1804
-- Name: distributions_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Distributions"
    ADD CONSTRAINT distributions_pkey PRIMARY KEY (name);


--
-- TOC entry 2193 (class 2606 OID 73921)
-- Dependencies: 1805 1805
-- Name: measurements_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY measurements
    ADD CONSTRAINT measurements_pkey PRIMARY KEY (name);


--
-- TOC entry 2195 (class 2606 OID 73923)
-- Dependencies: 1806 1806
-- Name: measures_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY measures
    ADD CONSTRAINT measures_pkey PRIMARY KEY (name);


--
-- TOC entry 2213 (class 2606 OID 73925)
-- Dependencies: 1815 1815
-- Name: observations_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_pkey PRIMARY KEY (name);


--
-- TOC entry 2197 (class 2606 OID 73927)
-- Dependencies: 1807 1807
-- Name: phenomenons_pk; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY phenomenons
    ADD CONSTRAINT phenomenons_pk PRIMARY KEY (id);


--
-- TOC entry 2199 (class 2606 OID 73929)
-- Dependencies: 1808 1808
-- Name: process_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY process
    ADD CONSTRAINT process_pkey PRIMARY KEY (name);


--
-- TOC entry 2201 (class 2606 OID 73931)
-- Dependencies: 1809 1809
-- Name: references_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "references"
    ADD CONSTRAINT references_pkey PRIMARY KEY (id_reference);


--
-- TOC entry 2203 (class 2606 OID 73933)
-- Dependencies: 1810 1810
-- Name: sampling_features_pk; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sampling_features
    ADD CONSTRAINT sampling_features_pk PRIMARY KEY (id);


--
-- TOC entry 2205 (class 2606 OID 73935)
-- Dependencies: 1811 1811
-- Name: sampling_points_pk; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sampling_points
    ADD CONSTRAINT sampling_points_pk PRIMARY KEY (id);


--
-- TOC entry 2207 (class 2606 OID 73937)
-- Dependencies: 1812 1812 1812
-- Name: simple_data_record_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY simple_data_records
    ADD CONSTRAINT simple_data_record_pkey PRIMARY KEY (id_datablock, id_datarecord);


--
-- TOC entry 2209 (class 2606 OID 73939)
-- Dependencies: 1813 1813
-- Name: text_block_encoding_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY text_block_encodings
    ADD CONSTRAINT text_block_encoding_pkey PRIMARY KEY (id_encoding);


--
-- TOC entry 2211 (class 2606 OID 73941)
-- Dependencies: 1814 1814
-- Name: unit_of_measures_pkey; Type: CONSTRAINT; Schema: observation; Owner: -; Tablespace: 
--

ALTER TABLE ONLY unit_of_measures
    ADD CONSTRAINT unit_of_measures_pkey PRIMARY KEY (id);

SET search_path = sos, pg_catalog;

--
-- TOC entry 2171 (class 2606 OID 74038)
-- Dependencies: 1793 1793
-- Name: envelopes_pkey; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY envelopes
    ADD CONSTRAINT envelopes_pkey PRIMARY KEY (id);


--
-- TOC entry 2181 (class 2606 OID 74040)
-- Dependencies: 1799 1799
-- Name: geographic_pk; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY geographic_localisations
    ADD CONSTRAINT geographic_pk PRIMARY KEY (id);


--
-- TOC entry 2169 (class 2606 OID 74044)
-- Dependencies: 1792 1792
-- Name: observation_offerings_pkey; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY observation_offerings
    ADD CONSTRAINT observation_offerings_pkey PRIMARY KEY (id);


--
-- TOC entry 2173 (class 2606 OID 74046)
-- Dependencies: 1795 1795 1795
-- Name: offering_procedures_pkey; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offering_procedures
    ADD CONSTRAINT offering_procedures_pkey PRIMARY KEY (id_offering, "procedure");


ALTER TABLE offering_phenomenons
  ADD CONSTRAINT offering_phenomenons_pk PRIMARY KEY(id_offering, phenomenon, composite_phenomenon);

--
-- TOC entry 2175 (class 2606 OID 74048)
-- Dependencies: 1796 1796 1796
-- Name: offering_response_modes_pkey; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offering_response_modes
    ADD CONSTRAINT offering_response_modes_pkey PRIMARY KEY (id_offering, "mode");


--
-- TOC entry 2177 (class 2606 OID 74694)
-- Dependencies: 1797 1797 1797
-- Name: offering_sampling_feature; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offering_sampling_features
    ADD CONSTRAINT offering_sampling_feature PRIMARY KEY (id_offering, sampling_feature);


--
-- TOC entry 2179 (class 2606 OID 74042)
-- Dependencies: 1798 1798
-- Name: projected_pk; Type: CONSTRAINT; Schema: sos; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projected_localisations
    ADD CONSTRAINT projected_pk PRIMARY KEY (id);


SET search_path = observation, pg_catalog;

--
-- TOC entry 2218 (class 1259 OID 82906)
-- Dependencies: 1818
-- Name: fki_data_array_encoding_fk; Type: INDEX; Schema: observation; Owner: -; Tablespace: 
--

CREATE INDEX fki_data_array_encoding_fk ON data_array_definition USING btree ("encoding");


--
-- TOC entry 2226 (class 2606 OID 73942)
-- Dependencies: 1801 1807 2196
-- Name: components_component_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY components
    ADD CONSTRAINT components_component_fkey FOREIGN KEY (component) REFERENCES phenomenons(id);


--
-- TOC entry 2227 (class 2606 OID 73947)
-- Dependencies: 1801 1802 2186
-- Name: components_composite_phenomenon_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY components
    ADD CONSTRAINT components_composite_phenomenon_fkey FOREIGN KEY (composite_phenomenon) REFERENCES composite_phenomenons(id);


--
-- TOC entry 2243 (class 2606 OID 82901)
-- Dependencies: 2208 1813 1818
-- Name: data_array_encoding_fk; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY data_array_definition
    ADD CONSTRAINT data_array_encoding_fk FOREIGN KEY ("encoding") REFERENCES text_block_encodings(id_encoding);


--
-- TOC entry 2228 (class 2606 OID 73952)
-- Dependencies: 1803 1813 2208
-- Name: data_block_definitions_encoding_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY data_block_definitions
    ADD CONSTRAINT data_block_definitions_encoding_fkey FOREIGN KEY ("encoding") REFERENCES text_block_encodings(id_encoding);


--
-- TOC entry 2225 (class 2606 OID 73957)
-- Dependencies: 1800 1800 1812 1812 2206
-- Name: data_record_fields_id_datablock_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY any_scalars
    ADD CONSTRAINT data_record_fields_id_datablock_fkey FOREIGN KEY (id_datablock, id_datarecord) REFERENCES simple_data_records(id_datablock, id_datarecord);


--
-- TOC entry 2229 (class 2606 OID 73962)
-- Dependencies: 1805 1810 2202
-- Name: measurements_feature_of_interest_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY measurements
    ADD CONSTRAINT measurements_feature_of_interest_fkey FOREIGN KEY (feature_of_interest) REFERENCES sampling_features(id);


--
-- TOC entry 2230 (class 2606 OID 73967)
-- Dependencies: 2196 1805 1807
-- Name: measurements_observed_property_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY measurements
    ADD CONSTRAINT measurements_observed_property_fkey FOREIGN KEY (observed_property) REFERENCES phenomenons(id);


--
-- TOC entry 2231 (class 2606 OID 73972)
-- Dependencies: 2198 1808 1805
-- Name: measurements_procedure_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY measurements
    ADD CONSTRAINT measurements_procedure_fkey FOREIGN KEY ("procedure") REFERENCES process(name);


--
-- TOC entry 2232 (class 2606 OID 73977)
-- Dependencies: 1805 2194 1806
-- Name: measurements_result_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY measurements
    ADD CONSTRAINT measurements_result_fkey FOREIGN KEY (result) REFERENCES measures(name);


--
-- TOC entry 2233 (class 2606 OID 73982)
-- Dependencies: 1814 2210 1806
-- Name: measures_uom_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY measures
    ADD CONSTRAINT measures_uom_fkey FOREIGN KEY (uom) REFERENCES unit_of_measures(id);


--
-- TOC entry 2234 (class 2606 OID 73987)
-- Dependencies: 1815 1804 2190
-- Name: observations_distribution_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_distribution_fkey FOREIGN KEY (distribution) REFERENCES "Distributions"(name);


--
-- TOC entry 2235 (class 2606 OID 73992)
-- Dependencies: 1815 1810 2202
-- Name: observations_feature_of_interest_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_feature_of_interest_fkey FOREIGN KEY (feature_of_interest) REFERENCES sampling_features(id);


--
-- TOC entry 2236 (class 2606 OID 73997)
-- Dependencies: 1815 1811 2204
-- Name: observations_feature_of_interest_point_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_feature_of_interest_point_fkey FOREIGN KEY (feature_of_interest_point) REFERENCES sampling_points(id);


--
-- TOC entry 2237 (class 2606 OID 74002)
-- Dependencies: 1815 1802 2186
-- Name: observations_observed_property_composite_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_observed_property_composite_fkey FOREIGN KEY (observed_property_composite) REFERENCES composite_phenomenons(id);


--
-- TOC entry 2238 (class 2606 OID 74007)
-- Dependencies: 1815 1807 2196
-- Name: observations_observed_property_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_observed_property_fkey FOREIGN KEY (observed_property) REFERENCES phenomenons(id);


--
-- TOC entry 2239 (class 2606 OID 74012)
-- Dependencies: 2198 1815 1808
-- Name: observations_procedure_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_procedure_fkey FOREIGN KEY ("procedure") REFERENCES process(name);


--
-- TOC entry 2240 (class 2606 OID 74017)
-- Dependencies: 1815 1803 2188
-- Name: observations_result_definition_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_result_definition_fkey FOREIGN KEY (result_definition) REFERENCES data_block_definitions(id);


--
-- TOC entry 2241 (class 2606 OID 74022)
-- Dependencies: 1815 1816 2214
-- Name: observations_result_fkey; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY observations
    ADD CONSTRAINT observations_result_fkey FOREIGN KEY (result) REFERENCES any_results(id_result);


--
-- TOC entry 2242 (class 2606 OID 74027)
-- Dependencies: 1816 1809 2200
-- Name: reference_pk; Type: FK CONSTRAINT; Schema: observation; Owner: -
--

ALTER TABLE ONLY any_results
    ADD CONSTRAINT reference_pk FOREIGN KEY (reference) REFERENCES "references"(id_reference);


SET search_path = sos, pg_catalog;

--
-- TOC entry 2219 (class 2606 OID 74049)
-- Dependencies: 1792 1793 2170
-- Name: observation_offerings_bounded_by_fkey; Type: FK CONSTRAINT; Schema: sos; Owner: -
--

ALTER TABLE ONLY observation_offerings
    ADD CONSTRAINT observation_offerings_bounded_by_fkey FOREIGN KEY (bounded_by) REFERENCES envelopes(id);


--
-- TOC entry 2220 (class 2606 OID 74054)
-- Dependencies: 2186 1794 1802
-- Name: offering_phenomenons_composite_phenomenon_fkey; Type: FK CONSTRAINT; Schema: sos; Owner: -
--

ALTER TABLE ONLY offering_phenomenons
    ADD CONSTRAINT offering_phenomenons_composite_phenomenon_fkey FOREIGN KEY (composite_phenomenon) REFERENCES observation.composite_phenomenons(id);


--
-- TOC entry 2221 (class 2606 OID 74059)
-- Dependencies: 1794 1792 2168
-- Name: offering_phenomenons_id_offering_fkey; Type: FK CONSTRAINT; Schema: sos; Owner: -
--

ALTER TABLE ONLY offering_phenomenons
    ADD CONSTRAINT offering_phenomenons_id_offering_fkey FOREIGN KEY (id_offering) REFERENCES observation_offerings(id);


--
-- TOC entry 2222 (class 2606 OID 74064)
-- Dependencies: 1807 2196 1794
-- Name: offering_phenomenons_phenomenon_fkey; Type: FK CONSTRAINT; Schema: sos; Owner: -
--

ALTER TABLE ONLY offering_phenomenons
    ADD CONSTRAINT offering_phenomenons_phenomenon_fkey FOREIGN KEY (phenomenon) REFERENCES observation.phenomenons(id);


--
-- TOC entry 2223 (class 2606 OID 74069)
-- Dependencies: 1795 2168 1792
-- Name: offering_procedures_id_offering_fkey; Type: FK CONSTRAINT; Schema: sos; Owner: -
--

ALTER TABLE ONLY offering_procedures
    ADD CONSTRAINT offering_procedures_id_offering_fkey FOREIGN KEY (id_offering) REFERENCES observation_offerings(id);


--
-- TOC entry 2224 (class 2606 OID 74079)
-- Dependencies: 2168 1797 1792
-- Name: offering_sampling_features_id_offering_fkey; Type: FK CONSTRAINT; Schema: sos; Owner: -
--

ALTER TABLE ONLY offering_sampling_features
    ADD CONSTRAINT offering_sampling_features_id_offering_fkey FOREIGN KEY (id_offering) REFERENCES observation_offerings(id);



-- we insert the null phenomenons
INSERT INTO "observation"."phenomenons" VALUES ('', '', 'phenomenon null');
INSERT INTO "observation"."composite_phenomenons" VALUES ('', '', 'composite phenomenon null', 0);


