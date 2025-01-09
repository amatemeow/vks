# VK Scanner Application

## About
This service is used to scan single VK wall for desired posts.

## Versions

### Full version
The full version can operate in three modes:
- New posts
- Simple oneline query search
- Advanced search with search list (e.g. ['criteria1','criteria2'], where we seek for ANY criteria was met)

The full version runs with java 23 and uses advantages of PostgreSQL and Docker.

### Lite version
The lite Linux-service version supports only Simple oneline query search mode. Runs with java 11 with no persistence.

## Docker
This app is intented to be built and hosted with Docker.  
Folder `/docker` contains `App.Dockerfile`, `docker-compose.yml`, `entrypoint.sh` and environment files. Also it contains `Makefile` in order to build easier.  
To build and host application:

```bash
> cd ./docker
> make all
> docker compose up -d
```

Beforehand you need to set up your own values in `app.env` and `db.env` files.
