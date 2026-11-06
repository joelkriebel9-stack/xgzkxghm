# GambleFX (Fabric, Minecraft 26.2)

Gambling-Animationen fuer Hypixel SkyBlock. Rein visuell/clientseitig: liest nur Chat und Fenstertitel.

## Voraussetzungen
- JDK 25, Gradle 9.3+ (oder IntelliJ), Internet fuer den ersten Build
- Zum Spielen: Fabric Loader 0.19.3+, Fabric API 0.154.2+26.2, Java 25

## .jar bauen
    gradle wrapper --gradle-version latest   # einmalig
    ./gradlew build                          # Windows: gradlew.bat build

Ergebnis: `build/libs/gamblefx-1.0.0.jar` (NICHT die -sources.jar) -> in `.minecraft/mods`, zusammen mit Fabric API.

## Bedienung
- Menue: **Rechts-Shift** (aenderbar unter Steuerung) oder `/gamblefx` (Alias `/gfx`)
- Grosser Knopf im Menue: **Drop-Animation testen** (Diana-Beispiel "Chimera I")
- Pro Kategorie: Schalter + "Test"-Knopf
- Globale Schalter: alle Animationen, Sounds, Konfetti, Tempo, Stil (Crate-Reveal / Case Opening / Slot Machine / Gemischt)
- Befehle: `/gamblefx diana`, `/gamblefx test [KATEGORIE] [SELTENHEIT]`, `/gamblefx list`
- Config: `config/gamblefx.json`

## Anpassen
- Chat-Muster / Kistentitel: `Triggers.java`
- Item-Symbol je Drop: `ItemResolver.java`
- Kistenfarben: `Crate.java`

## Falls der Build wegen API-Namen scheitert
26.2 ist neu; Namen koennen sich zwischen 26.x-Versionen aendern. Verdaechtige Stellen:
`ScreenEvents.afterExtract` (GambleFXClient), `SimpleSoundInstance.forUI` (Anim), `Loom-Version` (gradle.properties: alternativ 1.17-SNAPSHOT).

## Jar ohne lokale Installation bauen (GitHub Actions)
1. Neues GitHub-Repo anlegen, den Inhalt dieses Ordners hochladen (inkl. `.github/`).
2. Reiter "Actions" -> "Build jar" laeuft automatisch (oder "Run workflow").
3. Fertigen Lauf oeffnen -> unter "Artifacts" `gamblefx-jar` herunterladen (ZIP, darin die .jar).
