# Showbox

Android-App für den Festivalkiosk bei **Wasted in Jarmen**. Sechs Bereiche:
Schicht-Countdown, Musikplayer mit Geschwindigkeitsregler, Quiz, Speed Dating,
Schätzfragen und Podcast-Themen. Kotlin mit Jetpack Compose, durchgehend
dunkles Theme mit gelber Schrift.

## Funktionen

**Schicht** — Beim ersten Start wählst du, wer du bist (Benni, Janna, Jana,
Hagen oder Gifti). Danach läuft ein Countdown bis zur nächsten Schicht; sobald
sie beginnt, zeigt er stattdessen, wie lange sie noch geht. Zum Schichtende
gehen Alarmton und Vibration an und die App meldet **„Zeit für lecker
Bierchen!"**. Der Alarm kommt zusätzlich als Benachrichtigung, wenn die App
geschlossen ist.

**Musik** — Audiodateien vom Gerät laden und abspielen. Ein Slider regelt die
Abspielgeschwindigkeit von 0,5x bis 2,0x in 0,1er-Schritten, tonhöhenkorrigiert.
Dazu Fortschrittsbalken, Play/Pause und ein Zurücksetzen auf Normaltempo.

**Quiz** und **Schätzfragen** — Frage und Antwort. Die Antwort ist zunächst
verdeckt und lässt sich pro Eintrag aufdecken, damit man die Frage erst
vorlesen kann. Neue Einträge lassen sich anlegen und löschen.

**Speed Dating** und **Podcast-Themen** — nur Fragen bzw. Themen, ohne Antwort.
Ebenfalls erweiterbar.

Alle vier Frage-Bereiche sind mit Platzhaltern vorbefüllt.

## Der Schichtplan

Aus `Schichtplan WIJ Kiosk.pdf` übernommen:

| Tag | Schicht | Besetzung |
|---|---|---|
| Donnerstag | 12:00 – 19:00 | Benni & Janna |
| Donnerstag | 19:00 – ca. 02:00 | Hagen & Gifti (DJ Team Ost) |
| Freitag | 08:00 – 15:00 | Jana & Janna |
| Freitag | 15:00 – 22:00 | Benni & Hagen |
| Samstag | 08:00 – 15:00 | Benni & Hagen |
| Samstag | 15:00 – 22:00 | Jana & Janna |
| Sonntag | 08:00 – 12:00 | Gifti & Jana |

Jana und Janna sind zwei verschiedene Personen und haben unterschiedliche
Schichten.

Der Plan nennt nur Wochentage, keine Daten. Die App rechnet deshalb ab einem
einstellbaren **Festivalstart (Donnerstag)**, standardmäßig dem nächsten
Donnerstag. Das Datum lässt sich unten auf dem Schicht-Tab über „Ändern"
setzen — danach stimmen alle Countdowns.

## APK bekommen

Die APK wird von GitHub Actions gebaut, nicht im Repo abgelegt.

1. Tab **Actions** → Workflow **Android CI** → letzter Lauf auf diesem Branch
2. Unter **Artifacts**:
   - `showbox-debug-apk` — mit dem Debug-Keystore signiert, direkt installierbar
   - `showbox-release-apk-unsigned` — von R8 verkleinert, aber **unsigniert**
     und damit nicht installierbar
3. ZIP entpacken, `app-debug.apk` aufs Gerät kopieren, Installation aus
   unbekannten Quellen erlauben, öffnen.

Beim ersten Start nach Benachrichtigungen fragen lassen — sonst kommt der
Schichtalarm nicht an, wenn die App im Hintergrund ist.

## Lokal bauen

Braucht das Android SDK (Platform 35) und JDK 17+:

```bash
./gradlew assembleDebug      # APK unter app/build/outputs/apk/debug/
./gradlew testDebugUnitTest  # Unit-Tests
./gradlew lintDebug
```

## Aufbau

```
app/src/main/java/com/example/showbox/
├── MainActivity.kt
├── alarm/
│   ├── AlarmPlayer.kt         Klingelton und Vibration in der App
│   ├── AlarmScheduler.kt      Systemalarme pro Schichtende
│   └── ShiftAlarmReceiver.kt  Benachrichtigung bei geschlossener App
├── data/
│   ├── Model.kt               Kategorien und Einträge
│   ├── Entries.kt             Validierung neuer Einträge
│   ├── DefaultEntries.kt      Platzhalterfragen
│   ├── LibraryStore.kt        Fragen und Lieder als JSON
│   ├── Playback.kt            Tempobereich und Zeitformatierung
│   ├── ShiftPlan.kt           Rota, Countdown-Logik
│   └── ShiftStore.kt          Person und Festivaldatum
└── ui/
    ├── ShowboxApp.kt          Bottom-Navigation
    ├── LibraryViewModel.kt
    ├── PlayerViewModel.kt     MediaPlayer samt Tempo
    ├── ShiftViewModel.kt      Sekundentakt und Alarmauslösung
    ├── components/
    ├── screens/
    └── theme/                 Dunkel mit Gelb, fest eingestellt
```

Die Zeit- und Speicherlogik ist frei von Android-Typen und liegt in `data/` —
deshalb ist sie ohne Emulator testbar. Die Tests decken unter anderem den
Wechsel von „bis Schichtbeginn" auf „noch so lange", die Donnerstagsschicht
über Mitternacht hinaus und das einmalige Auslösen des Alarms ab.

## Grenzen

- Der Musikplayer spielt nur im Vordergrund; verlässt man die App, pausiert das
  System die Wiedergabe irgendwann. Kein Hintergrunddienst, keine Playlist,
  kein Shuffle.
- Der Schichtalarm im Hintergrund hängt an exakten Alarmen. Verweigert das
  System sie, kommt der Alarm bis zu eine Minute später.
- Der Schichtplan steht im Code, nicht in der Oberfläche — nur das Startdatum
  ist einstellbar.
