--
-- PostgreSQL database dump
--

-- Dumped from database version 9.4.4
-- Dumped by pg_dump version 9.4.4
-- Started on 2015-08-26 16:02:15 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 7 (class 2615 OID 462067)
-- Name: admin; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA admin;


SET search_path = admin, pg_catalog;

SET default_with_oids = false;

--
-- TOC entry 210 (class 1259 OID 462579)
-- Name: chain_process; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE chain_process (
    id integer NOT NULL,
    auth character varying(512),
    code character varying(512),
    config text
);


--
-- TOC entry 209 (class 1259 OID 462577)
-- Name: chain_process_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE chain_process_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2406 (class 0 OID 0)
-- Dependencies: 209
-- Name: chain_process_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE chain_process_id_seq OWNED BY chain_process.id;


--
-- TOC entry 182 (class 1259 OID 462128)
-- Name: crs; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE crs (
    dataid integer NOT NULL,
    crscode character varying(64) NOT NULL
);


--
-- TOC entry 176 (class 1259 OID 462070)
-- Name: cstl_user; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE cstl_user (
    id integer NOT NULL,
    login character varying(32) NOT NULL,
    password character varying(32) NOT NULL,
    firstname character varying(64) NOT NULL,
    lastname character varying(64) NOT NULL,
    email character varying(64) NOT NULL,
    active boolean NOT NULL,
    avatar character varying(64),
    zip character varying(64),
    city character varying(64),
    country character varying(64),
    phone character varying(64),
    forgot_password_uuid character varying(64),
    address text,
    additional_address text,
    civility character varying(64),
    title text,
    locale text NOT NULL
);


--
-- TOC entry 175 (class 1259 OID 462068)
-- Name: cstl_user_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE cstl_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2407 (class 0 OID 0)
-- Dependencies: 175
-- Name: cstl_user_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE cstl_user_id_seq OWNED BY cstl_user.id;


--
-- TOC entry 180 (class 1259 OID 462093)
-- Name: data; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE data (
    id integer NOT NULL,
    name character varying(512) NOT NULL,
    namespace character varying(256) NOT NULL,
    provider integer NOT NULL,
    type character varying(32) NOT NULL,
    subtype character varying(32) DEFAULT ''::character varying NOT NULL,
    included boolean DEFAULT true NOT NULL,
    sensorable boolean DEFAULT false NOT NULL,
    date bigint NOT NULL,
    owner integer,
    metadata text,
    dataset_id integer,
    feature_catalog text,
    stats_result text,
    rendered boolean,
    stats_state text,
    hidden boolean DEFAULT false NOT NULL
);


--
-- TOC entry 181 (class 1259 OID 462117)
-- Name: data_i18n; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE data_i18n (
    data_id integer NOT NULL,
    lang character(2) NOT NULL,
    title integer NOT NULL,
    description integer NOT NULL
);


--
-- TOC entry 179 (class 1259 OID 462091)
-- Name: data_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2408 (class 0 OID 0)
-- Dependencies: 179
-- Name: data_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE data_id_seq OWNED BY data.id;


--
-- TOC entry 217 (class 1259 OID 462719)
-- Name: data_x_data; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE data_x_data (
    data_id integer NOT NULL,
    child_id integer NOT NULL
);


--
-- TOC entry 216 (class 1259 OID 462632)
-- Name: dataset; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE dataset (
    id integer NOT NULL,
    identifier character varying(100) NOT NULL,
    owner integer,
    date bigint,
    feature_catalog text
);


--
-- TOC entry 215 (class 1259 OID 462630)
-- Name: dataset_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE dataset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2409 (class 0 OID 0)
-- Dependencies: 215
-- Name: dataset_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE dataset_id_seq OWNED BY dataset.id;


--
-- TOC entry 188 (class 1259 OID 462231)
-- Name: layer; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE layer (
    id integer NOT NULL,
    name character varying(512) NOT NULL,
    namespace character varying(256),
    alias character varying(512),
    service integer NOT NULL,
    data integer NOT NULL,
    date bigint NOT NULL,
    config text,
    owner integer,
    title text
);


--
-- TOC entry 189 (class 1259 OID 462259)
-- Name: layer_i18n; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE layer_i18n (
    layer_id integer NOT NULL,
    lang character(2) NOT NULL,
    title integer NOT NULL,
    description integer NOT NULL
);


--
-- TOC entry 187 (class 1259 OID 462229)
-- Name: layer_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE layer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2410 (class 0 OID 0)
-- Dependencies: 187
-- Name: layer_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE layer_id_seq OWNED BY layer.id;


--
-- TOC entry 191 (class 1259 OID 462272)
-- Name: mapcontext; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE mapcontext (
    id integer NOT NULL,
    name character varying(512) NOT NULL,
    owner integer,
    description character varying(512),
    crs character varying(32),
    west double precision,
    north double precision,
    east double precision,
    south double precision,
    keywords character varying(256)
);


--
-- TOC entry 190 (class 1259 OID 462270)
-- Name: mapcontext_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE mapcontext_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2411 (class 0 OID 0)
-- Dependencies: 190
-- Name: mapcontext_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE mapcontext_id_seq OWNED BY mapcontext.id;


--
-- TOC entry 212 (class 1259 OID 462591)
-- Name: mapcontext_styled_layer; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE mapcontext_styled_layer (
    id integer NOT NULL,
    mapcontext_id integer NOT NULL,
    layer_id integer,
    style_id integer,
    layer_order integer DEFAULT 1 NOT NULL,
    layer_opacity integer DEFAULT 100 NOT NULL,
    layer_visible boolean DEFAULT true NOT NULL,
    external_layer character varying(512),
    external_layer_extent character varying(512),
    external_service_url character varying(512),
    external_service_version character varying(32),
    external_style character varying(128),
    iswms boolean DEFAULT true NOT NULL,
    data_id integer
);


