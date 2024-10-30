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
    noEvents: "Keine Events vorhanden...",
    openOverlay: "Events anzeigen",
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
};
