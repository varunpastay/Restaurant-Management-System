#!/bin/sh
# Some hosts (Render) assign the port to listen on via $PORT rather than
# always using 8080. Patch Tomcat's HTTP connector to match before starting,
# defaulting to 8080 (what Railway and a plain `docker run -p` expect).
set -e

PORT="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"${PORT}\"/" /usr/local/tomcat/conf/server.xml

exec /usr/local/tomcat/bin/catalina.sh run