--
-- TOC entry 211 (class 1259 OID 462589)
-- Name: mapcontext_styled_layer_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE mapcontext_styled_layer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2412 (class 0 OID 0)
-- Dependencies: 211
-- Name: mapcontext_styled_layer_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE mapcontext_styled_layer_id_seq OWNED BY mapcontext_styled_layer.id;


--
-- TOC entry 219 (class 1259 OID 462774)
-- Name: metadata; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE metadata (
    id integer NOT NULL,
    metadata_id character varying(100) NOT NULL,
    metadata_iso text NOT NULL,
    data_id integer,
    dataset_id integer,
    service_id integer,
    md_completion integer,
    owner integer,
    datestamp bigint,
    date_creation bigint,
    title character varying(500),
    profile character varying(255),
    parent_identifier integer,
    is_validated boolean DEFAULT false NOT NULL,
    is_published boolean DEFAULT false NOT NULL,
    level character varying(50) DEFAULT 'NONE'::character varying NOT NULL,
    resume character varying(5000),
    validation_required character varying(10) DEFAULT 'NONE'::character varying NOT NULL,
    validated_state text,
    comment text
);


--
-- TOC entry 221 (class 1259 OID 462850)
-- Name: metadata_bbox; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE metadata_bbox (
    metadata_id integer NOT NULL,
    east double precision NOT NULL,
    west double precision NOT NULL,
    north double precision NOT NULL,
    south double precision NOT NULL
);


--
-- TOC entry 218 (class 1259 OID 462772)
-- Name: metadata_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2413 (class 0 OID 0)
-- Dependencies: 218
-- Name: metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE metadata_id_seq OWNED BY metadata.id;


--
-- TOC entry 220 (class 1259 OID 462798)
-- Name: metadata_x_csw; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE metadata_x_csw (
    metadata_id integer NOT NULL,
    csw_id integer NOT NULL
);


--
-- TOC entry 184 (class 1259 OID 462185)
-- Name: permission; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE permission (
    id integer NOT NULL,
    name character varying(32) NOT NULL,
    description character varying(512) NOT NULL
);


--
-- TOC entry 183 (class 1259 OID 462183)
-- Name: permission_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2414 (class 0 OID 0)
-- Dependencies: 183
-- Name: permission_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE permission_id_seq OWNED BY permission.id;


--
-- TOC entry 194 (class 1259 OID 462336)
-- Name: property; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE property (
    name character varying(32) NOT NULL,
    value character varying(64) NOT NULL
);


--
-- TOC entry 178 (class 1259 OID 462080)
-- Name: provider; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE provider (
    id integer NOT NULL,
    identifier character varying(512) NOT NULL,
    parent character varying(512),
    type character varying(8) NOT NULL,
    impl character varying(32) NOT NULL,
    config text NOT NULL,
    owner integer
);


--
-- TOC entry 177 (class 1259 OID 462078)
-- Name: provider_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE provider_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2415 (class 0 OID 0)
-- Dependencies: 177
-- Name: provider_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE provider_id_seq OWNED BY provider.id;


--
-- TOC entry 205 (class 1259 OID 462520)
-- Name: role; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE role (
    name character varying(32) NOT NULL
);


--
-- TOC entry 196 (class 1259 OID 462348)
-- Name: sensor; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE sensor (
    id integer NOT NULL,
    identifier character varying(512) NOT NULL,
    type character varying(64) NOT NULL,
    parent character varying(512),
    owner integer,
    metadata text,
    date bigint
);


--
-- TOC entry 195 (class 1259 OID 462346)
-- Name: sensor_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE sensor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2416 (class 0 OID 0)
-- Dependencies: 195
-- Name: sensor_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE sensor_id_seq OWNED BY sensor.id;


--
-- TOC entry 197 (class 1259 OID 462364)
-- Name: sensored_data; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE sensored_data (
    sensor integer NOT NULL,
    data integer NOT NULL
);


--
-- TOC entry 186 (class 1259 OID 462213)
-- Name: service; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE service (
    id integer NOT NULL,
    identifier character varying(512) NOT NULL,
    type character varying(32) NOT NULL,
    date bigint NOT NULL,
    config text,
    owner integer,
    status character varying(32) NOT NULL,
    versions character varying(32) NOT NULL
);


--
-- TOC entry 198 (class 1259 OID 462381)
-- Name: service_details; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE service_details (
    id integer NOT NULL,
    lang character varying(3) NOT NULL,
    content text,
    default_lang boolean
);


--
-- TOC entry 199 (class 1259 OID 462395)
-- Name: service_extra_config; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE service_extra_config (
    id integer NOT NULL,
    filename character varying(32) NOT NULL,
    content text
);


--
-- TOC entry 185 (class 1259 OID 462211)
-- Name: service_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2417 (class 0 OID 0)
-- Dependencies: 185
-- Name: service_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE service_id_seq OWNED BY service.id;


--
-- TOC entry 193 (class 1259 OID 462289)
-- Name: style; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE style (
    id integer NOT NULL,
    name character varying(512) NOT NULL,
    provider integer NOT NULL,
    type character varying(32) NOT NULL,
    date bigint NOT NULL,
    body text NOT NULL,
    owner integer
);


--
-- TOC entry 200 (class 1259 OID 462424)
-- Name: style_i18n; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE style_i18n (
    style_id integer NOT NULL,
    lang character(2) NOT NULL,
    title integer NOT NULL,
    description integer NOT NULL
);


--
-- TOC entry 192 (class 1259 OID 462287)
-- Name: style_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE style_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2418 (class 0 OID 0)
-- Dependencies: 192
-- Name: style_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE style_id_seq OWNED BY style.id;


