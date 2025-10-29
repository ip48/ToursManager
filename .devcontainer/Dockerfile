# Use official Microsoft Java devcontainer base with Java 21
FROM mcr.microsoft.com/devcontainers/java:21-bullseye

# Install Maven
ARG MAVEN_VERSION=3.9.5
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar xzf - -C /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
    && ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

# Install Node.js 20.x LTS (required for frontend)
# Use a retry loop and safer apt flags to survive transient network timeouts
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates gnupg curl; \
    # Try the nodesource setup + nodejs install up to 3 times on transient failures
    for i in 1 2 3; do \
        curl --retry 3 --retry-delay 5 -fsSL https://deb.nodesource.com/setup_20.x -o /tmp/nodesetup.sh && bash /tmp/nodesetup.sh \
            && apt-get update \
            && apt-get install -y --no-install-recommends nodejs \
            && npm install -g npm@latest \
            && break || (echo "Node install attempt $i failed, retrying..." && sleep 5); \
    done; \
    rm -f /tmp/nodesetup.sh

# Install PostgreSQL client and other development utilities
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
       postgresql-client \
       git \
       curl \
       wget \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# MongoDB tools can be installed separately if needed via:
# wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | apt-key add -
# echo "deb http://repo.mongodb.org/apt/debian bullseye/mongodb-org/6.0 main" | tee /etc/apt/sources.list.d/mongodb-org-6.0.list
# apt-get update && apt-get install -y mongodb-mongosh mongodb-database-tools

# Set working directory
WORKDIR /workspace

# Expose ports: backend (8080), frontend dev (3000), postgres (5432)
EXPOSE 8080 3000 5432
