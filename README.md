# Aulab Chronicle

Portale di notizie e articoli sviluppato come progetto finale del corso Aulab Hackademy. L'applicazione permette la pubblicazione, revisione e gestione di articoli con un sistema di ruoli utente, upload immagini su cloud e notifiche via email.

---

## Stack tecnologico

- **Java 21**
- **Spring Boot 4.0.6**
- **Spring Security** — autenticazione e autorizzazione basata su ruoli
- **Spring Data JPA + Hibernate** — Object Reltion Mapping e accesso al database
- **Thymeleaf + Thymeleaf Security Extras** — template engine con supporto sec:authorize
- **MySQL** — database relazionale
- **Supabase** — storage cloud per le immagini degli articoli
- **Mailtrap** — SMTP (Simple Mail Transfer Protocol)sandbox per l'invio di email (notifiche e reset password)
- **ModelMapper 3.2.0** — mapping tra entity e Data Transfer Object
- **Lombok** — riduzione del boilerplate(codice ripetitivo) Java
- **Bootstrap 5.3.3** — framework CSS
- **Font Awesome 6.5.1** — icone

---

## Prerequisiti

- Java 21
- Maven
- MySQL
- Account Supabase (bucket configurato)
- Account Mailtrap (o altro SMTP)

---

## Configurazione

Crea il file `src/main/resources/application.properties` con i seguenti valori (non committare questo file, è escluso da git):

```properties
spring.application.name=progetto_finale_docente

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/progettoFinaleDocente
spring.datasource.username=root
spring.datasource.password=TUA_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.open-in-view=false

# Supabase
supabase.url=https://TUO_PROGETTO.supabase.co
supabase.key=TUA_CHIAVE
supabase.bucket=/storage/v1/object/immagini-articoli/
supabase.image=/storage/v1/object/public/immagini-articoli/

# Mailtrap
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=TUO_USERNAME
spring.mail.password=TUA_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Upload file
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

---

## Setup del database

Crea il database MySQL:

```sql
CREATE DATABASE progettoFinaleDocente;
```

Poi esegui le seguenti migration manualmente (in ordine):

```bash
# Contatore visualizzazioni articoli
mysql -u root -p progettoFinaleDocente -e "ALTER TABLE articles ADD COLUMN view_count INT DEFAULT 0 NOT NULL;"

# Flag immagine principale
mysql -u root -p progettoFinaleDocente -e "ALTER TABLE images ADD COLUMN is_primary TINYINT(1) NOT NULL DEFAULT 0;"

# Aumento limiti campi articolo
mysql -u root -p progettoFinaleDocente -e "ALTER TABLE articles MODIFY COLUMN title VARCHAR(255) NOT NULL, MODIFY COLUMN subtitle VARCHAR(255) NOT NULL, MODIFY COLUMN body TEXT NOT NULL;"

# Dati personali utente
mysql -u root -p progettoFinaleDocente -e "ALTER TABLE users ADD COLUMN gender VARCHAR(10) NULL, ADD COLUMN birth_date DATE NULL;"

# Token reset password
mysql -u root -p progettoFinaleDocente -e "ALTER TABLE users ADD COLUMN reset_token VARCHAR(255) NULL, ADD COLUMN reset_token_expiry DATE NULL;"
```

> La colonna `created_at` nella tabella `users` viene creata automaticamente da Hibernate tramite `@PrePersist`.

---

## Avvio

```bash
mvn spring-boot:run
```

L'applicazione è disponibile su `http://localhost:8080`.

---

## Ruoli utente

Il sistema prevede quattro ruoli:

| Ruolo          | Permessi                                                                                                            |
| -------------- | ------------------------------------------------------------------------------------------------------------------- |
| `ROLE_USER`    | Ruolo di default assegnato alla registrazione. Può leggere gli articoli e inviare richieste di carriera.            |
| `ROLE_WRITER`  | Può creare, modificare ed eliminare i propri articoli. Gli articoli vengono inviati in revisione.                   |
| `ROLE_REVISOR` | Può visualizzare gli articoli in attesa di revisione e accettarli o rifiutarli. Non può valutare i propri articoli. |
| `ROLE_ADMIN`   | Accesso completo. Gestisce le richieste di carriera, le categorie e i ruoli degli utenti.                           |

Un utente può avere un solo ruolo alla volta. Quando l'admin accetta una richiesta di carriera, il ruolo precedente viene sostituito dal nuovo.

---

## Funzionalità principali

### Autenticazione

- Registrazione con login automatico dopo la registrazione
- Login con email e password
- Visualizzazione/occultamento password con icona occhio
- Logout

