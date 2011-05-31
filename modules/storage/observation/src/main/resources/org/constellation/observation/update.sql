
ALTER TABLE  "observation"."measurements" ADD COLUMN "feature_of_interest_curve" varchar(40) ;

ALTER TABLE  "observation"."observations" ADD COLUMN "feature_of_interest_curve" varchar(40) ;

CREATE TABLE "observation"."sampling_curves" (
    "id"                 character varying(40) NOT NULL,
    "description"        character varying(50),
    "name"               character varying(40),
    "boundedby"          character varying(40),
    "sampled_feature"    character varying(40),
    "length_uom"         character varying(40),
    "length_value"       double precision,
    "shape_id"           character varying(40),
    "shape_srsname"      character varying(40)
);

CREATE TABLE "observation"."linestring" (
    "id"                 character varying(40) NOT NULL,
    "x"                  double precision,
    "y"                  double precision,
    "z"                  double precision
);

ALTER TABLE "observation"."sampling_curves" ADD CONSTRAINT sampling_curves_pk PRIMARY KEY ("id");
