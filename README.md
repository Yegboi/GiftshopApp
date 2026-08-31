# GiftshopApp

Zwei eigenständige Android-Apps in einem Repo. Sie teilen sich Gradle-Wrapper,
Versionskatalog und CI, werden aber getrennt gebaut und installieren sich auf
dem Handy nebeneinander.

| Modul | App | Worum es geht |
|---|---|---|
| `app/` | **Giftshop Crew** | Schicht-Countdown, Musikplayer mit Tempo-Regler, Quiz, Speed Dating, Schätzfragen, Podcast-Themen |
| `steamfun/` | **Steam Fun** | Ratespiel: echte Steam-Seiten, wie viele Reviews hat das Spiel? |

## APK bekommen

Beide APKs baut GitHub Actions, sie liegen nicht im Repo.

1. Tab **Actions** → Workflow **Android CI** → letzter Lauf auf diesem Branch
2. Unter **Artifacts**:
   - `giftshop-crew-debug-apk` — Giftshop Crew, direkt installierbar
   - `steam-fun-debug-apk` — Steam Fun, direkt installierbar
   - `release-apks-unsigned` — beide von R8 verkleinert, aber **unsigniert**
     und damit nicht installierbar
3. ZIP entpacken, `app-debug.apk` aufs Gerät, Installation aus unbekannten
   Quellen erlauben, öffnen.

---

# Giftshop Crew

## Funktionen

**Schicht** — Beim ersten Start wählst du, wer du bist (Benni, Janna, Jana,
Hagen oder Gifti). Danach läuft ein Countdown bis zur nächsten Schicht; sobald
sie beginnt, zeigt er stattdessen, wie lange sie noch geht. Zum Schichtende
gehen Alarmton und Vibration an und die App meldet **„Zeit für lecker
Bierchen!"**. Der Alarm kommt zusätzlich als Benachrichtigung, wenn die App
geschlossen ist.

**Musik** — Audiodateien vom Gerät laden und abspielen. Ein Slider regelt die
Abspielgeschwindigkeit von **0,05x bis 2,0x** — also bis auf ein Zwanzigstel des
Normaltempos — tonhöhenkorrigiert. Der Regler läuft logarithmisch: linear läge
der ganze Langsam-Bereich in 23 % der Strecke, so sind es 62 %, und 1,0x liegt
bei 81 %. Der Knopf „Normal (1,0x)" trifft das Normaltempo exakt.
Dazu Fortschrittsbalken und Play/Pause.

Verlangsamt wird wie bei einem Plattenspieler: die Tonhöhe fällt mit dem Tempo.
Technisch wird die Tonhöhe auf denselben Faktor gesetzt wie die Geschwindigkeit —
damit wird das Stretch-Verhältnis intern 1, der Zeitdehner fällt weg und es
bleibt reines Resampling. Mit Geschwindigkeit allein müsste der Zeitdehner bei
0,05x das Zwanzigfache an Material erfinden, was zerhackt klingt.

Wie tief ein Resampler geht, ist geräteabhängig. Lehnt das Gerät eine Rate ab,
wird schrittweise etwas schneller nachgefragt, bis eine angenommen wird — der
Regler landet dann auf dem Langsamsten, was dieses Gerät kann, statt zu
verweigern.

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
│   ├── Playback.kt            Tempobereich, Reglerkurve, Zeitformatierung
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

Das gesamte `data/`-Paket ist frei von Android-Typen und lässt sich deshalb
auch ohne Emulator ausführen — 63 Unit-Tests decken es ab. Die Tests decken unter anderem den
Wechsel von „bis Schichtbeginn" auf „noch so lange", die Donnerstagsschicht
über Mitternacht hinaus und das einmalige Auslösen des Alarms ab.

## Namen im Code

Die App heißt **Giftshop Crew**; Paket und Ordner heißen weiterhin
`com.example.showbox`. Das ist Absicht: die `applicationId` zu ändern würde
eine bereits installierte App nicht aktualisieren, sondern eine zweite
danebenstellen. Sag Bescheid, wenn das Paket trotzdem mitziehen soll.

## Grenzen

- Der Musikplayer spielt nur im Vordergrund; verlässt man die App, pausiert das
  System die Wiedergabe irgendwann. Kein Hintergrunddienst, keine Playlist,
  kein Shuffle.
- Der Schichtalarm im Hintergrund hängt an exakten Alarmen. Verweigert das
  System sie, kommt der Alarm bis zu eine Minute später.
- Der Schichtplan steht im Code, nicht in der Oberfläche — nur das Startdatum
  ist einstellbar.

---

# Steam Fun

Ratespiel: die App zeigt eine zufällige Steam-Seite, du schätzt, wie viele
Reviews das Spiel hat. Liegst du richtig, gibt es Konfetti; liegst du daneben,
ein rotes ❌. Die echte Zahl erscheint erst nach dem Tipp.

Gezeigt wird die Seite mit allem, was dazugehört: Titelbild, Trailer (antippen
zum Abspielen), Screenshot-Karussell (antippen für Vollbild), Kurzbeschreibung,
Entwickler, Erscheinungsdatum, Preis und Genres. Über „Ganze Beschreibung" öffnet
sich Steams eigener Beschreibungstext als HTML — mitsamt der eingebetteten GIFs
und AVIF-Bilder, die dort oft stehen.