--
-- TOC entry 201 (class 1259 OID 462452)
-- Name: styled_data; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE styled_data (
    style integer NOT NULL,
    data integer NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 462469)
-- Name: styled_layer; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE styled_layer (
    style integer NOT NULL,
    layer integer NOT NULL,
    is_default boolean
);


--
-- TOC entry 203 (class 1259 OID 462486)
-- Name: task; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE task (
    identifier character varying(512) NOT NULL,
    state character varying(32) NOT NULL,
    type character varying(32) NOT NULL,
    date_start bigint NOT NULL,
    date_end bigint,
    owner integer,
    message text,
    task_parameter_id integer,
    progress double precision,
    task_output text
);


--
-- TOC entry 204 (class 1259 OID 462500)
-- Name: task_i18n; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE task_i18n (
    task_identifier character varying(512) NOT NULL,
    lang character(2) NOT NULL,
    title integer NOT NULL,
    description integer NOT NULL
);


--
-- TOC entry 214 (class 1259 OID 462620)
-- Name: task_parameter; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE task_parameter (
    id integer NOT NULL,
    owner integer NOT NULL,
    name character varying(255) NOT NULL,
    date bigint NOT NULL,
    process_authority character varying(100) NOT NULL,
    process_code character varying(100) NOT NULL,
    inputs text NOT NULL,
    trigger text,
    trigger_type character varying(30),
    type text
);


--
-- TOC entry 213 (class 1259 OID 462618)
-- Name: task_parameter_id_seq; Type: SEQUENCE; Schema: admin; Owner: -
--

CREATE SEQUENCE task_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2419 (class 0 OID 0)
-- Dependencies: 213
-- Name: task_parameter_id_seq; Type: SEQUENCE OWNED BY; Schema: admin; Owner: -
--

ALTER SEQUENCE task_parameter_id_seq OWNED BY task_parameter.id;


--
-- TOC entry 206 (class 1259 OID 462525)
-- Name: user_x_role; Type: TABLE; Schema: admin; Owner: -
--

CREATE TABLE user_x_role (
    user_id integer NOT NULL,
    role character varying(32) NOT NULL
);


--
-- TOC entry 2075 (class 2604 OID 462582)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY chain_process ALTER COLUMN id SET DEFAULT nextval('chain_process_id_seq'::regclass);


--
-- TOC entry 2062 (class 2604 OID 462073)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY cstl_user ALTER COLUMN id SET DEFAULT nextval('cstl_user_id_seq'::regclass);


--
-- TOC entry 2064 (class 2604 OID 462096)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data ALTER COLUMN id SET DEFAULT nextval('data_id_seq'::regclass);


--
-- TOC entry 2082 (class 2604 OID 462635)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY dataset ALTER COLUMN id SET DEFAULT nextval('dataset_id_seq'::regclass);


--
-- TOC entry 2071 (class 2604 OID 462234)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer ALTER COLUMN id SET DEFAULT nextval('layer_id_seq'::regclass);


--
-- TOC entry 2072 (class 2604 OID 462275)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext ALTER COLUMN id SET DEFAULT nextval('mapcontext_id_seq'::regclass);


--
-- TOC entry 2076 (class 2604 OID 462594)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext_styled_layer ALTER COLUMN id SET DEFAULT nextval('mapcontext_styled_layer_id_seq'::regclass);


--
-- TOC entry 2083 (class 2604 OID 462777)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata ALTER COLUMN id SET DEFAULT nextval('metadata_id_seq'::regclass);


--
-- TOC entry 2069 (class 2604 OID 462188)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY permission ALTER COLUMN id SET DEFAULT nextval('permission_id_seq'::regclass);


--
-- TOC entry 2063 (class 2604 OID 462083)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY provider ALTER COLUMN id SET DEFAULT nextval('provider_id_seq'::regclass);


--
-- TOC entry 2074 (class 2604 OID 462351)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensor ALTER COLUMN id SET DEFAULT nextval('sensor_id_seq'::regclass);


--
-- TOC entry 2070 (class 2604 OID 462216)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service ALTER COLUMN id SET DEFAULT nextval('service_id_seq'::regclass);


--
-- TOC entry 2073 (class 2604 OID 462292)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style ALTER COLUMN id SET DEFAULT nextval('style_id_seq'::regclass);


--
-- TOC entry 2081 (class 2604 OID 462623)
-- Name: id; Type: DEFAULT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task_parameter ALTER COLUMN id SET DEFAULT nextval('task_parameter_id_seq'::regclass);


--
-- TOC entry 2390 (class 0 OID 462579)
-- Dependencies: 210
-- Data for Name: chain_process; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2420 (class 0 OID 0)
-- Dependencies: 209
-- Name: chain_process_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('chain_process_id_seq', 1, false);


--
-- TOC entry 2364 (class 0 OID 462128)
-- Dependencies: 182
-- Data for Name: crs; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2358 (class 0 OID 462070)
-- Dependencies: 176
-- Data for Name: cstl_user; Type: TABLE DATA; Schema: admin; Owner: -
--

INSERT INTO cstl_user VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '', 'Administrator', 'contact@geomatys.com', true, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'en');


--
-- TOC entry 2421 (class 0 OID 0)
-- Dependencies: 175
-- Name: cstl_user_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('cstl_user_id_seq', 1, true);


--
-- TOC entry 2362 (class 0 OID 462093)
-- Dependencies: 180
-- Data for Name: data; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2363 (class 0 OID 462117)
-- Dependencies: 181
-- Data for Name: data_i18n; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2422 (class 0 OID 0)
-- Dependencies: 179
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('data_id_seq', 1, false);


