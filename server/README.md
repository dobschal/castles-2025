# Castles Server

# Installation

You need Java 11 (e.g. via Homebrew) and docker installed.

# Get started

Start the database via Terminal:
```bash
docker network create castles # on first installation only
docker compose up database -d
```

Start the server via Terminal:
```bash
./gradlew quarkusDev
```
