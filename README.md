# ETSF20 Base Journal System

## Krav
Java 17 JDK eller en senare LTS version som exempelvis 21.

Installera för din maskin härifrån:
https://adoptium.net/temurin/releases/?version=17&package=jdk

## Java, H2 och SQL referensmaterial
Javakoden använder [lambdas](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html), [method-references](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html), [records](https://docs.oracle.com/en/java/javase/17/language/records.html) 
och andra konstruktioner som är tillgängliga i språket i 17 och framåt.

För SQL så finns det mycket matnyttigt hos [W3 Schools](https://www.w3schools.com/sql/).

Dokumentation om H2 [finns här](https://www.h2database.com/), en H2 specifik extension används och [dokumenteras här](https://www.h2database.com/html/commands.html#merge_into).

Javadoc för grundsystemet [finns här](https://pages.coursegit.cs.lth.se/etsf20/base-system/).

## Snabbstart från terminalen - miljötest
Nedan laddar ner alla beroenden, bygger hela projektet och sedan skapar en jar fil.

För alla nix system (Linux, macOS):
```bash
./mvnw package
cd target
java -jar base-journal-system-1.0.1.jar
```

För Windows:
```bat
./mvnw.cmd package
cd target
java -jar base-journal-system-1.0.1.jar
```
Om applikationen ska paketeras och köras på annan maskin så krävs ovan jar samt hela katalogen `libs` 
som innehåller alla beroenden.

### Viktigt!
För all användning av `./mvnw` framåt antas du på en Windows maskin lägga på `.cmd` så att det blir `./mvnw.cmd`

### Alternativa sätt (rekommenderas inte för Windows maskiner)
Snabbvariant för ovan att starta systemet från terminalen:

```bash
./mvnw package exec:exec -Pmain
```

Skippa att köra testen med:
```bash
./mvnw package exec:exec -Pmain -DskipTests
```

På Windows maskiner så fungerar inte CTRL+C inte korrekt utan processen blir kvar i 
bakgrunden och måste stängas av i aktivitetshanteraren.

## Komma igång med utvecklingsmiljö

För alla miljöer så finns huvudmetoden i `etsf20.basesystem` och klassen heter `Main`.

### IntelliJ IDEA (rekommenderad)

1. Ladda ner och installera: https://www.jetbrains.com/idea/download/ (utan licens välj IDEA Community Edition)
2. Öppna pom.xml, du blir tillfrågad "Open as project" eller "Open as file", välj "Open as project"
3. Du är igång.

När du öppnar en JTE mall-fil så kommer du bli frågad om att installera ett plug-in, denna rekommenderas och finns inte i
någon annan utvecklingsmiljö.

### Visual Studio Code 

1. Ladda ner och installera: https://code.visualstudio.com
2. Installera "Extension Pack for Java": https://code.visualstudio.com/docs/java/extensions
3. Starta upp Visual Studio Code, File -> Open Folder, navigera till katalogen som innehåller pom.xml och öppna
4. Om allt går vägen ska du se "Java: Activating" och i slutet när allt är klart: "Java: Ready" i nedre delen av Visual Studio Code fönstret.
5. Du är igång.

### Eclipse

1. Ladda ner och installera: https://www.eclipse.org/downloads/packages/release/2024-12/r/eclipse-ide-java-developers
2. Starta upp Eclipse, välj en plats för ditt workspace.
3. Välj File -> Import
4. Leta upp Maven och sedan "Existing Maven Projects"
5. För "Root Directory", tryck på "Browse..." och välj den katalog av grundsystemet som har en pom.xml i sig (**viktigt!**).
6. Om allt gått väl ska /pom.xml dyka upp med namnet "etsf20.basesystem:base-journal-system:1.0.1.jar", se till att det är i bockat.
7. Tryck på Finish och vänta en liten stund, nu ska projektet finnas att välja
8. Du är igång.

## Databastips

### Nollställa databasen
Radera filerna `data.mv.db` och `data.trace.db` i arbetskatalogen, alla filer finns eventuellt inte.

Starta sedan grundsystemet och som en del av startsekvensen kommer `src/main/resources/schema.sql` köras för att skapa 
den initiala databasen.

Efter denna punkt körs den aldrig igen, du måste alltså radera filerna för att databasen ska återskapas genom `schema.sql`.

### Åtkomst utifrån via en databasklient
H2 databaser är exklusivt låsta av den applikation som använder dessa. Du kan aktivera det som 
kallas *automatic mixed mode* vilket gör att flera applikationer kan öppna samma databas. Tänk på att 
detta startar en server i bakgrunden.

Aktivering av *automatic mixed mode* görs i grundsystemet genom att sätta `DEFAULT_MIXED_MODE` till 
`true` i `etsf20.basesystem.Config`. I `Config` kan även alla inställningar sättas för användarnamn och lösenord.

Därefter kommer systemet vid start skriva ut en JDBC URL som kan användas med en databasklient.

### Förslag på klienter:

 * Inbyggd klient, kör `./mvnw exec:exec -Ph2`
 * [**SQL Workbench/J**](https://www.sql-workbench.eu/). välj "Generic package for all systems including all optional libraries"
 * [**DBVisualizer**](https://www.dbvis.com)

## Fler Maven operationer

För maven så finns en del operationer som kan vara bra att känna till, alla läggs efter `./mvnw`.

 * `clean` - raderar byggnations data, kan vara hjälpsamt vid konstiag problem
 * `compile` - kompilerar projektet
 * `test` - kompilera och kör alla JUnit test från terminalen
 * `package` - kompilerar, kör alla test och paketerar till jar och kopierar alla beroenden till `target/` och `target/libs`
 * `javadoc:javadoc` - genererar javadoc hemsida, finns i `target/reports/apidocs` relativt projekts rotkatalog, öppna index.html i din favorit webbläsare.
 * `exec:exec -P[program]` - startar inbyggda program från terminalen

Om du behöver skippa test för att få något byggt, så lägg på växeln `-DskipTests`

## Länkar till använda ramverk och annat material:
 * **Maven:** https://maven.apache.org/
 * **H2:**  http://www.h2database.com
 * **Javalin:** https://javalin.io/
 * **JTE (Java Template Engine):** https://jte.gg/
 * **Boostrap:** https://getbootstrap.com/docs/5.3/getting-started/introduction/
