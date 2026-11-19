# EP&M cheat sheet (Event Planning & Management)
### Candidate Numbers: 
- Candidate 1: 28
- Candidate 2: 12
- Candidate 3: 14

PG3402 exam project by Kevin Phan, Sadik Partalko and Rich Rase.
Users and vendors create and join events. Spring Boot services behind a gateway, React frontend, Kafka, Consul and Postgres, all started with Docker Compose.

---

## 1. Run it

You need Docker (Compose v2) and these free ports: 5173, 8100, 8500, 9092, 5432, 5433, 5434.

```
docker compose up --build
```

Wait about 1 minute. Then open **http://localhost:5173**.

Ready when http://localhost:8500 shows `gateway`, `user`, `event` and `vendor` in green.
(Before that the gateway answers 503. Just wait.)

## 2. Links

- App: http://localhost:5173
- **Swagger user:** http://localhost:8100/user/swagger-ui/index.html
- **Swagger event:** http://localhost:8100/event/swagger-ui/index.html
- **Swagger vendor:** http://localhost:8100/vendor/swagger-ui/index.html
- Consul (health of all services): http://localhost:8500
- Gateway health: http://localhost:8100/actuator/health

## 3. Log in

- User: `ola` / `password` (also `kari` / `password`)
- Vendor: `post@oslocatering.example` / `password123`
  (also `booking@festlyd.example`, `hei@blomsten.example`, `info@lysogtone.example`, `vendor@test.no`, same password)
- Or register a new user or vendor in the app.


## Database Connection
- To connect to databases. Every database use the same username and password
- Username: root
- Password: password
- For event:
    - Database event: eventPlanerManager
    - Port: 5432
- For user:
    - Database user: userPlanerManager
    - Port: 5433
- For vendor:
    - Database vendor: vendorPlanerManager
    - Port: 5434

GitHub and Google login for users: put `GITHUB_ID`, `GITHUB_SECRET`, `GOOGLE_ID` and `GOOGLE_SECRET` in a file `user/.env` before starting. Without it, use username and password.

## 4. Try it

1. Log in as `ola` at http://localhost:5173/login.
2. Go to http://localhost:5173/events and create an event ("Host a Community Event"). **(US1)**
3. In a second browser or private window, log in as `kari`. Press **Join Event** on ola's event. **(US2)**
4. Back as `ola`: the box *Organizer Notifications (Kafka Async)* shows "Kari Nordmann joined your event" after a few seconds.
5. As `ola`: press **Edit** on your event, change it, **Save Changes**. **(US3)** Then **Delete**.
6. Vendor: open http://localhost:5173/vendor, log in, press *Mine Eventer / Opprett Event* and create an event.

Without logging in you can still see all events at http://localhost:5173/events.

## 5. Check that it works

```
docker compose ps
curl http://localhost:8100/actuator/health
curl http://localhost:8100/api/event
docker compose logs -f gateway user event vendor
```

## 6. Scale up, load balancing

```
docker compose up -d --no-recreate --scale user=2 --scale event=2 --scale vendor=2
docker compose logs -f gateway
```

- Consul now shows 2 instances of `user`, `event` and `vendor`.
- Call `curl http://localhost:8100/api/user` a few times. The gateway log shows the address alternating between the instances.

## 7. Tests

```
cd user            (or event, vendor, gateway)
./mvnw verify      (or mvn verify) 
```

Docker must be running (Testcontainers). Report: `target/site/jacoco/index.html`.

## 8. Stop and reset

```
docker compose down        # stop, keep data
docker compose down -v     # stop and delete data (back to seed data)
```

Problems:
- Page says "Failed to load data" or 503: services still starting, wait a minute.
- `port is already allocated`: stop the other program on that port.
- A container keeps restarting: `docker compose logs <service>`.

# For the assessor

## 9. How it fits together

```
Browser --> Frontend :5173 --> Gateway :8100 --+--> User   :8081 --> db_user
                                  |            +--> Event  :8080 --> db_event
                              Consul :8500     +--> Vendor :8082 --> db_vendor
                        (discovery, health)

REST between services:   User <--> Event <--> Vendor
Kafka "event-joined":    Event --> User and Vendor (they store a notification)
```

- The gateway is the only entrance. The services have no host ports.
- The gateway checks the JWT, handles CORS and load balances over the instances in Consul (`lb://user`, `lb://event`, `lb://vendor`).
- Every service has its own Postgres database, with Flyway migrations and seed data.
- Services only share UUIDs (`externalUserId`, `externalEventId`, `externalVendorId`), never database ids.

## 10. Exam requirements: where to see them

1. Several services: user, event, vendor, gateway.
2. Sync: REST between user, event and vendor.
3. Async: Kafka `event-joined`.
4. Docker: one `Dockerfile` per service, `docker-compose.yaml` starts everything.
5. Gateway as access point and load balancing.
6. Health check: Consul UI on :8500, every instance checked on `/actuator/health`.
7. Several instances.
8. Central configuration: Consul is used for discovery and health only. Configuration is in the `application-*.yaml` files and overridden with environment variables in `docker-compose.yaml`.

## 11. Decisions and simplifications

- **Notifications** are saved in the user and vendor database (table `notifications`), so all instances show the same list and nothing is lost on restart. The event service sends the Kafka message after the join is saved. Every instance reads the topic with its own consumer group, and a unique key stops duplicates.
- **Capacity:** the event row is locked while someone joins, so two people cannot take the last place. Joining twice is rejected.
- **Login:** the user service issues a JWT, the gateway checks it. The secret has a default value in the code. Set `APP_JWT_SECRET` for real use.
- **Public routes:** to keep the frontend simple, all of `/api/event/**`, `/api/client/event/**`, `/api/vendors/**` and every `GET` on `/api/user/**` need no token. So the API lets anyone edit or delete events. The frontend only shows Edit and Delete to the organizer.
- **Service calls** use Docker names (`http://event:8080`), not Consul. Only the gateway load balances through Consul.
- **Profiles:** Docker uses `prod` (Postgres, Flyway). An IDE uses `dev` (H2, demo data).
- The Docker builds skip tests. Run `./mvnw verify` for the tests (60 user, 65 event, 70 vendor, 10 gateway).

## 12. Different from the arbeidskrav

- **User story 1 and User story 3 in the frontend** call the event service directly. The user service has `POST /api/user/{id}/events` (calls the event service), but the form does not use it. There is no edit endpoint in the user service, and the event service does not check who the organizer is.
- **No separate notification service.** User and vendor read the Kafka messages themselves.
- **Vendor booking, analytics, publish/cancel/reschedule events and notification channels** are not implemented. Vendors can register, log in and host their own events.
- **Vendor login with Google or GitHub** has buttons but is not set up in Docker.
