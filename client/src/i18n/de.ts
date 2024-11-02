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
  },
  unitType: {
    WORKER: "Arbeiter",
    SWORDSMAN: "Schwertkämpfer",
    SPEARMAN: "Speerträger",
    HORSEMAN: "Reiter",
  },
  serverError: {
    notEnoughBeer: "Nicht genug Bier vorhanden.",
    onlyOnePerVillage:
      "Du kannst nur eine Brauerei, eine Farm und eine Burg pro Dorf bauen.",
    tooManyMoves:
      "Die Einheit hat ihr Limit an Zügen für diese Stunde erreicht.",
    notYourBuilding: "Du kannst nur deine eigenen Gebäude zerstören.",
    lastVillage: "Du kannst dein letztes Dorf nicht zerstören.",
  },
  serverSuccess: {
    beerCollected: "Bier erfolgreich gesammelt.",
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
  },
  startVillageAction: {
    text: "Bitte wähle einen Standort für dein erstes Dorf.",
    dialog: "Möchtest du hier starten?",
  },
  villageAction: {
    createWorker: "Arbeiter erstellen",
    moveUnit: "Einheit bewegen ({0}/{1})",
    villageOf: "Dorf von {playerName}",
  },
  farmAction: {
    farmOf: "Farm von {playerName}",
  },
  breweryAction: {
    breweryOf: "Brauerei von {playerName}",
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
  },
  castleAction: {
    castleOf: "Burg von {playerName}",
    createSwordsman: "Schwertkämpfer erstellen",
    createSpearman: "Speerkämpfer erstellen",
    createHorseman: "Reiter erstellen",
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
    },
    other: {
      UNIT_MOVED: "{playerName} hat einen {unitType} nach {x}, {y} bewegt.",
      UNIT_CREATED: "{playerName} hat einen {unitType} erstellt.",
      BUILDING_CREATED: "{playerName} hat ein Gebäude erstellt.",
      BEER_COLLECTED: "{playerName} hat Bier gesammelt.",
      BUILDING_CONQUERED: "{playerName} hat ein Gebäude bei {x}, {y} erobert.",
      BUILDING_DESTROYED: "{playerName} hat ein Gebäude bei {x}, {y} zerstört.",
      LOST_UNIT: "{playerName} hat einem {unitType} bei {x}, {y} verloren.",
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
};