## Zwei Modi

**Roundabout** — sechs Knöpfe: `0 – 10`, `10 – 100`, `100 – 500`, `500 – 1.000`,
`1.000 – 5.000`, `5.000+`. Die Beschriftungen teilen sich ihre Endpunkte, deshalb
gehört jeder Endwert zum unteren Bereich: 100 Reviews sind `100 – 500`, 101 dann
`500 – 1.000`. So fällt jede Zahl in genau einen Bereich.

**Accurate** — du tippst die Zahl selbst ein. Exakt treffen kann niemand, sonst
gäbe es nie Konfetti, also zählt ein Tipp innerhalb von **±25 %** als Treffer.
Bei Spielen mit sehr wenigen Reviews greift eine Untergrenze von 2, damit auch
die spielbar bleiben. Nach dem Tipp steht da, um wie viel Prozent du daneben lagst.

## Woher die Daten kommen

Jede Runde ist eine **Live-Abfrage an Steam**. Nichts wird vorab geladen,
nichts liegt auf dem Gerät, nichts steckt in der App:

- `store.steampowered.com/search/results/?json=1` → ein zufälliger Ausschnitt
  aus Steams laufender Store-Liste
- `store.steampowered.com/api/appdetails` → Seite, Medien, Beschreibung
- `store.steampowered.com/appreviews/<id>?json=1` → `total_reviews`

Die Store-Suche liefert mit `total_count` mit, wie viele Titel Steam gerade
listet, und lässt sich an jedem beliebigen Offset anspringen. Die App würfelt
also ein Offset zwischen 0 und `total_count` und nimmt, was dort steht. Weil
jedes Offset gleich wahrscheinlich ist, spielt es keine Rolle, wie Steam die
Ergebnisse sortiert — gezogen wird gleichverteilt über den ganzen Store. Unten
auf dem Bildschirm steht die Zahl, aus der gerade gezogen wird.

Pro Abfrage werden 50 Einträge geholt und gemischt, damit eine Suchanfrage
mehrere Runden trägt statt Steam für jeden Zug einzeln zu belasten. Ist der
Vorrat aufgebraucht, wird ein neues zufälliges Offset gewürfelt.

**Es gibt bewusst keine Ersatzliste.** Eine frühere Fassung fiel bei Netzproblemen
still auf eine eingebaute Auswahl bekannter Spiele zurück — mit dem Ergebnis,
dass genau die immer wieder kamen. Antwortet Steam nicht, sagt die App das jetzt
und bietet einen neuen Versuch an, statt heimlich etwas anderes zu spielen.

Damit ist auch die angezeigte Reviewzahl immer die aktuelle und kann gar nicht
erfunden sein. **Ohne Internet läuft das Spiel nicht** — gespeicherte Zahlen
würden veralten und als „tatsächliche" Zahl schlicht falsch dastehen.

## Aufbau

```
steamfun/src/main/java/com/example/steamfun/
├── MainActivity.kt
├── data/
│   ├── ReviewBucket.kt   die sechs Bereiche, lückenlos und überschneidungsfrei
│   ├── Guessing.kt       Modi, Trefferregeln, Eingabe-Parsing
│   ├── StorePage.kt      Seite, Screenshots, Trailer
│   ├── SteamSearch.kt    zufälliges Offset in Steams Store-Liste
│   ├── SteamJson.kt      Antworten von Steam lesen
│   └── SteamApi.kt       HTTP
└── ui/
    ├── SteamFunViewModel.kt  Ziehung, Rundenablauf, Punktestand
    ├── GameScreen.kt         Modi, Eingabe, Ergebnis
    ├── StorePageView.kt      Titelbild, Trailer, Screenshots, Fakten
    ├── Overlays.kt           Vollbild für Screenshot, Trailer, Beschreibung
    ├── Confetti.kt           Partikel auf einem Canvas, ohne Fremdbibliothek
    └── theme/                Steams eigene Dunkelblautöne
```

Die Reviewzahl reist zwar ab der ersten Sekunde im Zustand mit, aber nur der
Zustand `Answered` darf sie rendern — das ist das ganze Spiel.

`ReviewBucket`, `Guessing`, `SteamJson` und `SteamSearch` kommen ohne
Android-Typen aus und sind mit 62 Unit-Tests abgedeckt — unter anderem gegen
Steams Eigenart, bei unbekannten Appids `"data": []` statt eines Objekts zu
senden, und gegen die `http://`-Medienlinks, die Android sonst als Klartext
blockiert.

## Grenzen

- Getestet ist die Logik, nicht die Verbindung: die Steam-API war aus der
  Bauumgebung nicht erreichbar, das Zusammenspiel zeigt sich erst auf dem Gerät.
- `appdetails` ist ratenbegrenzt (grob 200 Anfragen pro 5 Minuten). Da eine
  Runde mehrere Anläufe braucht, kann sehr schnelles Durchklicken in die Drossel
  laufen.
- Trailer laufen über die Systemkomponente `VideoView`. Exotische Formate, die
  das Gerät nicht kann, spielen nicht.
