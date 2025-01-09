DOCKER_BUILD_OPTS=

.PHONY: all

all: build-app

build-app: App.Dockerfile
	docker build --rm $(DOCKER_BUILD_OPTS) \
		-f App.Dockerfile \
		-t vks-service:latest \
		..
