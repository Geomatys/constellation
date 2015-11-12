# Constellation-SDI
Constellation-SDI makes it possible to easily create a complete Spatial Data Infrastructure, from cataloging geographic
resources to operate a platform of sensors that feeds back information in real time.

[http://www.constellation-sdi.org/](http://www.constellation-sdi.org/)

#### Available [OGC web services](http://www.opengeospatial.org/standards)
* **WMS** : 1.1.1 and 1.3.0 (INSPIRE-compliant)
* **WMTS** : 1.0.0
* **CSW** : 2.0.0 and 2.0.2 (INSPIRE-compliant)
* **SOS** : 1.0.0 and 2.0.0 (need PostGIS database)
* **WFS** : 1.1.0 and  2.0.0 (INSPIRE-compliant)
* **WPS** : 1.0.0
* **WCS** : 1.0.0

#### Supported input data
* Vector :
  * Shapefiles
  * GeoJSON
  * KML
  * GPX
  * GML
  * CSV (with geometry in WKT)
  * MapInfo MIF/MID format
  * PostGIS database
* Raster :
  * Geotiff
  * NetCDF/NetCDF+NCML
  * Grib
  * Images with .tfw and .prj files for projection and transformation informations

## Get started

### Prerequires
To run Constellation-SDI, you'll need :
* **JDK7u45+** from Oracle. Can be downloaded [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) for your platform.
* **PostgreSQL 9.x** (found [here](http://www.postgresql.org/download/)) with a database named `constellation` owned by role:password `cstl:admin`
* **Apache Tomcat 7.0.27+** found [here](http://tomcat.apache.org/download-70.cgi)

For building :
* **Maven 3.3.x** found [here](https://maven.apache.org/download.cgi)

### Download latest version
You can download the latest version WAR [here](http://constellation-sdi.org/en/download.html).

### Build from sources
```sh
git clone https://github.com/Geomatys/constellation.git
mvn install -DskipTests
```
Note 1 : for smaller download without git history: `git clone --depth 1 https://github.com/Geomatys/constellation.git`

Note 2 : if you want to build with tests, you'll need a test database named `cstl-test` owned by role:password `cstl:admin`.

### Deploy on Tomcat
#### Tomcat configuration
Create a **setenv.sh** executable file in **bin/** folder of Tomcat with :

```
CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF8 -Xmx1024m -XX:MaxPermSize=128m -Dgeotk.image.cache.size=128m -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./constellation.hprof"
JAVA_HOME=<PATH_TO_JDK>
JRE_HOME=<PATH_TO_JDK>/jre
 ```
Tomcat startup :
 ```
<PATH_TO_TOMCAT>/bin/startup.sh
 ```
Tomcat shutdown :
 ```
<PATH_TO_TOMCAT>/bin/shutdown.sh
 ```

### Run with Jetty
Constellation-SDI can also be started with embedded jetty maven plug-in.
```
mvn jetty:run-war -DMAVEN_OPTS="-Xmx1G -XX:MaxPermSize=256m"
```

### Usage
Browse  [http://localhost:8080/constellation](http://localhost:8080/constellation) and authenticate with user `admin` and password `admin`.


### Configuration
Constellation retrieve his configuration through various inputs using following priority  :
1. System environment variables
2. Startup options (`-Dproperty=value`)
3. External configuration file (referenced with `-Dcstl.config=/path/to/config.properties` option)
4. Default embedded configuration

#### Available configuration properties
* **DATABASE_URL** : application database URL in Hiroku like format. Default value `postgres://cstl:admin@localhost:5432/constellation`
* **EPSG_DATABASE_URL** : EPSG database URL. Default value same as **DATABASE_URL**
* **TEST_DATABASE_URL** : testing database URL. Default value `postgres://test:test@localhost:5432/cstl-test`
* **cstl.config** : Path to application external configuration properties file. Optional, default null.
* **cstl.url** : Constellation application URL. Used by Constellation to generate resources URLs.
* **cstl.home** : Application home directory, used by Constellation to store logs, indexes, ... . Default set to `~/.constellation/`
* **cstl.data** : Application data directory, used by Constellation to store integrated data and some configurations ... . Default set to `~/.constellation/data`

SMTP server configuration (used to re-initialized user password) :
* **cstl.mail.smtp.from** : Default value `no-reply@localhost`
* **cstl.mail.smtp.host** : Default value `localhost`
* **cstl.mail.smtp.port** : Default value `25`
* **cstl.mail.smtp.username** : Default value `no-reply@localhost`
* **cstl.mail.smtp.password** : Default value `mypassword`
* **cstl.mail.smtp.ssl** : Default value `false`

## Contribute

### Activate Git hooks
Constellation use Git hooks to standardize commits message format.

```shell
rm .git/hooks/commit-msg
ln -s githook/* .git/hooks/
```