--
-- TOC entry 2397 (class 0 OID 462719)
-- Dependencies: 217
-- Data for Name: data_x_data; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2396 (class 0 OID 462632)
-- Dependencies: 216
-- Data for Name: dataset; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2423 (class 0 OID 0)
-- Dependencies: 215
-- Name: dataset_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('dataset_id_seq', 1, false);


--
-- TOC entry 2370 (class 0 OID 462231)
-- Dependencies: 188
-- Data for Name: layer; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2371 (class 0 OID 462259)
-- Dependencies: 189
-- Data for Name: layer_i18n; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2424 (class 0 OID 0)
-- Dependencies: 187
-- Name: layer_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('layer_id_seq', 1, false);


--
-- TOC entry 2373 (class 0 OID 462272)
-- Dependencies: 191
-- Data for Name: mapcontext; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2425 (class 0 OID 0)
-- Dependencies: 190
-- Name: mapcontext_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('mapcontext_id_seq', 1, false);


--
-- TOC entry 2392 (class 0 OID 462591)
-- Dependencies: 212
-- Data for Name: mapcontext_styled_layer; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2426 (class 0 OID 0)
-- Dependencies: 211
-- Name: mapcontext_styled_layer_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('mapcontext_styled_layer_id_seq', 1, false);


--
-- TOC entry 2399 (class 0 OID 462774)
-- Dependencies: 219
-- Data for Name: metadata; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2401 (class 0 OID 462850)
-- Dependencies: 221
-- Data for Name: metadata_bbox; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2427 (class 0 OID 0)
-- Dependencies: 218
-- Name: metadata_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('metadata_id_seq', 1, false);


--
-- TOC entry 2400 (class 0 OID 462798)
-- Dependencies: 220
-- Data for Name: metadata_x_csw; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2366 (class 0 OID 462185)
-- Dependencies: 184
-- Data for Name: permission; Type: TABLE DATA; Schema: admin; Owner: -
--

INSERT INTO permission VALUES (1, 'SERVICE_READ_ACCESS', 'Accès en lecture des service');
INSERT INTO permission VALUES (2, 'SERVICE_WRITE_ACCESS', 'Accès en écriture des service');
INSERT INTO permission VALUES (3, 'SERVICE_CREATION', 'Création de service');
INSERT INTO permission VALUES (4, 'DATA_CREATION', 'Création de donnée');


--
-- TOC entry 2428 (class 0 OID 0)
-- Dependencies: 183
-- Name: permission_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('permission_id_seq', 1, false);


--
-- TOC entry 2376 (class 0 OID 462336)
-- Dependencies: 194
-- Data for Name: property; Type: TABLE DATA; Schema: admin; Owner: -
--

-- INSERT INTO property VALUES ('email.smtp.from', 'no-reply@localhost');
-- INSERT INTO property VALUES ('email.smtp.host', 'localhost');
-- INSERT INTO property VALUES ('email.smtp.port', '25');
-- INSERT INTO property VALUES ('email.smtp.username', 'no-reply@localhost');
-- INSERT INTO property VALUES ('email.smtp.password', 'mypassword');


--
-- TOC entry 2360 (class 0 OID 462080)
-- Dependencies: 178
-- Data for Name: provider; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2429 (class 0 OID 0)
-- Dependencies: 177
-- Name: provider_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('provider_id_seq', 1, false);


--
-- TOC entry 2387 (class 0 OID 462520)
-- Dependencies: 205
-- Data for Name: role; Type: TABLE DATA; Schema: admin; Owner: -
--

INSERT INTO role VALUES ('cstl-admin');
INSERT INTO role VALUES ('cstl-publish');
INSERT INTO role VALUES ('cstl-data');


--
-- TOC entry 2378 (class 0 OID 462348)
-- Dependencies: 196
-- Data for Name: sensor; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2430 (class 0 OID 0)
-- Dependencies: 195
-- Name: sensor_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('sensor_id_seq', 1, false);


--
-- TOC entry 2379 (class 0 OID 462364)
-- Dependencies: 197
-- Data for Name: sensored_data; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2368 (class 0 OID 462213)
-- Dependencies: 186
-- Data for Name: service; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2380 (class 0 OID 462381)
-- Dependencies: 198
-- Data for Name: service_details; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2381 (class 0 OID 462395)
-- Dependencies: 199
-- Data for Name: service_extra_config; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2431 (class 0 OID 0)
-- Dependencies: 185
-- Name: service_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('service_id_seq', 1, false);


--
-- TOC entry 2375 (class 0 OID 462289)
-- Dependencies: 193
-- Data for Name: style; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2382 (class 0 OID 462424)
-- Dependencies: 200
-- Data for Name: style_i18n; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2432 (class 0 OID 0)
-- Dependencies: 192
-- Name: style_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('style_id_seq', 1, false);


--
-- TOC entry 2383 (class 0 OID 462452)
-- Dependencies: 201
-- Data for Name: styled_data; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2384 (class 0 OID 462469)
-- Dependencies: 202
-- Data for Name: styled_layer; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2385 (class 0 OID 462486)
-- Dependencies: 203
-- Data for Name: task; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2386 (class 0 OID 462500)
-- Dependencies: 204
-- Data for Name: task_i18n; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2394 (class 0 OID 462620)
-- Dependencies: 214
-- Data for Name: task_parameter; Type: TABLE DATA; Schema: admin; Owner: -
--



--
-- TOC entry 2433 (class 0 OID 0)
-- Dependencies: 213
-- Name: task_parameter_id_seq; Type: SEQUENCE SET; Schema: admin; Owner: -
--

SELECT pg_catalog.setval('task_parameter_id_seq', 1, false);


