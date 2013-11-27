CREATE SCHEMA "admin";


-- i18n

CREATE SEQUENCE "admin"."i18n_id" AS INTEGER START WITH 0;

CREATE TABLE "admin"."i18n"(
  "id"    INTEGER     NOT NULL,
  "lang"  VARCHAR(7)  NOT NULL,
  "value" CLOB        NOT NULL
);

ALTER TABLE "admin"."i18n" ADD CONSTRAINT i18n_pk PRIMARY KEY ("id","lang");


-- users

CREATE TABLE "admin"."user"(
  "login"    VARCHAR(32) NOT NULL,
  "password" VARCHAR(32) NOT NULL,
  "name"     VARCHAR(64) NOT NULL,
  "roles"    VARCHAR(200)
);

ALTER TABLE "admin"."user" ADD CONSTRAINT user_pk PRIMARY KEY ("login");


-- providers

CREATE TABLE "admin"."provider"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
  "identifier"  VARCHAR(64) NOT NULL UNIQUE,
  "type"        VARCHAR(8)  NOT NULL,
  "impl"        VARCHAR(32) NOT NULL,
  "config"      CLOB        NOT NULL,
  "owner"       VARCHAR(32),
  "metadata"    CLOB
);

ALTER TABLE "admin"."provider" ADD CONSTRAINT provider_pk       PRIMARY KEY ("id");
ALTER TABLE "admin"."provider" ADD CONSTRAINT provider_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");


-- provider items

CREATE TABLE "admin"."style"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
  "name"        VARCHAR(64) NOT NULL,
  "provider"    INTEGER     NOT NULL,
  "type"        VARCHAR(16) NOT NULL,
  "date"        BIGINT      NOT NULL,
  "title"       INTEGER     NOT NULL,
  "description" INTEGER     NOT NULL,
  "body"        CLOB        NOT NULL,
  "owner"       VARCHAR(32)
);

ALTER TABLE "admin"."style" ADD CONSTRAINT style_pk          PRIMARY KEY ("id");
ALTER TABLE "admin"."style" ADD CONSTRAINT style_owner_fk    FOREIGN KEY ("owner")    REFERENCES "admin"."user"("login");
ALTER TABLE "admin"."style" ADD CONSTRAINT style_provider_fk FOREIGN KEY ("provider") REFERENCES "admin"."provider"("id") ON DELETE CASCADE;

CREATE TABLE "admin"."data"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
  "name"        VARCHAR(64) NOT NULL,
  "namespace"   VARCHAR(256)NOT NULL,
  "provider"    INTEGER     NOT NULL,
  "type"        VARCHAR(16) NOT NULL,
  "date"        BIGINT      NOT NULL,
  "title"       INTEGER     NOT NULL,
  "description" INTEGER     NOT NULL,
  "owner"       VARCHAR(32)
);

ALTER TABLE "admin"."data" ADD CONSTRAINT data_pk          PRIMARY KEY ("id");
ALTER TABLE "admin"."data" ADD CONSTRAINT data_owner_fk    FOREIGN KEY ("owner")    REFERENCES "admin"."user"("login");
ALTER TABLE "admin"."data" ADD CONSTRAINT data_provider_fk FOREIGN KEY ("provider") REFERENCES "admin"."provider"("id") ON DELETE CASCADE;


CREATE TABLE "admin"."crs"(
  "dataid"  INTEGER NOT NULL,
  "crscode" VARCHAR(64)
);

ALTER TABLE "admin"."crs" ADD CONSTRAINT crs_data_fk FOREIGN KEY ("dataid") REFERENCES "admin"."data"("id") ON DELETE CASCADE;


CREATE TABLE "admin"."styled_data"(
  "style"       INTEGER     NOT NULL,
  "data"        INTEGER     NOT NULL
);

