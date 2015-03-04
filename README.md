To run:
----------
 * git clone <repository URL>
  * *for smaller download without git history*: git clone --depth 1 <repository URL>
 * mvn install
 * mvn jetty:run-war
export MAVEN_OPTS="-Xmx1G -XX:MaxPermSize=256m"
 * browse: http://localhost:8080
 * Authenticate: user 'admin', password 'admin'