--
-- TOC entry 2388 (class 0 OID 462525)
-- Dependencies: 206
-- Data for Name: user_x_role; Type: TABLE DATA; Schema: admin; Owner: -
--

INSERT INTO user_x_role VALUES (1, 'cstl-admin');


--
-- TOC entry 2185 (class 2606 OID 462587)
-- Name: chain_process_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY chain_process
    ADD CONSTRAINT chain_process_pk PRIMARY KEY (id);


--
-- TOC entry 2111 (class 2606 OID 462132)
-- Name: crs_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY crs
    ADD CONSTRAINT crs_pk PRIMARY KEY (dataid, crscode);


--
-- TOC entry 2089 (class 2606 OID 462871)
-- Name: cstl_user_email_key; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY cstl_user
    ADD CONSTRAINT cstl_user_email_key UNIQUE (email);


--
-- TOC entry 2091 (class 2606 OID 462876)
-- Name: cstl_user_forgot_password_uuid_key; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY cstl_user
    ADD CONSTRAINT cstl_user_forgot_password_uuid_key UNIQUE (forgot_password_uuid);


--
-- TOC entry 2093 (class 2606 OID 462077)
-- Name: cstl_user_login_key; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY cstl_user
    ADD CONSTRAINT cstl_user_login_key UNIQUE (login);


--
-- TOC entry 2108 (class 2606 OID 462121)
-- Name: data_i18n_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data_i18n
    ADD CONSTRAINT data_i18n_pk PRIMARY KEY (data_id, lang);


--
-- TOC entry 2104 (class 2606 OID 462104)
-- Name: data_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);


--
-- TOC entry 2195 (class 2606 OID 462733)
-- Name: data_x_data_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data_x_data
    ADD CONSTRAINT data_x_data_pk PRIMARY KEY (data_id, child_id);


--
-- TOC entry 2193 (class 2606 OID 462640)
-- Name: dataset_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pk PRIMARY KEY (id);


--
-- TOC entry 2130 (class 2606 OID 462263)
-- Name: layer_i18n_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer_i18n
    ADD CONSTRAINT layer_i18n_pk PRIMARY KEY (layer_id, lang);


--
-- TOC entry 2123 (class 2606 OID 462708)
-- Name: layer_name_uq; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer
    ADD CONSTRAINT layer_name_uq UNIQUE (name, namespace, service);


--
-- TOC entry 2126 (class 2606 OID 462239)
-- Name: layer_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer
    ADD CONSTRAINT layer_pk PRIMARY KEY (id);


--
-- TOC entry 2133 (class 2606 OID 462280)
-- Name: mapcontext_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext
    ADD CONSTRAINT mapcontext_pk PRIMARY KEY (id);


--
-- TOC entry 2187 (class 2606 OID 462602)
-- Name: mapcontext_styled_layer_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext_styled_layer
    ADD CONSTRAINT mapcontext_styled_layer_pk PRIMARY KEY (id);


--
-- TOC entry 2201 (class 2606 OID 462859)
-- Name: metadata_bbox_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata_bbox
    ADD CONSTRAINT metadata_bbox_pk PRIMARY KEY (metadata_id, east, west, north, south);


--
-- TOC entry 2197 (class 2606 OID 462782)
-- Name: metadata_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_pk PRIMARY KEY (id);


--
-- TOC entry 2199 (class 2606 OID 462802)
-- Name: metadata_x_csw_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata_x_csw
    ADD CONSTRAINT metadata_x_csw_pk PRIMARY KEY (metadata_id, csw_id);


--
-- TOC entry 2113 (class 2606 OID 462193)
-- Name: permission_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT permission_pk PRIMARY KEY (id);


--
-- TOC entry 2141 (class 2606 OID 462340)
-- Name: property_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY property
    ADD CONSTRAINT property_pk PRIMARY KEY (name);


--
-- TOC entry 2099 (class 2606 OID 462088)
-- Name: provider_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT provider_pk PRIMARY KEY (id);


--
-- TOC entry 2176 (class 2606 OID 462524)
-- Name: role_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pk PRIMARY KEY (name);


--
-- TOC entry 2150 (class 2606 OID 462368)
-- Name: sensor_data_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensored_data
    ADD CONSTRAINT sensor_data_pk PRIMARY KEY (sensor, data);


--
-- TOC entry 2143 (class 2606 OID 462660)
-- Name: sensor_id_uq; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensor
    ADD CONSTRAINT sensor_id_uq UNIQUE (identifier);


--
-- TOC entry 2147 (class 2606 OID 462356)
-- Name: sensor_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensor
    ADD CONSTRAINT sensor_pk PRIMARY KEY (id);


--
-- TOC entry 2154 (class 2606 OID 462388)
-- Name: service_details_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service_details
    ADD CONSTRAINT service_details_pk PRIMARY KEY (id, lang);


--
-- TOC entry 2157 (class 2606 OID 462402)
-- Name: service_extra_config_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service_extra_config
    ADD CONSTRAINT service_extra_config_pk PRIMARY KEY (id, filename);


--
-- TOC entry 2117 (class 2606 OID 462221)
-- Name: service_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pk PRIMARY KEY (id);


--
-- TOC entry 2119 (class 2606 OID 462562)
-- Name: service_uq; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_uq UNIQUE (identifier, type);


--
-- TOC entry 2101 (class 2606 OID 462566)
-- Name: sql140711122144190; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT sql140711122144190 UNIQUE (identifier);


--
-- TOC entry 2159 (class 2606 OID 462428)
-- Name: style_i18n_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style_i18n
    ADD CONSTRAINT style_i18n_pk PRIMARY KEY (style_id, lang);


