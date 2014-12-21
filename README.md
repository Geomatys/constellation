Features
========

Inputs
------
 * GeoJSON
 * ESRI Shapefile
 * Database (PostgreSQL / PostGIS, H2, ...)
 * SensorML
 * WFS API

Outputs
-------
 * WFS
 * WPS 
 * ...


To run:
-------
 * git clone <repository URL>
  * *for smaller download without git history*: git clone --depth 1 <repository URL>
 * mvn install
 * mvn jetty:run-war
 * browse: http://localhost:8080
 * Authenticate: user 'admin', password 'admin'

