CREATE SCHEMA "CstlAdmin";

CREATE TABLE "CstlAdmin"."User"(
  "login"       VARCHAR(32) NOT NULL,
  "password"    VARCHAR(32) NOT NULL,
  "name"        VARCHAR(64) NOT NULL,
  "roles"       VARCHAR(200)
);
CREATE TABLE "CstlAdmin"."Provider"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "identifier"  VARCHAR(64) NOT NULL,
  "type"        VARCHAR(32) NOT NULL,
  "impl"        VARCHAR(32) NOT NULL,
  "owner"       VARCHAR(32)
);
CREATE TABLE "CstlAdmin"."Style"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "name"        VARCHAR(64) NOT NULL,
  "provider"    INTEGER     NOT NULL,
  "owner"       VARCHAR(32)
);
CREATE TABLE "CstlAdmin"."Data"(
  "id"          INTEGER     NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "name"        VARCHAR(64) NOT NULL,
  "provider"    INTEGER     NOT NULL,
  "type"        VARCHAR(32) NOT NULL,
  "owner"       VARCHAR(32)
);
CREATE TABLE "CstlAdmin"."StyledData"(
  "style"       INTEGER     NOT NULL,
  "data"        INTEGER     NOT NULL
);
CREATE TABLE "CstlAdmin"."Task"(
  "identifier"  VARCHAR(64) NOT NULL,
  "state"       VARCHAR(32) NOT NULL,
  "type"        VARCHAR(32) NOT NULL,
  "description" VARCHAR(200),
  "start"       BIGINT      NOT NULL,
  "end"         BIGINT,
  "owner"       VARCHAR(32)
);

ALTER TABLE "CstlAdmin"."User"        ADD CONSTRAINT user_pk            PRIMARY KEY ("login");
ALTER TABLE "CstlAdmin"."Provider"    ADD CONSTRAINT provider_pk        PRIMARY KEY ("id");
ALTER TABLE "CstlAdmin"."Style"       ADD CONSTRAINT style_pk           PRIMARY KEY ("id");
ALTER TABLE "CstlAdmin"."Data"        ADD CONSTRAINT data_pk            PRIMARY KEY ("id");
ALTER TABLE "CstlAdmin"."Task"        ADD CONSTRAINT task_pk            PRIMARY KEY ("identifier");

ALTER TABLE "CstlAdmin"."Style"       ADD CONSTRAINT style_owner_fk     FOREIGN KEY ("owner")     REFERENCES "CstlAdmin"."User"     ("login");
ALTER TABLE "CstlAdmin"."Data"        ADD CONSTRAINT data_owner_fk      FOREIGN KEY ("owner")     REFERENCES "CstlAdmin"."User"     ("login");
ALTER TABLE "CstlAdmin"."Provider"    ADD CONSTRAINT provider_owner_fk  FOREIGN KEY ("owner")     REFERENCES "CstlAdmin"."User"     ("login");
ALTER TABLE "CstlAdmin"."Task"        ADD CONSTRAINT task_owner_fk      FOREIGN KEY ("owner")     REFERENCES "CstlAdmin"."User"     ("login");
ALTER TABLE "CstlAdmin"."Style"       ADD CONSTRAINT style_provider_fk  FOREIGN KEY ("provider")  REFERENCES "CstlAdmin"."Provider" ("id") ON DELETE CASCADE;
ALTER TABLE "CstlAdmin"."Data"        ADD CONSTRAINT data_provider_fk   FOREIGN KEY ("provider")  REFERENCES "CstlAdmin"."Provider" ("id") ON DELETE CASCADE;
ALTER TABLE "CstlAdmin"."StyledData"  ADD CONSTRAINT style_fk           FOREIGN KEY ("style")     REFERENCES "CstlAdmin"."Style"    ("id") ON DELETE CASCADE;
ALTER TABLE "CstlAdmin"."StyledData"  ADD CONSTRAINT data_fk            FOREIGN KEY ("data")      REFERENCES "CstlAdmin"."Data"     ("id") ON DELETE CASCADE;