# Kore — Backend

### Piattaforma SaaS per il Wellness Integrato · API & Servizi

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-async-FF6600?logo=rabbitmq&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT_stateless-000000?logo=jsonwebtokens&logoColor=white)
![Tests](https://img.shields.io/badge/tests-JUnit_5_%2B_Mockito-25A162?logo=junit5&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

Backend RESTful di **Kore**, la piattaforma SaaS che riunisce in un unico abbonamento Personal
Trainer, Nutrizionisti e copertura assicurativa. È un **monolite modulare** su Java 21 e Spring
Boot 4, con un'architettura rigorosa a layer, sicurezza JWT stateless, chat real-time su WebSocket
e messaggistica asincrona su RabbitMQ.

> L'applicazione vive sotto `kore/`. Tutti i comandi Maven vanno eseguiti da quella directory.

---

## Indice

1. [Tech Stack](#tech-stack)
2. [Architettura](#architettura)
3. [Facade & Service](#facade--service)
4. [Pattern di Design](#pattern-di-design)
5. [Domain Model](#domain-model)
6. [API REST](#api-rest)
7. [Real-time & Messaggistica](#real-time--messaggistica)
8. [Scheduler](#scheduler)
9. [Gestione delle eccezioni](#gestione-delle-eccezioni)
10. [Quick Start](#quick-start)
11. [Credenziali di Test](#credenziali-di-test)
12. [Configurazione](#configurazione)
13. [Testing](#testing)
14. [Licenza](#licenza)

---

## Tech Stack

| Categoria | Tecnologia |
|---|---|
| Runtime | Java 21 (LTS) |
| Framework | Spring Boot 4.0.5 — Web, Data JPA, Security, WebSocket, Validation, AMQP, Mail, Actuator |
| Database | PostgreSQL 16 (anche per i test) |
| Sicurezza | Spring Security + JWT stateless (`jjwt` 0.11.5) |
| Real-time | STOMP su WebSocket |
| Messaggistica asincrona | RabbitMQ (con Dead Letter Queue) |
| Logging | Log4j2 (via SLF4J) — Console + RollingFile + JDBC async su DB dedicato |
| Coverage | JaCoCo 0.8.12 |
| Video consulti | Jitsi Meet (`JitsiVideoConferenceServiceImpl`) |
| Build | Maven Wrapper (`mvnw`) |
| Container | spring-boot-docker-compose (PostgreSQL + pgAdmin + RabbitMQ) |
| Testing | JUnit 5, Mockito, Spring Security Test |

Coordinate Maven: `com.project:kore:0.0.1-SNAPSHOT`. Package base: `com.project.kore`.

---

## Architettura

Ogni richiesta segue un percorso unidirezionale, senza salti di layer:

```
Controllers → Facades → Services → Builders → Repositories → PostgreSQL
                                 ↕ Mappers (entità ↔ DTO)
```

- **Controllers** (`controller/`) — espongono gli endpoint REST; delegano interamente a facade o service, senza business logic.
- **Facades** (`facade/` + `facade/impl/`) — punti d'ingresso coarse-grained che orchestrano più servizi per le operazioni complesse.
- **Services** (`service/` + `service/impl/`) — contengono la business logic; interfacce in `service/`, implementazioni in `service/impl/`.
- **Builders** (`builder/` + `builder/impl/`) — costruiscono le entità del dominio tramite Builder pattern; tutte le entità passano da un builder dedicato.
- **Mappers** (`mapper/`) — convertono entità JPA ↔ DTO, un mapper per entità.
- **Repositories** (`repository/`) — Spring Data JPA; nessun SQL custom oltre alle JPQL nelle `@Query`.

### Struttura dei package

```
com.project.kore/
├── controller/           # 16 REST controller
├── facade/               # 13 interfacce facade
│   └── impl/             # implementazioni facade
├── service/              # interfacce service
│   ├── impl/             # implementazioni service
│   └── strategy/         # Strategy pattern per le prenotazioni
├── builder/              # 9 interfacce builder
│   └── impl/             # implementazioni builder
├── mapper/               # 9 mapper entità ↔ DTO
├── dto/                  # request / response
├── model/                # 10 entità JPA
├── repository/           # Spring Data JPA
├── config/               # CORS, WebSocket, RabbitMQ, Async, Audit, init di dev/log
├── security/             # JWT filter, UserDetailsService, interceptor STOMP
├── scheduler/            # job pianificati
├── messaging/            # publisher / consumer RabbitMQ
├── exception/            # gerarchia per modulo + GlobalExceptionHandler
├── enums/                # Role, BookingStatus, ...
└── util/                 # BusinessConstants
```

---

## Facade & Service

### Facade (13)

| Facade | Responsabilità |
|---|---|
| `AuthFacade` | Registrazione, login, reset password |
| `UserFacade` | Profilo utente, dashboard cliente |
| `AdminFacade` | Gestione utenti, piani e statistiche admin |
| `ModeratorFacade` | Gestione anagrafiche moderabili, abbonamenti, contatti chat |
| `InsuranceFacade` | View assicuratore: clienti, abbonamenti, polizze |
| `ProfessionalFacade` | Lista professionisti, gestione slot e disponibilità |
| `BookingFacade` | Prenotazione e cancellazione slot |
| `SubscriptionFacade` | Attivazione abbonamenti e gestione crediti |
| `PlanFacade` | CRUD piani di abbonamento |
| `ChatFacade` | Conversazioni, messaggi, permessi, chiusura chat |
| `DocumentFacade` | Upload/download/eliminazione documenti per ruolo |
| `ReviewFacade` | Creazione e lettura recensioni |
| `ActivityFeedFacade` | Feed attività recenti (prenotazioni + documenti) |

Ogni interfaccia in `facade/` ha la sua implementazione `…Impl` in `facade/impl/`.

### Service principali

`SlotService`, `SubscriptionService`, `UserService`, `ChatService`, `ChatAsyncService`,
`MessageService`, `DocumentService`, `FileStorageService`, `ReviewService`, `PlanService`,
`WeeklyScheduleService`, `EmailService`, `VideoConferenceService` (impl. Jitsi).

---

## Pattern di Design

### GoF implementati

| Pattern | Dove | Descrizione |
|---|---|---|
| **Strategy** | `service/strategy/` | `BookingStrategy` con `PersonalTrainerBookingStrategy` e `NutritionistBookingStrategy`; `SlotServiceImpl` seleziona la strategia a runtime in base al ruolo del professionista (dispatch dinamico) |
| **Builder** | `builder/` + `builder/impl/` | Ogni entità è assemblata da un builder dedicato; `SlotBuilder` copre l'intero ciclo di vita dello slot, incluso `bookedAt` |
| **Facade** | `facade/` + `facade/impl/` | Punti d'ingresso che orchestrano più servizi; naming `<Name>Facade` |

### Builder (9)

`UserBuilder`, `SubscriptionBuilder`, `SlotBuilder`, `WeeklyScheduleBuilder`, `PlanBuilder`,
`ReviewBuilder`, `ChatBuilder`, `MessageBuilder`, `DocumentBuilder` — ciascuno con la propria
implementazione in `builder/impl/`.

### Mapper dedicati (9)

| Mapper | Converte |
|---|---|
| `UserMapper` | `User` ↔ `UserResponse` |
| `BookingMapper` | `Slot` (contesto prenotazione) ↔ `BookingResponse` |
| `SubscriptionMapper` | `Subscription` ↔ `SubscriptionResponse` |
| `SlotMapper` | `Slot` ↔ `SlotDTO` |
| `ReviewMapper` | `Review` ↔ `ReviewResponse` |
| `DocumentMapper` | `Document` ↔ `DocumentResponse` |
| `ChatMapper` | `Chat`/`Message` ↔ DTO chat |
| `PlanMapper` | `Plan` ↔ `PlanResponseDTO` |
| `ActivityFeedMapper` | `Slot`/`Document` → `ActivityFeedItemResponse` |

### Concorrenza

Per prevenire overbooking e aggiornamenti concorrenti sulle risorse "calde":

| Meccanismo | Dove | Scopo |
|---|---|---|
| **Optimistic locking** | `@Version` su `Slot`, `Subscription`, `User` | Gestione conflitti senza lock espliciti; `ObjectOptimisticLockingFailureException` → `ConcurrentUpdateException` |
| **Pessimistic locking** | `@Lock(PESSIMISTIC_WRITE)` su `SlotRepository.findByIdWithLock()` e `SubscriptionRepository.findByUserAndActiveTrueWithLock()` | Lock a DB sulle righe contese |
| **Lock in-process fine-grained** | `ConcurrentHashMap<Long, LockReference>` di `ReentrantLock` per-slot in `SlotServiceImpl`, con `synchronized` sull'accesso alla mappa | Serializzazione delle prenotazioni sullo stesso slot all'interno della JVM |

---

## Domain Model

### Ruoli

| Ruolo | Descrizione |
|---|---|
| `CLIENT` | Acquista piani, prenota slot, scarica documenti, lascia recensioni |
| `PERSONAL_TRAINER` | Definisce la disponibilità, gestisce i propri clienti, carica schede di allenamento |
| `NUTRITIONIST` | Definisce la disponibilità, gestisce i propri clienti, carica piani alimentari |
| `INSURANCE_MANAGER` | Gestisce le polizze infortuni; consulta clienti e abbonamenti |
| `MODERATOR` | Moderazione contenuti, supporto clienti, gestione anagrafiche CLIENT/PT/NUTRITIONIST |
| `ADMIN` | Supervisione globale, creazione piani, gestione di MODERATOR e INSURANCE_MANAGER |

### Entità (10)

Tutte in `model/`. Lo **stato di prenotazione vive interamente nell'entità `Slot`** — non esiste
un'entità `Booking` separata.

| Entità | Campi chiave | Note |
|---|---|---|
| `User` | email, password, firstName, lastName, role, assignedPtId, assignedNutritionistId, `@Version` | L'email funge da username |
| `Subscription` | planId, paymentFrequency, startDate, endDate, active, crediti PT/Nutri correnti, rate pagate/totali, nextPaymentDate, `@Version` | Semestrale/Annuale, unica soluzione o rate |
| `Slot` | professionalId, startTime, endTime, bookedById, status, meetingLink, bookedAt, reminderSent, `@Version` | Finestre da 30 minuti |
| `WeeklySchedule` | professionalId, dayOfWeek, startTime, endTime | Pattern di disponibilità del professionista |
| `Plan` | name, duration, crediti mensili PT/Nutri, fullPrice, monthlyInstallmentPrice | Basic/Premium × Semestrale/Annuale |
| `Review` | clientId, professionalId, rating, comment, createdAt | Una recensione per coppia cliente–professionista |
| `Chat` | participantA, participantB, status, createdAt | Conversazione a due |
| `Message` | chatId, senderId, content, status, createdAt | Real-time via WebSocket/RabbitMQ |
| `Document` | fileName, filePath, contentType, type, ownerId, uploadedById, notes | File su filesystem, metadati su DB |
| `AuditLog` | userId, action, entityType, entityId, timestamp | Tracciamento azioni utente |

### Enum chiave

| Enum | Valori |
|---|---|
| `Role` | `CLIENT`, `PERSONAL_TRAINER`, `NUTRITIONIST`, `MODERATOR`, `INSURANCE_MANAGER`, `ADMIN` |
| `BookingStatus` | `CONFIRMED`, `CANCELED`, `COMPLETED` |
| `ChatStatus` | `OPEN`, `CLOSED` |
| `MessageStatus` | `SENT`, `DELIVERED`, `READ` |
| `DocumentType` | `INSURANCE_POLICE`, `DIET_PLAN`, `WORKOUT_PLAN` |
| `PaymentFrequency` | `UNICA_SOLUZIONE`, `RATE_MENSILI` |
| `PlanDuration` | `SEMESTRALE` (6 mesi), `ANNUALE` (12 mesi) |

### Piani e Crediti

| Piano | Durata | Crediti PT/mese | Crediti Nutri/mese |
|---|---|---|---|
| Basic | Semestrale | 1 | 1 |
| Basic | Annuale | 1 | 1 |
| Premium | Semestrale | 2 | 2 |
| Premium | Annuale | 2 | 2 |

I crediti si azzerano mensilmente (non cumulabili). Lo `SubscriptionScheduler` gira ogni notte a
mezzanotte per rinnovare i crediti e gestire le rate.

### Regole di dominio

- **Prenotazioni** — slot da 30 minuti generati dai `WeeklySchedule` dei professionisti. Locking a doppio livello (`ReentrantLock` JVM + `PESSIMISTIC_WRITE` DB) contro l'overbooking. Cancellazione gratuita con credito rimborsato se richiesta con almeno 24 ore di anticipo. Alla prenotazione viene generato automaticamente un link **Jitsi** e inviata una email post-commit. Il `BookingReminderScheduler` invia i promemoria, impostando `reminderSent` per evitare duplicati.
- **Recensioni** — un cliente può recensire un professionista solo se esiste almeno una prenotazione confermata tra i due (o è attualmente assegnato) e non ha già recensito quella coppia (unicità garantita a DB).
- **Chat** — real-time via STOMP/WebSocket con autenticazione JWT sul frame CONNECT; fallback REST per lo storico. I permessi (`validateChatPermission()`) seguono l'ordine di guardie ADMIN → INSURANCE_MANAGER → MODERATOR → controllo assegnazione. Il pulsante "Contatta Amministrazione" apre una chat con un **moderatore** casuale; il moderatore può chiudere la conversazione.
- **Documenti** — storage su filesystem (`uploads/`) con metadati a DB: schede di allenamento (PT), piani alimentari (Nutrizionista), polizze (Insurance Manager).
- **Activity Feed** — `GET /api/activity` restituisce prenotazioni e documenti recenti, ordinati cronologicamente.
- **Candidature** — `POST /api/job-applications` riceve un CV in PDF e lo inoltra via email.
- **Statistiche dashboard** — KPI per ADMIN (utenti per ruolo, crescita mensile, popolarità piani, ricavi, prenotazioni, carico professionisti) e per PT/Nutrizionista (prenotazioni odierne, clienti da seguire, documenti caricati nella settimana).
- **Audit trail** — `AuditLog` + `AuditInterceptor` registrano tutte le azioni utente.

---

## API REST

I 16 controller espongono la superficie API sotto `/api`.

| Controller | Base path | Accesso |
|---|---|---|
| `AuthController` | `/api/auth` | Pubblico (login, register, reset password) |
| `BookingController` | `/api/bookings` | Autenticato (CLIENT) |
| `ProfessionalController` | `/api/professionals` | CLIENT (ricerca), PT/NUTRITIONIST (slot) |
| `ProfessionalStatsController` | `/api/professional` | PT, NUTRITIONIST |
| `ReviewController` | `/api/reviews` | Autenticato |
| `SubscriptionController` | `/api/subscriptions` | Autenticato |
| `UserController` | `/api/users` | Autenticato |
| `DocumentController` | `/api/documents` | CLIENT (lettura), PT/NUTRITIONIST/INSURANCE (upload) |
| `ChatController` | `/api/chat` + WebSocket `/ws` | Autenticato |
| `ActivityFeedController` | `/api/activity` | Autenticato |
| `PlanController` | `/api/plans` | Pubblico (lettura) |
| `AdminController` | `/api/admin` | ADMIN |
| `ModeratorController` | `/api/moderator` | MODERATOR |
| `InsuranceController` | `/api/insurance` | INSURANCE_MANAGER |
| `JobApplicationController` | `/api/job-applications` | Pubblico |
| `ChatWebSocketController` | STOMP `/app/chat.*` | Autenticato (JWT su CONNECT) |

---

## Real-time & Messaggistica

### WebSocket / STOMP

- Endpoint di connessione: **`/ws`**. Il `WebSocketChannelInterceptor` valida il token JWT sul frame STOMP **CONNECT** prima di permettere qualsiasi subscription.
- Canali principali:
  - `/topic/chat/{roomId}` — messaggi della stanza (broadcast)
  - `/user/queue/notifications` — notifiche private (nuovo messaggio, conteggio non letti, delivered/read)
  - `/app/chat.join`, `/app/chat.leave`, `/app/chat.send`, `/app/chat.typing`, `/app/chat.read` — comandi client → server

### RabbitMQ

La consegna asincrona dei messaggi di chat passa da `ChatMessagePublisher` →
`ChatMessageConsumer`. I messaggi non recuperabili vengono instradati alla **Dead Letter Queue**
`chat.messages.dlq` (`default-requeue-rejected: false`, `max-attempts: 3`,
`AmqpRejectAndDontRequeueException` per gli errori permanenti). I thread pool sono configurati in
`AsyncConfig`.

---

## Scheduler

| Scheduler | Cron | Scopo |
|---|---|---|
| `SubscriptionScheduler` | `0 0 0 * * ?` (ogni notte a mezzanotte) | Rinnovo crediti mensili e gestione delle rate |
| `BookingReminderScheduler` | `0 */5 * * * ?` (ogni 5 minuti) | Invio promemoria e set di `reminderSent` per evitare duplicati |
| `SlotGenerationScheduler` | all'avvio / pianificato | Generazione degli slot da 30 minuti dai `WeeklySchedule` |

Gli scheduler vengono disabilitati automaticamente durante i test.

---

## Gestione delle eccezioni

Le eccezioni **di dominio** estendono `BaseException` (che trasporta lo status HTTP) e sono
organizzate per modulo. `GlobalExceptionHandler` (`@RestControllerAdvice`) le mappa centralmente
sulle risposte HTTP. Le situazioni **già coperte da Spring** riusano le eccezioni standard del
framework invece di duplicarle in classi custom:

| Caso | Eccezione usata | Status |
|---|---|---|
| Accesso negato per ruolo | `org.springframework.security.access.AccessDeniedException` (Spring) | 403 |
| Conflitto optimistic locking | `org.springframework.orm.ObjectOptimisticLockingFailureException` (Spring) | 409 |
| Credenziali errate | `BadCredentialsException` (Spring) | 401 |
| Risorsa non trovata (entità) | `CustomResourceNotFoundException` | 404 |

Eccezioni di dominio per modulo:

| Modulo | Esempi |
|---|---|
| `exception/common/` | `BaseException`, `BusinessLogicException`, `CustomResourceNotFoundException`, `ResourceAlreadyExistsException` |
| `exception/booking/` | `SlotAlreadyBookedException`, `InsufficientCreditsException`, `BookingCancellationException`, `ProfessionalNotAssignedException`, `ProfessionalSoldOutException`, `SubscriptionExpiredException` |
| `exception/chat/` | `ChatNotAllowedException` |
| `exception/document/` | `DocumentStorageException`, `InvalidFileException` |
| `exception/email/` | `EmailDeliveryException` |
| `exception/review/` | `ReviewNotAllowedException` |

---

## Quick Start

### Prerequisiti

- **Java 21** — [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=21)
- **Docker Desktop** in esecuzione (richiesto dal profilo `dev`)

> Maven è incluso nel wrapper (`mvnw`), non serve installarlo.

### Avvio (profilo dev)

```powershell
cd kore

# Windows PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

All'avvio del profilo `dev`, Docker Compose lancia automaticamente:

- **PostgreSQL** su `localhost:5432`
- **pgAdmin** su `localhost:5050` (`a@a.a` / `root`)
- **RabbitMQ** su `localhost:5672`; management UI su `localhost:15672` (`guest` / `guest`)

Lo schema del database viene **ricreato** (`ddl-auto: create`) e ripopolato da `data.sql` ad ogni
avvio. Il backend risponde su `http://localhost:8080`.

### Comandi utili

```powershell
# Eseguire i test
.\mvnw.cmd test

# Singola classe di test
.\mvnw.cmd test "-Dtest=SlotServiceImplTest"

# Build JAR (senza test)
.\mvnw.cmd clean package "-DskipTests"

# Report coverage JaCoCo (in target/site/jacoco/)
.\mvnw.cmd verify
```

---

## Credenziali di Test

Disponibili con il profilo `dev` (seed da `data.sql`). **Password comune: `password`.**

| Email | Ruolo | Note |
|---|---|---|
| `admin@test.com` | Admin | Accesso completo |
| `moderator1@test.com` | Moderatore | (anche `moderator2`, `moderator3`) |
| `insurance@test.com` | Insurance Manager | Gestione polizze |
| `pt1@test.com` | Personal Trainer | Marco Rossi |
| `pt2@test.com` | Personal Trainer | Giulia Bianchi |
| `nutri1@test.com` | Nutrizionista | Laura Verdi |
| `nutri2@test.com` | Nutrizionista | Andrea Esposito |
| `luca@test.com` | Cliente | Assegnato a pt1 + nutri1 |
| `sofia@test.com` | Cliente | Assegnato a pt1 + nutri2 |
| `matteo@test.com` | Cliente | Assegnato a pt2 + nutri1 |
| `chiara@test.com` | Cliente | Assegnato a pt2 + nutri2 |
| `testreview@test.com` | Cliente | Dedicato al test delle recensioni |

> Il seed include una ventina di clienti, oltre 300 slot, prenotazioni passate/confermate,
> recensioni e documenti, così da popolare realisticamente le statistiche delle dashboard.

---

## Configurazione

### Variabili d'ambiente

| Variabile | Default dev | Descrizione |
|---|---|---|
| `MAIL_FROM` | `koreadministration@gmail.com` | Mittente delle email transazionali |
| `SMTP_HOST` | `smtp.gmail.com` | Host SMTP |
| `SMTP_PORT` | `587` | Porta SMTP |
| `SMTP_USERNAME` | `koreadministration@gmail.com` | Username SMTP |
| `SMTP_PASSWORD` | *(app password)* | Password / app password SMTP |
| `cors.allowed-origins` | `http://localhost:4200` | Origin consentita per CORS |

> **Chiave di firma JWT** — viene **generata casualmente all'avvio** (`RandomGenerationServiceImpl`,
> `jwt.length: 128`, algoritmo HS256): non richiede variabili d'ambiente. Conseguenza: i token
> emessi diventano invalidi a ogni riavvio dell'applicazione.

### Profili

| Profilo | Database | Docker Compose | DDL |
|---|---|---|---|
| `dev` (default) | PostgreSQL locale (`localhost:5432`) | Auto-avviato | `create` (schema ricreato ad ogni restart) |
| `test` | PostgreSQL locale (`localhost:5432`) | Disabilitato | `create` |

### Logging (Log4j2)

Configurazione in `src/main/resources/log4j2-spring.xml`, con tre appender:

| Appender | Destinazione | Note |
|---|---|---|
| `Console` | stdout | Tutti i layer applicativi |
| `File` (RollingFile) | `logs/app.log` | Rolling giornaliero, max 10 MB, 30 file |
| `AsyncLogDB` (JDBC async) | catalog PostgreSQL `kore_logs` (tabella `app_logs`) | Buffer di eventi; DB creato all'avvio da `LogsDatabaseInitializer` |

### Note non ovvie

- **Email come username** — `UserDetails.getUsername()` restituisce l'email; non esiste un campo username separato.
- **Doppia durata JWT** — token di autenticazione 24 h, token di reset password 30 min (entrambi in `JwtUtil`).
- **IPv4 per SMTP** — `KoreApplication` imposta `java.net.preferIPv4Stack=true` per evitare hang SMTP su IPv6.
- **WebSocket JWT** — il token è validato sul frame STOMP CONNECT prima di ogni subscription.
- **DDL dev** — `ddl-auto: create` ricrea lo schema ad ogni avvio; `data.sql` lo ripopola.
- **Jitsi** — il base URL delle stanze è configurabile (`https://meet.jit.si/Kore_Consulto_...` di default).
- **DB di log separato** — `kore_logs` è un catalog PostgreSQL distinto dal database principale.

---

## Testing

```powershell
# Suite completa (~64 classi di test)
.\mvnw.cmd test

# Singola classe
.\mvnw.cmd test "-Dtest=SlotServiceImplTest"

# Report coverage JaCoCo (in target/site/jacoco/)
.\mvnw.cmd verify
```

I test girano sul profilo `test` contro **PostgreSQL locale** (`localhost:5432`). Gli scheduler sono
disabilitati automaticamente.

### Pattern adottati

- `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks` per gli unit test puri
- `@WebMvcTest` + `MockMvc` per i controller
- `@DisplayName` su ogni metodo per output leggibile

### Copertura per layer

Controller, facade, service, strategy, mapper, builder, security (JWT, filter, interceptor STOMP),
scheduler, messaging (publisher/consumer) ed exception handling.

---

## Licenza

Distribuito con licenza **MIT**.
