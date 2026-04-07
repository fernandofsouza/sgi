#!/bin/bash
# Heroku CI: .profile.d scripts are NOT executed, so JDBC_DATABASE_URL is never set
# by the Java buildpack. This script parses DATABASE_URL (always available via the
# heroku-postgresql add-on) and exports the JDBC_DATABASE_* vars before running Maven.
set -e

if [ -n "$DATABASE_URL" ] && [ -z "$JDBC_DATABASE_URL" ]; then
  # DATABASE_URL format: postgres://user:pass@host:port/dbname
  REST="${DATABASE_URL#postgres://}"
  USERPASS="${REST%@*}"
  HOSTDB="${REST#*@}"
  export JDBC_DATABASE_URL="jdbc:postgresql://$HOSTDB"
  export JDBC_DATABASE_USERNAME="${USERPASS%:*}"
  export JDBC_DATABASE_PASSWORD="${USERPASS#*:}"
fi

exec mvn verify -P integration-test
