-- Ecocast - NASA Ames Research Center
-- (C) 2008, Ecocast

-- Adds the metadata tables to the postgrid db
-- Schema implements FGDC metadata standards

--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';

--
-- Name: topsweb; Type: SCHEMA; Schema: -; Owner: geoadmin
--

CREATE SCHEMA topsweb;


ALTER SCHEMA topsweb OWNER TO geoadmin;
GRANT USAGE ON SCHEMA topsweb TO public;

--
-- Name: SCHEMA topsweb; Type: COMMENT; Schema: -; Owner: geoadmin
--

COMMENT ON SCHEMA topsweb IS 'Ancillary TOPS data and metadata for web applications.';


SET search_path = postgrid, topsweb, postgis, pg_catalog;
ALTER USER geoadmin SET search_path TO postgrid, topsweb, postgis, pg_catalog;
ALTER USER geouser SET search_path TO postgrid, topsweb, postgis, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: CoverageMetadata; Type: TABLE; Schema: topsweb; Owner: geoadmin; Tablespace:
--

CREATE TABLE "CoverageMetadata" (
    coveragemetaid character(200) NOT NULL,
    coverageid character(200) DEFAULT ''::bpchar NOT NULL,
    uri character varying,
    creationdate character(100),
    seriesname character(50) DEFAULT ''::bpchar NOT NULL
);


ALTER TABLE topsweb."CoverageMetadata" OWNER TO geoadmin;
GRANT SELECT ON TABLE topsweb."CoverageMetadata" TO public;

--
-- Name: layermetadata; Type: TABLE; Schema: topsweb; Owner: geoadmin; Tablespace:
--

CREATE TABLE layermetadata (
    layer_meta_name character(50) NOT NULL,
    layer_name character(50) NOT NULL,
    abbr_title character(100),
    short_title character(150),
    long_title character(250),
    parameter_name character(150),
    parameter_type character(100),
    description character varying,
    long_description character varying,
    data_source character(150),
    purpose character varying,
    supplemental_info character varying,
    update_frequency character(75),
    use_constraint character varying
);


ALTER TABLE topsweb.layermetadata OWNER TO geoadmin;
GRANT SELECT ON TABLE layermetadata TO public;

--
-- Name: poc; Type: TABLE; Schema: topsweb; Owner: geoadmin; Tablespace:
--

CREATE TABLE poc (
    poc_id serial NOT NULL,
    last_name character(30),
    first_name character(20),
    address1 character(100),
    address2 character(100),
    city character(100),
    state character(50),
    country character(50),
    zip integer,
    phone character(20),
    email character(150),
    org character(150),
    org_address1 character(100),
    org_address2 character(100),
    org_city character(100),
    org_state character(50),
    org_zip integer,
    org_country character(50),
    org_contact character(150)
);


ALTER TABLE topsweb.poc OWNER TO geoadmin;
GRANT SELECT ON TABLE topsweb.poc TO public;

--
-- Name: seriesmetadata; Type: TABLE; Schema: topsweb; Owner: geoadmin; Tablespace:
--

CREATE TABLE seriesmetadata (
    series_meta_name character(50) DEFAULT ''::bpchar NOT NULL,
    series_name character(50) DEFAULT ''::bpchar NOT NULL,
    legend_uri character varying,
    pub_date character(100),
    poc_id integer,
    version character varying(150),
    forecast character(2),
    themekey1 character(100),
    themekey2 character(100),
    themekey3 character(100),
    themekey4 character(100),
    themekey5 character(100),
    themekey6 character(100),
    themekey7 character(100),
    themekey8 character(100)
);


ALTER TABLE topsweb.seriesmetadata OWNER TO geoadmin;
GRANT SELECT ON TABLE topsweb.seriesmetadata TO public;

--
-- Name: CoverageMetadata_pkey; Type: CONSTRAINT; Schema: topsweb; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "CoverageMetadata"
    ADD CONSTRAINT "CoverageMetadata_pkey" PRIMARY KEY (coverageid);


--
-- Name: layermetadata_layer_meta_name_key; Type: CONSTRAINT; Schema: topsweb; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY layermetadata
    ADD CONSTRAINT layermetadata_layer_meta_name_key UNIQUE (layer_meta_name);


--
-- Name: layermetadata_pkey; Type: CONSTRAINT; Schema: topsweb; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY layermetadata
    ADD CONSTRAINT layermetadata_pkey PRIMARY KEY (layer_name);


--
-- Name: poc_pkey; Type: CONSTRAINT; Schema: topsweb; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY poc
    ADD CONSTRAINT poc_pkey PRIMARY KEY (poc_id);


--
-- Name: seriesmetadata_pkey; Type: CONSTRAINT; Schema: topsweb; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY seriesmetadata
    ADD CONSTRAINT seriesmetadata_pkey PRIMARY KEY (series_name);


--
-- Name: seriesmetadata_series_meta_name_key; Type: CONSTRAINT; Schema: topsweb; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY seriesmetadata
    ADD CONSTRAINT seriesmetadata_series_meta_name_key UNIQUE (series_meta_name);


--
-- Name: poc_index; Type: INDEX; Schema: topsweb; Owner: geoadmin; Tablespace:
--

CREATE INDEX poc_index ON poc USING btree (last_name, first_name);


--
-- Name: topsweb; Type: ACL; Schema: -; Owner: geoadmin
--

REVOKE ALL ON SCHEMA topsweb FROM PUBLIC;
REVOKE ALL ON SCHEMA topsweb FROM geoadmin;
GRANT ALL ON SCHEMA topsweb TO geoadmin;
GRANT USAGE ON SCHEMA topsweb TO PUBLIC;


--
-- Name: layermetadata; Type: ACL; Schema: topsweb; Owner: geoadmin
--

REVOKE ALL ON TABLE layermetadata FROM PUBLIC;
REVOKE ALL ON TABLE layermetadata FROM geoadmin;
GRANT ALL ON TABLE layermetadata TO geoadmin;


--
-- PostgreSQL database dump complete
--
