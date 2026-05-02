.PHONY: build up down logs clean test

build:
	mvn -T 1C clean install -DskipTests

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f --tail=100

clean:
	mvn clean
	docker compose down -v

test:
	mvn test