--
-- TOC entry 2135 (class 2606 OID 462658)
-- Name: style_name_provider_uq; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style
    ADD CONSTRAINT style_name_provider_uq UNIQUE (name, provider);


--
-- TOC entry 2138 (class 2606 OID 462297)
-- Name: style_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style
    ADD CONSTRAINT style_pk PRIMARY KEY (id);


--
-- TOC entry 2163 (class 2606 OID 462456)
-- Name: styled_data_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY styled_data
    ADD CONSTRAINT styled_data_pk PRIMARY KEY (style, data);


--
-- TOC entry 2167 (class 2606 OID 462473)
-- Name: styled_layer_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY styled_layer
    ADD CONSTRAINT styled_layer_pk PRIMARY KEY (style, layer);


--
-- TOC entry 2174 (class 2606 OID 462560)
-- Name: task_i18n_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task_i18n
    ADD CONSTRAINT task_i18n_pk PRIMARY KEY (task_identifier, lang);


--
-- TOC entry 2190 (class 2606 OID 462628)
-- Name: task_parameter_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task_parameter
    ADD CONSTRAINT task_parameter_pk PRIMARY KEY (id);


--
-- TOC entry 2171 (class 2606 OID 462493)
-- Name: task_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task
    ADD CONSTRAINT task_pk PRIMARY KEY (identifier);


--
-- TOC entry 2095 (class 2606 OID 462075)
-- Name: user_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY cstl_user
    ADD CONSTRAINT user_pk PRIMARY KEY (id);


--
-- TOC entry 2178 (class 2606 OID 462529)
-- Name: user_x_role_pk; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY user_x_role
    ADD CONSTRAINT user_x_role_pk PRIMARY KEY (user_id, role);


--
-- TOC entry 2182 (class 2606 OID 462869)
-- Name: user_x_role_user_id_key; Type: CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY user_x_role
    ADD CONSTRAINT user_x_role_user_id_key UNIQUE (user_id);


--
-- TOC entry 2120 (class 1259 OID 462240)
-- Name: LAYER_NAME-SERVICE_IDX; Type: INDEX; Schema: admin; Owner: -
--

CREATE UNIQUE INDEX "LAYER_NAME-SERVICE_IDX" ON layer USING btree (name, service);


--
-- TOC entry 2114 (class 1259 OID 462222)
-- Name: SERVICE_IDENTIFIER-TYPE_IDX; Type: INDEX; Schema: admin; Owner: -
--

CREATE UNIQUE INDEX "SERVICE_IDENTIFIER-TYPE_IDX" ON service USING btree (identifier, type);


--
-- TOC entry 2183 (class 1259 OID 462588)
-- Name: chain_process_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE UNIQUE INDEX chain_process_idx ON chain_process USING btree (auth, code);


--
-- TOC entry 2109 (class 1259 OID 462133)
-- Name: crs_dataid_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX crs_dataid_idx ON crs USING btree (dataid);


--
-- TOC entry 2106 (class 1259 OID 462122)
-- Name: data_i18n_data_id_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX data_i18n_data_id_idx ON data_i18n USING btree (data_id);


--
-- TOC entry 2102 (class 1259 OID 462105)
-- Name: data_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX data_owner_idx ON data USING btree (owner);


--
-- TOC entry 2105 (class 1259 OID 462106)
-- Name: data_provider_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX data_provider_idx ON data USING btree (provider);


--
-- TOC entry 2191 (class 1259 OID 462651)
-- Name: dataset_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX dataset_owner_idx ON dataset USING btree (owner);


--
-- TOC entry 2121 (class 1259 OID 462242)
-- Name: layer_data_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX layer_data_idx ON layer USING btree (data);


--
-- TOC entry 2128 (class 1259 OID 462264)
-- Name: layer_i18n_layer_id_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX layer_i18n_layer_id_idx ON layer_i18n USING btree (layer_id);


--
-- TOC entry 2124 (class 1259 OID 462243)
-- Name: layer_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX layer_owner_idx ON layer USING btree (owner);


--
-- TOC entry 2127 (class 1259 OID 462241)
-- Name: layer_service_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX layer_service_idx ON layer USING btree (service);


--
-- TOC entry 2131 (class 1259 OID 462281)
-- Name: mapcontext_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX mapcontext_owner_idx ON mapcontext USING btree (owner);


--
-- TOC entry 2096 (class 1259 OID 462089)
-- Name: provider_identifier_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE UNIQUE INDEX provider_identifier_idx ON provider USING btree (identifier);


--
-- TOC entry 2097 (class 1259 OID 462090)
-- Name: provider_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX provider_owner_idx ON provider USING btree (owner);


--
-- TOC entry 2148 (class 1259 OID 462370)
-- Name: sensor_data_data_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX sensor_data_data_idx ON sensored_data USING btree (data);


--
-- TOC entry 2151 (class 1259 OID 462369)
-- Name: sensor_data_sensor_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX sensor_data_sensor_idx ON sensored_data USING btree (sensor);


--
-- TOC entry 2144 (class 1259 OID 462357)
-- Name: sensor_identifier_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE UNIQUE INDEX sensor_identifier_idx ON sensor USING btree (identifier);


--
-- TOC entry 2145 (class 1259 OID 462358)
-- Name: sensor_identifier_owner; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX sensor_identifier_owner ON sensor USING btree (owner);


--
-- TOC entry 2152 (class 1259 OID 462389)
-- Name: service_details_id_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX service_details_id_idx ON service_details USING btree (id);


--
-- TOC entry 2155 (class 1259 OID 462403)
-- Name: service_extra_config_id_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX service_extra_config_id_idx ON service_extra_config USING btree (id);


--
-- TOC entry 2115 (class 1259 OID 462223)
-- Name: service_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX service_owner_idx ON service USING btree (owner);


