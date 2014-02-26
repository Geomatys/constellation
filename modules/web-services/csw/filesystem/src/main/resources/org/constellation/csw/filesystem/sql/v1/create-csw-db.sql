CREATE SCHEMA "csw";

CREATE TABLE "csw"."records"(
  "identifier"  VARCHAR(128) NOT NULL UNIQUE,
  "path"  VARCHAR(1024)      NOT NULL
);

ALTER TABLE "csw"."records" ADD CONSTRAINT records_pk PRIMARY KEY ("identifier","path");
