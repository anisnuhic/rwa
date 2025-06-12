# rwa

- u DatabaseConnection.java varijable USERNAME i PASSWORD i u VideoGenerator.java varijable DB_USER i DB_PASSWORD promjenite na odgovarajuce za vas mysql server.

- Za pokretanje aplikacije :
    - ./gradlew build
    - ./gradlew appRun

Ukoliko ne postoji baza, napravit ce novu pod nazivom 'youtube_voting' sa relacijom 'videos' koja ce u sebi sadrzati 25 inicijalnih videa
Ukoliko baza postoji, koristit ce vec postojecu

- Za generisanje random videa i dodavanje u bazu:
    - ./gradlew runGenerator

Nakon ovoga ocekuje se unos broja videa koje zelimo generisati (ukoliko samo udarite Enter, generisat ce po defaultu 50).


