# Giftshop

Ein kleiner Android-Geschenkeladen: Katalog mit Kategoriefilter, Produktdetail
und Warenkorb inklusive Versandkostenlogik. Native App in Kotlin mit Jetpack
Compose und Material 3.

## APK bekommen

Die APK wird von GitHub Actions gebaut, nicht lokal eingecheckt.

1. Tab **Actions** → Workflow **Android CI** → letzter Lauf auf diesem Branch
2. Unten unter **Artifacts**:
   - `giftshop-debug-apk` — mit dem Debug-Keystore signiert, direkt auf einem
     Gerät installierbar
   - `giftshop-release-apk-unsigned` — von R8 verkleinert, aber **unsigniert**
     und damit nicht installierbar
3. ZIP entpacken, `app-debug.apk` aufs Gerät kopieren, dort Installation aus
   unbekannten Quellen erlauben und öffnen.

## Lokal bauen

Braucht das Android SDK (Platform 35, Build-Tools) und ein JDK 17+:

```bash
./gradlew assembleDebug      # APK unter app/build/outputs/apk/debug/
./gradlew testDebugUnitTest  # Unit-Tests der Warenkorb- und Preislogik
./gradlew lintDebug          # Android Lint
```

Fehlt das SDK, findet Gradle es über `local.properties` (`sdk.dir=…`) oder die
Umgebungsvariable `ANDROID_HOME`.

## Aufbau

```
app/src/main/java/com/example/giftshop/
├── MainActivity.kt          Einstiegspunkt, setzt Theme und Edge-to-Edge
├── data/
│   ├── Product.kt           Modell, Kategorien, Preisformatierung
│   └── ProductRepository.kt  Katalog (aktuell in-memory)
└── ui/
    ├── GiftshopApp.kt       NavHost und Routen
    ├── CartViewModel.kt     Warenkorb, aktivitätsweit geteilt
    ├── components/          ProductArtwork, QuantityStepper
    ├── screens/             Katalog, Detail, Warenkorb
    └── theme/               Farben, Typografie, Material-You-Anbindung
```

Der Warenkorb hält seine Produkte selbst statt IDs in den Katalog
nachzuschlagen — dadurch hängt er nicht am Repository und ist ohne Android-
Framework testbar.

Produktbilder gibt es keine: jede Kachel ist die Akzentfarbe des Produkts mit
seinem Emoji darauf. Das hält das Repo frei von Binärdateien. Wenn echte Fotos
dazukommen, ist `ProductArtwork` die einzige Stelle, die sich ändert.

## Eckdaten

| | |
|---|---|
| minSdk | 26 (Android 8.0) |
| targetSdk / compileSdk | 35 |
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.10.2 |
| Compose BOM | 2024.10.01 |

## Was noch fehlt

Der Katalog ist fest im Code, der Warenkorb überlebt keinen App-Neustart, und
„Kostenpflichtig bestellen" leert nur den Korb und zeigt eine Snackbar — es gibt
keine Kasse, kein Backend und keine Zahlungsanbindung.