### Articoli

- Creazione articoli con upload immagini multiple su Supabase
- La prima immagine caricata viene impostata come copertina (`is_primary = true`)
- Modifica e cancellazione articoli (solo dal writer proprietario)
- Ricerca full-text per keyword
- Filtro per categoria cliccando sul badge dell'articolo
- Contatore visualizzazioni (`view_count`) incrementato ad ogni accesso al dettaglio
- Sezione "Articoli più letti" in homepage basata sul contatore

### Revisione articoli

- Dashboard revisore con lista articoli in attesa (`is_accepted = null`)
- Galleria immagini con miniature cliccabili nella pagina di dettaglio revisore
- Bottoni Accetta/Rifiuta con redirect e messaggio di feedback
- Blocco: il revisore non può valutare articoli scritti da lui stesso
- Badge campanella nella navbar con contatore articoli da revisionare

### Dashboard Admin

- Lista richieste di carriera pendenti con badge campanella nella navbar
- Accettazione richieste con assegnazione ruolo (sostituisce il ruolo precedente)
- Gestione categorie
- Pannello gestione utenti: visualizza tutti gli utenti non-admin con ruolo attuale e possibilità di cambiarlo

### Profilo utente

- Visualizzazione dati account: nome, email, data iscrizione, ruolo
- Modifica dati personali: sesso (opzionale), data di nascita (opzionale)
- Cambio password con verifica della password attuale
- Reset password via email con token a scadenza giornaliera
- Eliminazione account con conferma e invalidazione sessione

### Sicurezza

- Protezione route con Spring Security basata su ruoli
- Pagina 403 personalizzata per accessi non autorizzati
- Gestione errore upload file troppo grande con messaggio all'utente
- Blocco richieste duplicate di carriera (stesso ruolo già richiesto o già assegnato)

---

## Struttura del progetto

```
src/main/java/it/aulab/progetto_finale_docente/
├── config/
│   └── SecurityConfig.java
├── controllers/
│   ├── ArticleController.java
│   ├── CategoryController.java
│   ├── GlobalControllerAdvice.java     ← badge navbar globali
│   ├── GlobalExceptionHandler.java     ← gestione errori upload
│   ├── OperationController.java        ← richieste di carriera
│   └── UserController.java             ← home, auth, profilo, admin
├── dtos/
│   ├── ArticleDto.java
│   ├── CategoryDto.java
│   └── UserDto.java
├── models/
│   ├── Article.java
│   ├── CareerRequest.java
│   ├── Category.java
│   ├── Image.java
│   ├── Role.java
│   └── User.java
├── repositories/
│   ├── ArticleRepository.java
│   ├── CareerRequestRepository.java
│   ├── CategoryRepository.java
│   ├── ImageRepository.java
│   ├── RoleRepository.java
│   └── UserRepository.java
└── services/
    ├── ArticleService.java
    ├── CareerRequestService.java / CareerRequestServiceImpl.java
    ├── CategoryService.java
    ├── CustomUserDetails.java
    ├── CustomUserDetailsService.java
    ├── EmailService.java
    ├── ImageService.java
    ├── UserService.java / UserServiceImpl.java
    └── CrudService.java

src/main/resources/
├── templates/
│   ├── admin/
│   │   ├── dashboard.html
│   │   └── users.html
│   ├── article/
│   │   ├── articles.html
│   │   ├── create.html
│   │   ├── detail.html
│   │   └── edit.html
│   ├── auth/
│   │   ├── forgot-password.html
│   │   ├── login.html
│   │   ├── register.html
│   │   └── reset-password.html
│   ├── career/
│   │   ├── requestDetail.html
│   │   └── requestForm.html
│   ├── error/
│   │   └── 403.html
│   ├── revisor/
│   │   ├── dashboard.html
│   │   └── detail.html
│   ├── user/
│   │   └── profile.html
│   ├── writer/
│   │   └── dashboard.html
│   ├── home.html
│   └── index.html                      ← layout con navbar e footer
└── static/
    ├── css/
    │   └── style.css
    └── images/
        └── default.jpg
```

---

## Note per i collaboratori

- Il file `application.properties` non è committato. Ogni sviluppatore deve crearlo manualmente seguendo il template sopra.
- Le migration del database non sono automatiche (`ddl-auto=none` o `validate`). Eseguire i comandi SQL indicati nella sezione Setup.
- Le immagini degli articoli vengono caricate su Supabase, non in locale. Configurare il proprio bucket e aggiornare le chiavi nell'`application.properties`.
- Per i test email usare Mailtrap: le email non vengono consegnate realmente in ambiente di sviluppo.