--
-- TOC entry 2160 (class 1259 OID 462429)
-- Name: style_i18n_style_id_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX style_i18n_style_id_idx ON style_i18n USING btree (style_id);


--
-- TOC entry 2136 (class 1259 OID 462298)
-- Name: style_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX style_owner_idx ON style USING btree (owner);


--
-- TOC entry 2139 (class 1259 OID 462299)
-- Name: style_provider_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX style_provider_idx ON style USING btree (provider);


--
-- TOC entry 2161 (class 1259 OID 462458)
-- Name: styled_data_data_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX styled_data_data_idx ON styled_data USING btree (data);


--
-- TOC entry 2164 (class 1259 OID 462457)
-- Name: styled_data_style_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX styled_data_style_idx ON styled_data USING btree (style);


--
-- TOC entry 2165 (class 1259 OID 462475)
-- Name: styled_layer_layer_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX styled_layer_layer_idx ON styled_layer USING btree (layer);


--
-- TOC entry 2168 (class 1259 OID 462474)
-- Name: styled_layer_style_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX styled_layer_style_idx ON styled_layer USING btree (style);


--
-- TOC entry 2172 (class 1259 OID 462506)
-- Name: task_i18n_identifier_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX task_i18n_identifier_idx ON task_i18n USING btree (task_identifier);


--
-- TOC entry 2169 (class 1259 OID 462494)
-- Name: task_owner_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX task_owner_idx ON task USING btree (owner);


--
-- TOC entry 2188 (class 1259 OID 462629)
-- Name: task_parameter_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE UNIQUE INDEX task_parameter_idx ON task_parameter USING btree (name, process_authority, process_code);


--
-- TOC entry 2179 (class 1259 OID 462531)
-- Name: user_x_role_role_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX user_x_role_role_idx ON user_x_role USING btree (role);


--
-- TOC entry 2180 (class 1259 OID 462530)
-- Name: user_x_role_user_id_idx; Type: INDEX; Schema: admin; Owner: -
--

CREATE INDEX user_x_role_user_id_idx ON user_x_role USING btree (user_id);


--
-- TOC entry 2245 (class 2606 OID 462853)
-- Name: bbox_metadata_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata_bbox
    ADD CONSTRAINT bbox_metadata_fk FOREIGN KEY (metadata_id) REFERENCES metadata(id) ON DELETE CASCADE;


--
-- TOC entry 2207 (class 2606 OID 462134)
-- Name: crs_dataid_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY crs
    ADD CONSTRAINT crs_dataid_fk FOREIGN KEY (dataid) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2243 (class 2606 OID 462803)
-- Name: csw_metadata_cross_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata_x_csw
    ADD CONSTRAINT csw_metadata_cross_id_fk FOREIGN KEY (csw_id) REFERENCES service(id) ON DELETE CASCADE;


--
-- TOC entry 2205 (class 2606 OID 462646)
-- Name: data_dataset_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dataset_id_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id);


--
-- TOC entry 2206 (class 2606 OID 462123)
-- Name: data_i18n_data_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data_i18n
    ADD CONSTRAINT data_i18n_data_id_fk FOREIGN KEY (data_id) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2203 (class 2606 OID 462107)
-- Name: data_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2204 (class 2606 OID 462112)
-- Name: data_provider_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_provider_fk FOREIGN KEY (provider) REFERENCES provider(id) ON DELETE CASCADE;


--
-- TOC entry 2237 (class 2606 OID 462722)
-- Name: data_x_data_cross_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data_x_data
    ADD CONSTRAINT data_x_data_cross_id_fk FOREIGN KEY (data_id) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2238 (class 2606 OID 462727)
-- Name: data_x_data_cross_id_fk2; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY data_x_data
    ADD CONSTRAINT data_x_data_cross_id_fk2 FOREIGN KEY (child_id) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2236 (class 2606 OID 462652)
-- Name: dataset_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2211 (class 2606 OID 462254)
-- Name: layer_data_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer
    ADD CONSTRAINT layer_data_fk FOREIGN KEY (data) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2212 (class 2606 OID 462265)
-- Name: layer_i18n_layer_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer_i18n
    ADD CONSTRAINT layer_i18n_layer_id_fk FOREIGN KEY (layer_id) REFERENCES layer(id) ON DELETE CASCADE;


--
-- TOC entry 2209 (class 2606 OID 462244)
-- Name: layer_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer
    ADD CONSTRAINT layer_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2210 (class 2606 OID 462249)
-- Name: layer_service_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY layer
    ADD CONSTRAINT layer_service_fk FOREIGN KEY (service) REFERENCES service(id) ON DELETE CASCADE;


--
-- TOC entry 2213 (class 2606 OID 462282)
-- Name: mapcontext_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext
    ADD CONSTRAINT mapcontext_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2235 (class 2606 OID 462742)
-- Name: mapcontext_styled_layer_data_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext_styled_layer
    ADD CONSTRAINT mapcontext_styled_layer_data_id_fk FOREIGN KEY (data_id) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2233 (class 2606 OID 462608)
-- Name: mapcontext_styled_layer_layer_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext_styled_layer
    ADD CONSTRAINT mapcontext_styled_layer_layer_id_fk FOREIGN KEY (layer_id) REFERENCES layer(id) ON DELETE CASCADE;


--
-- TOC entry 2232 (class 2606 OID 462603)
-- Name: mapcontext_styled_layer_mapcontext_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext_styled_layer
    ADD CONSTRAINT mapcontext_styled_layer_mapcontext_id_fk FOREIGN KEY (mapcontext_id) REFERENCES mapcontext(id) ON DELETE CASCADE;


