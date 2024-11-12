export const de = {
  general: {
    username: "Benutzername",
    password: "Passwort",
    serverError: "Ein Fehler ist aufgetreten. Seite neu laden?",
    no: "Nein",
    yes: "Ja",
    close: "Schließen",
    logout: "Abmelden",
    back: "Zurück",
    cancel: "Abbrechen",
    lessThanMinute: "Gerade eben",
    inLessThanMinute: "In weniger als einer Minute...",
    ago: "vor {0}",
    changeLanguage: "Sprache ändern: {0}",
    loading: "Lädt...",
    welcome: "Willkommen {0}!",
    passwordRepeated: "Bitte bestätige dein Passwort.",
    unit: "Einheit",
    toGame: "Zum Spiel",
  },
  unitType: {
    WORKER: "Arbeiter",
    SWORDSMAN: "Schwertkämpfer",
    SPEARMAN: "Speerträger",
    HORSEMAN: "Reiter",
  },
  buildingType: {
    VILLAGE: "Dorf",
    CASTLE: "Burg",
    FARM: "Farm",
    BREWERY: "Brauerei",
    CITY: "Stadt",
    MARKET: "Markt",
  },
  serverError: {
    notEnoughBeer: "Nicht genug Bier vorhanden.",
    onlyOnePerVillage:
      "Du kannst nur zwei Brauereien, eine Farm und eine Burg pro Dorf oder Stadt bauen.",
    tooManyMoves:
      "Die Einheit hat ihr Limit an Zügen für diese Stunde erreicht.",
    notYourBuilding: "Du kannst nur deine eigenen Gebäude zerstören.",
    lastVillage: "Du kannst dein letztes Dorf nicht zerstören.",
    noFarm: "Keine Farm in der Nähe!",
    tooManyUnits: "Du hast das Limit an Einheiten erreicht. Baue mehr Burgen.",
    noCity: "Du benötigt zunächst eine Stadt.",
    notEnoughGold: "Nicht genug Gold vorhanden.",
  },
  serverSuccess: {
    beerCollected: "Bier erfolgreich gesammelt.",
    buildingDestroyed: "Gebäude erfolgreich zerstört.",
  },
  registration: {
    title: "Registrierung",
    error:
      "Fehler bei der Registrierung aufgetreten. Ggf. ist der Nutzername schon vergeben.",
    action: "Account erstellen",
    toLogin: "Zum Login",
    passwordsNotEqual: "Passwörter stimmen nicht überein.",
    intro:
      "Willkommen beim Browsergame <b><i>Castles of Beer and Dragons</i></b>! Melde dich jetzt an und erlange Ruhm und Reichtum.",
    cookieInfo:
      "Diese Seite verwendet keine Cookies und es werden keine Personenbezogenen Daten gespeichert. Beim Verwenden des Spiel werden Spielereignisse und -daten auf dem Server und im Browser gespeichert. Falls du damit nicht einverstanden bist, spiele nicht.",
  },
  login: {
    title: "Login",
    error:
      "Fehler bei der Anmeldung aufgetreten. Bitte überprüfe deine Eingaben.",
    action: "Anmelden",
    toRegistration: "Zur Registrierung",
    aboutTheGame: "Mehr über das Spiel",
  },
  startVillageAction: {
    text: "Bitte wähle einen Standort für dein erstes Dorf.",
    dialog: "Möchtest du hier starten?",
  },
  villageAction: {
    createWorker: "Arbeiter erstellen",
    moveUnit: "Einheit bewegen ({0}/{1})",
    villageOf: "Dorf von {playerName}",
    upgradeToCity: "Zu Stadt ausbauen",
  },
  farmAction: {
    farmOf: "Farm von {playerName}",
  },
  breweryAction: {
    breweryOf:
      "Brauerei von {playerName} welche {beer} Bier pro Stunde produziert und maximal {breweryBeerStorage} Bier speichert. Aktuell sind {beerToCollect} Bier verfügbar.",
    noFarmNextTo:
      "Diese Brauerei hat keine Farm in der Nähe und produziert daher kein Bier.",
  },
  unitAction: {
    unitOf: "Einheit von {playerName} in {x}, {y}",
    chooseAction: "Wähle eine Aktion für die Einheit:",
    moveText: "Wohin soll die Einheit bewegt werden?",
    movesRemaining:
      "Diese Einheit hat noch {0} von {1} Zügen pro Stunde übrig.",
    buildFarm: "Farm bauen",
    buildBrewery: "Brauerei bauen",
    buildCastle: "Burg bauen",
    buildVillage: "Dorf bauen",
    buildMarket: "Markt bauen",
    build: "Bauen...",
    delete: "Einheit zerstören",
    deleteQuestion: "Möchtest du diese Einheit wirklich zerstören?",
  },
  castleAction: {
    castleOf: "Burg von {playerName}",
    createSwordsman: "Schwertkämpfer erstellen",
    createSpearman: "Speerkämpfer erstellen",
    createHorseman: "Reiter erstellen",
    destroy: "Burg zerstören",
    units: "Einheiten...",
    upgrade: "Burg ausbauen",
  },
  events: {
    showOnMap: "Ereignisse auf Karte anzeigen?",
    noEvents: "Keine Ereignisse vorhanden...",
    openOverlay: "Ereignisse",
    buildingDestroyed: "Gebäude zerstört!",
    buildingConquered: "Gebäude erobert!",
    lostUnit: "Einheit verloren!",
    wonFight: "Kampf gewonnen!",
    own: {
      UNIT_MOVED: "Du hast einen {unitType} nach {x}, {y} bewegt.",
      UNIT_CREATED: "Du hast einen {unitType} erstellt.",
      BUILDING_CREATED: "Du hast ein Gebäude erstellt.",
      BEER_COLLECTED: "Du hast Bier gesammelt.",
      BUILDING_CONQUERED: "Du hast ein Gebäude bei {x}, {y} erobert.",
      BUILDING_DESTROYED: "Du hast ein Gebäude bei {x}, {y} zerstört.",
      LOST_UNIT: "Du hast einem {unitType} bei {x}, {y} verloren.",
      GAME_OVER: "Du hast dein letztes Dorf verloren.",
      SOLD_BEER_FOR_GOLD: "Du hast Bier für Gold verkauft.",
    },
    other: {
      UNIT_MOVED: "{playerName} hat einen {unitType} nach {x}, {y} bewegt.",
      UNIT_CREATED: "{playerName} hat einen {unitType} erstellt.",
      BUILDING_CREATED: "{playerName} hat ein Gebäude erstellt.",
      BEER_COLLECTED: "{playerName} hat Bier gesammelt.",
      BUILDING_CONQUERED: "{playerName} hat ein Gebäude bei {x}, {y} erobert.",
      BUILDING_DESTROYED: "{playerName} hat ein Gebäude bei {x}, {y} zerstört.",
      LOST_UNIT: "{playerName} hat einem {unitType} bei {x}, {y} verloren.",
      GAME_OVER: "{playerName} hat sein letztes Dorf verloren.",
      SOLD_BEER_FOR_GOLD: "{playerName} hat Bier für Gold verkauft.",
    },
  },
  tutorialAction: {
    complete: "Tutorial abschließen",
    FIRST_WORKER: "Klicke auf ein Dorf und erstelle dort einen Arbeiter.",
    FIRST_FARM:
      "Bewege den Arbeiter auf eine freie Wiese und baue eine Farm. Stelle sicher, dass neben der Wiese eine weitere für die Brauerei ist!",
    FIRST_BREWERY:
      "Erstelle einen weiteren Arbeiter, bewege ihn auf eine freie Wiese neben einer Farm und baue eine Brauerei.",
    FIRST_BEER_COLLECTED:
      "Tippe auf das Bier-Symbol über der Brauerei und sammle dein erstes Bier.",
    FIRST_CASTLE:
      "Baue deine erste Burg, um neue Einheiten erstellen zu können.",
    FIRST_UNIT:
      "Erstelle eine Einheit in der Burg um dich verteidigen zu können.",
  },
  wiki: {
    link: "Regeln & Infos",
    description:
      "Bei Castles baut ihr euer eigenes Imperium aus Burgen auf und verteidigt\n" +
      "      es gegen andere Spieler und Barbaren. Hier erfahrt ihr alles wichtige zum\n" +
      "      Spiel und den Regeln.",
    title1: "Die Karte",
    paragraph1:
      "Die Karte besteht aus vier verschiedenen Typen von Feldern: Wald, Wiese,\n" +
      "      Wasser und Berg. Gebäude können nur auf Wiesen platziert werden. Einheiten\n" +
      "      können nicht über Wasser laufen. Die Karte ist fast endlos groß und\n" +
      "      aktualisiert sich während des Spiels in Echtzeit.",
    title2: "Burgen, Dörfer, Brauereien ...",
    paragraph2:
      "Es gibt verschiedene Gebäude im Spiel, die unterschiedliche Funktionen\n" +
      "      haben. Farm und Brauerei produzieren Bier, das für den Bau von Einheiten\n" +
      "      und Gebäuden gebraucht wird. Farm und Brauerei müssen direkt nebeneinander\n" +
      "      sein und man kann 2 Brauereien und eine Farm pro Dorf bauen. In Burgen könnt ihr Einheiten zur Verteidigung und zum Angriff\n" +
      "      erstellen. Dörfer können Arbeiter ausbilden und speichern euer Bier. Je\n" +
      "      mehr Dörfer ihr habt, desto mehr Bier könnt ihr speichern.\n" +
      "      Im Fall einer Eroberung werden Farm und Brauerei zerstört. Dörfer und\n" +
      "      Burgen werden eingenommen.",
    title3: "Einheiten und Kämpfe",
    paragraph3:
      "In Burgen und Dörfern könnt ihr Einheiten bauen. Dies kostet euch Bier.\n" +
      "      Auch das Bewegen von Einheiten kostet Bier. Es kann immer nur eine Einheit\n" +
      "      auf einem Feld sein. Sind befeindete Einheiten auf einem Feld, kommt es\n" +
      '      zum Kampf. Kämpfe funktionieren nach dem Prinzio "Stein, Schere, Papier".\n' +
      "      Reiter gewinnt gegen Schwertkämpfer, Schwertkämpfer gewinnt gegen\n" +
      "      Speerträger und Speerträger gewinnt gegen Reiter. Der Gewinner bleibt auf\n" +
      "      dem Feld, der Verlierer wird zerstört. Arbeiter können nicht angreifen und\n" +
      "      werden von Kampfeinheiten zerstört.",
    title4: "Barbaren",
    paragraph4:
      "Auf der Karte erscheinen willkürlich Barbaren. Diese greifen euch und\n" +
      "      andere Spieler an. Sie zerstören Gebäude und töten Einheiten. Ihr könnt\n" +
      "      Barbaren besiegen, indem ihr Einheiten auf sie bewegt. Die Bewegungen von\n" +
      "      Barbaren sind zufällig. Die Anzahl der Barbaren richtet sich nach der\n" +
      "      Anzahl der Spieler",
    title5: "Drachen, Städte und mehr",
    paragraph5:
      "Ich entwickele Castles stetig weiter und freue mich auf eure\n" +
      "      Unterstützung! Drachen, Städte und vieles mehr sind geplant. Bleibt\n" +
      "      gespannt und schreibt mir gerne, wenn ihr Ideen oder Anregungen habt.\n" +
      "      Vielen Dank fürs Testen und Spielen! 🍻",
    title6: "Kosten",
    paragraph6:
      "Kosten für Gebäude und Einheiten berechnen sich auf Basis von bereits erstellten Gebäuden und Einheiten." +
      "Besitzt man z.B. bereits eine Brauerei, so kostet die nächste Brauerei mehr Bier. " +
      "Gleiches gilt für Einheiten und Burgen. Beispiel: Man besitzt 3 Einheiten (Typ egal) und " +
      "der Basispreis für einen Reiter beträgt 100 Bier. Der nächste Reiter würde 100 x 2 x 2 x 2 = 800 Bier kosten.",
  },
  users: {
    link: "Spieler",
    description:
      "Spielerliste sortiert nach Punkten. Pro Gebäude gibt es 2 Punkte, pro Einheit 1 Punkt.",
  },
  userProfile: {
    link: "Profil",
    selectAvatar: "Wähle ein Avatar-Bild:",
    showOnMap: "Auf Karte anzeigen",
  },
  destroyBuilding: {
    button: "Gebäude zerstören",
    question: "Möchtest du dieses Gebäude wirklich zerstören?",
  },
  cityAction: {
    cityOf: "Stadt von {playerName}",
  },
  unitsAndBuildings: {
    units: "Einheiten",
    buildings: "Gebäude",
    link: "Einheiten & Gebäude",
    description:
      "Hier siehst du alle Einheiten und Gebäude von {playerName}. Klicke auf ein Gebäude oder eine Einheit um es auf der Karte anzuzeigen.",
  },
  marketAction: {
    marketOf: "Markt von {playerName}",
    sellFor1Gold: "Bier für 1 Gold verkaufen",
    sellFor10Gold: "Bier für 10 Gold verkaufen",
  },
};