ALTER TABLE "admin"."styled_data" ADD CONSTRAINT style_data_pk PRIMARY KEY ("style","data");
ALTER TABLE "admin"."styled_data" ADD CONSTRAINT style_fk      FOREIGN KEY ("style") REFERENCES "admin"."style"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."styled_data" ADD CONSTRAINT data_fk       FOREIGN KEY ("data")  REFERENCES "admin"."data"("id")  ON DELETE CASCADE;


-- services

CREATE TABLE "admin"."service"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
  "identifier"  VARCHAR(32) NOT NULL,
  "type"        VARCHAR(8)  NOT NULL,
  "date"        BIGINT      NOT NULL,
  "title"       INTEGER     NOT NULL,
  "description" INTEGER     NOT NULL,
  "config"      CLOB,
  "owner"       VARCHAR(32)
);

ALTER TABLE "admin"."service" ADD CONSTRAINT service_pk       PRIMARY KEY ("id");
ALTER TABLE "admin"."service" ADD CONSTRAINT service_uq       UNIQUE ("identifier","type");
ALTER TABLE "admin"."service" ADD CONSTRAINT service_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");

CREATE TABLE "admin"."service_extra_config"(
  "id"          INTEGER     NOT NULL,
  "filename"    VARCHAR(32) NOT NULL,
  "content"     CLOB
);

ALTER TABLE "admin"."service_extra_config" ADD CONSTRAINT service_extra_config_pk  PRIMARY KEY ("id", "filename");
ALTER TABLE "admin"."service_extra_config" ADD CONSTRAINT service_extra_config_service_fk FOREIGN KEY ("id") REFERENCES "admin"."service"("id");

CREATE TABLE "admin"."service_metadata"(
  "id"          INTEGER     NOT NULL,
  "lang"        VARCHAR(3) NOT NULL,
  "content"     CLOB
);

ALTER TABLE "admin"."service_metadata" ADD CONSTRAINT service_metadata_pk  PRIMARY KEY ("id", "lang");
ALTER TABLE "admin"."service_metadata" ADD CONSTRAINT service_metadata_service_fk FOREIGN KEY ("id") REFERENCES "admin"."service"("id");


-- service items

CREATE TABLE "admin"."layer"(
  "id"           INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
  "name"         VARCHAR(64) NOT NULL,
  "namespace"    VARCHAR(256)NOT NULL,
  "alias"        VARCHAR(64),
  "service"      INTEGER     NOT NULL,
  "data"         INTEGER     NOT NULL,
  "date"         BIGINT      NOT NULL,
  "title"        INTEGER     NOT NULL,
  "description"  INTEGER     NOT NULL,
  "config"       CLOB,
  "owner"        VARCHAR(32)
);

ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_pk         PRIMARY KEY ("id");
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_uq         UNIQUE ("name","service");
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_service_fk FOREIGN KEY ("service") REFERENCES "admin"."service"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_data_fk    FOREIGN KEY ("data")  REFERENCES "admin"."data"("id")      ON DELETE CASCADE;
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_owner_fk   FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");


-- tasks

CREATE TABLE "admin"."task"(
  "identifier"  VARCHAR(64) NOT NULL,
  "state"       VARCHAR(32) NOT NULL,
  "type"        VARCHAR(32) NOT NULL,
  "title"       INTEGER     NOT NULL,
  "description" INTEGER     NOT NULL,
  "start"       BIGINT      NOT NULL,
  "end"         BIGINT,
  "owner"       VARCHAR(32)
);

ALTER TABLE "admin"."task" ADD CONSTRAINT task_pk       PRIMARY KEY ("identifier");
ALTER TABLE "admin"."task" ADD CONSTRAINT task_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");

CREATE TABLE "admin"."properties"(
  "key"    VARCHAR(32) NOT NULL,
  "value"  VARCHAR(64) NOT NULL
);
ALTER TABLE "admin"."properties" ADD CONSTRAINT properties_pk PRIMARY KEY ("key");