--
-- TOC entry 2234 (class 2606 OID 462613)
-- Name: mapcontext_styled_layer_style_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY mapcontext_styled_layer
    ADD CONSTRAINT mapcontext_styled_layer_style_id_fk FOREIGN KEY (style_id) REFERENCES style(id) ON DELETE CASCADE;


--
-- TOC entry 2244 (class 2606 OID 462808)
-- Name: metadata_csw_cross_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata_x_csw
    ADD CONSTRAINT metadata_csw_cross_id_fk FOREIGN KEY (metadata_id) REFERENCES metadata(id) ON DELETE CASCADE;


--
-- TOC entry 2239 (class 2606 OID 462783)
-- Name: metadata_data_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_data_fk FOREIGN KEY (data_id) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2240 (class 2606 OID 462788)
-- Name: metadata_dataset_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE CASCADE;


--
-- TOC entry 2242 (class 2606 OID 462837)
-- Name: metadata_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2241 (class 2606 OID 462793)
-- Name: metadata_service_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_service_fk FOREIGN KEY (service_id) REFERENCES service(id) ON DELETE CASCADE;


--
-- TOC entry 2202 (class 2606 OID 462341)
-- Name: provider_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT provider_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2216 (class 2606 OID 462359)
-- Name: sensor_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensor
    ADD CONSTRAINT sensor_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2217 (class 2606 OID 462661)
-- Name: sensor_parent_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensor
    ADD CONSTRAINT sensor_parent_fk FOREIGN KEY (parent) REFERENCES sensor(identifier) ON DELETE CASCADE;


--
-- TOC entry 2219 (class 2606 OID 462376)
-- Name: sensored_data_data_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensored_data
    ADD CONSTRAINT sensored_data_data_fk FOREIGN KEY (data) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2218 (class 2606 OID 462371)
-- Name: sensored_data_sensor_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY sensored_data
    ADD CONSTRAINT sensored_data_sensor_fk FOREIGN KEY (sensor) REFERENCES sensor(id) ON DELETE CASCADE;


--
-- TOC entry 2220 (class 2606 OID 462390)
-- Name: service_details_service_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service_details
    ADD CONSTRAINT service_details_service_id_fk FOREIGN KEY (id) REFERENCES service(id) ON DELETE CASCADE;


--
-- TOC entry 2221 (class 2606 OID 462404)
-- Name: service_extra_config_service_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service_extra_config
    ADD CONSTRAINT service_extra_config_service_id_fk FOREIGN KEY (id) REFERENCES service(id) ON DELETE CASCADE;


--
-- TOC entry 2208 (class 2606 OID 462224)
-- Name: service_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2222 (class 2606 OID 462430)
-- Name: style_i18n_style_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style_i18n
    ADD CONSTRAINT style_i18n_style_id_fk FOREIGN KEY (style_id) REFERENCES style(id) ON DELETE CASCADE;


--
-- TOC entry 2214 (class 2606 OID 462300)
-- Name: style_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style
    ADD CONSTRAINT style_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2215 (class 2606 OID 462305)
-- Name: style_provider_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY style
    ADD CONSTRAINT style_provider_fk FOREIGN KEY (provider) REFERENCES provider(id) ON DELETE CASCADE;


--
-- TOC entry 2224 (class 2606 OID 462464)
-- Name: styled_data_data_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY styled_data
    ADD CONSTRAINT styled_data_data_fk FOREIGN KEY (data) REFERENCES data(id) ON DELETE CASCADE;


--
-- TOC entry 2223 (class 2606 OID 462459)
-- Name: styled_data_style_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY styled_data
    ADD CONSTRAINT styled_data_style_fk FOREIGN KEY (style) REFERENCES style(id) ON DELETE CASCADE;


--
-- TOC entry 2226 (class 2606 OID 462481)
-- Name: styled_layer_layer_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY styled_layer
    ADD CONSTRAINT styled_layer_layer_fk FOREIGN KEY (layer) REFERENCES layer(id) ON DELETE CASCADE;


--
-- TOC entry 2225 (class 2606 OID 462476)
-- Name: styled_layer_style_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY styled_layer
    ADD CONSTRAINT styled_layer_style_fk FOREIGN KEY (style) REFERENCES style(id) ON DELETE CASCADE;


--
-- TOC entry 2229 (class 2606 OID 462507)
-- Name: task_i18n_task_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task_i18n
    ADD CONSTRAINT task_i18n_task_id_fk FOREIGN KEY (task_identifier) REFERENCES task(identifier) ON DELETE CASCADE;


--
-- TOC entry 2227 (class 2606 OID 462495)
-- Name: task_owner_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task
    ADD CONSTRAINT task_owner_fk FOREIGN KEY (owner) REFERENCES cstl_user(id);


--
-- TOC entry 2228 (class 2606 OID 462767)
-- Name: task_task_parameter_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY task
    ADD CONSTRAINT task_task_parameter_id_fk FOREIGN KEY (task_parameter_id) REFERENCES task_parameter(id) ON DELETE CASCADE;


--
-- TOC entry 2231 (class 2606 OID 462552)
-- Name: user_x_role_role_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY user_x_role
    ADD CONSTRAINT user_x_role_role_fk FOREIGN KEY (role) REFERENCES role(name) ON DELETE CASCADE;


--
-- TOC entry 2230 (class 2606 OID 462547)
-- Name: user_x_role_user_id_fk; Type: FK CONSTRAINT; Schema: admin; Owner: -
--

ALTER TABLE ONLY user_x_role
    ADD CONSTRAINT user_x_role_user_id_fk FOREIGN KEY (user_id) REFERENCES cstl_user(id) ON DELETE CASCADE;


-- Completed on 2015-08-26 16:02:15 CEST

--
-- PostgreSQL database dump complete
--

