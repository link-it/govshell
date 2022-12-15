# GovShell


## Descrizione

Autenticazione e Proxy per l'ecosistema GovHub.

L'autenticazione è form-based, al momento la password è uguale per tutti gli utenti:

```bash
curl -v -X POST 'http://localhost:11001/do-login' -d "username=amministratore&password=password"
```

Utilizzare il cookie di sessione restituito per inviare richieste ai servizi di govhub:

```bash
curl -v --cookie "GOVHUB-JSESSIONID=0A8581FBDA2754C5DB3ADFB2E2018D29" -X GET 'http://localhost:11001/govregistry/users/1'
```

***

## Get

```
cd existing_repo
git remote add origin https://gitlab.link.it/govhub/applications/govshell.git
git branch -M main
git push -uf origin main
```

***

## Installazione

I comandi SQL assumono l'utilizzo di postgres.

Creare l'utenza SQL:

```bash
createuser --interactive
```

- Nome Utente: govhub
- Password Utente: govhub


Creare il database `govhub`

```bash
createdb 'govhub'
```

Creare lo schema per govshell

```bash
cd reverse-proxy
psql govhub govhub < src/main/resources/govshell-schema.sql
psql govhub govhub < src/main/resources/data-dev.sql
```

Creare la cartella di log:

```bash
mkdir /var/log/govshell
```


Eseguire l'applicazione:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE
```

L'applicazione verrà deployata di default sulla porta 11001. Il database delle utenze viene popolato deployando govregistry.

***
## WAR

Per ottenere un war deployabile su application server:


```bash
mvn package -P war -DskipTests
```

L'artefatto verrà prodotto sotto

    target/govshell.war


***
## Configurazione

Le applicazioni vengono cachate in modo da velocizzare il processo routing. Per modificare l'intervallo di pulizia della cache dell'anagrafica appilcazioni:

    caching.govhub.applications.TTL = 300000

