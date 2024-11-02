export const en = {
  general: {
    username: "Username",
    password: "Password",
    serverError: "An error occurred. Reload page?",
    no: "No",
    yes: "Yes",
    close: "Close",
    logout: "Logout",
    back: "Back",
    cancel: "Cancel",
    lessThanMinute: "Just now",
    inLessThanMinute: "In less than a minute...",
    ago: "{0} ago",
    changeLanguage: "Change language: {0}",
    loading: "Loading...",
    welcome: "Welcome {0}!",
    passwordRepeated: "Please confirm your password.",
    unit: "unit",
  },
  unitType: {
    WORKER: "Worker",
    SWORDSMAN: "Swordsman",
    SPEARMAN: "Spearman",
    HORSEMAN: "Horseman",
  },
  serverError: {
    notEnoughBeer: "Not enough beer available.",
    onlyOnePerVillage:
      "You can only build one brewery, one farm and one castle per village.",
    tooManyMoves: "The unit has reached its limit of moves for this hour.",
    notYourBuilding: "You can only destroy your own buildings.",
    lastVillage: "You cannot destroy your last village.",
  },
  serverSuccess: {
    beerCollected: "Beer successfully collected.",
    buildingDestroyed: "Building successfully destroyed.",
  },
  registration: {
    title: "Registration",
    error:
      "An error occurred during registration. Maybe the username is already taken.",
    action: "Create account",
    toLogin: "To login",
    passwordsNotEqual: "Passwords do not match.",
    intro:
      "Welcome to the browser game <b><i>Castles of Beer and Dragons</i></b>! Sign up now and gain fame and fortune.",
    cookieInfo:
      "This site does not use cookies and no personal data is stored. When using the game, game events and data are stored on the server and in the browser. If you do not agree with this, do not play.",
  },
  login: {
    title: "Login",
    error: "An error occurred during login. Please check your input.",
    action: "Login",
    toRegistration: "To registration",
  },
  startVillageAction: {
    text: "Please choose a location for your first village.",
    dialog: "Do you want to start here?",
  },
  villageAction: {
    createWorker: "Create worker",
    moveUnit: "Move unit ({0}/{1})",
    villageOf: "Village of {playerName}",
  },
  farmAction: {
    farmOf: "Farm of {playerName}",
  },
  breweryAction: {
    breweryOf: "Brewery of {playerName}",
  },
  unitAction: {
    unitOf: "Unit of {playerName} at {x}, {y}",
    chooseAction: "Choose an action for the unit:",
    moveText: "Where should the unit be moved?",
    movesRemaining: "This unit has {0} of {1} moves per hour remaining.",
    buildFarm: "Build farm",
    buildBrewery: "Build brewery",
    buildCastle: "Build castle",
    buildVillage: "Build village",
  },
  castleAction: {
    castleOf: "Castle of {playerName}",
    createSwordsman: "Create swordsman",
    createSpearman: "Create spearman",
    createHorseman: "Create horseman",
  },
  events: {
    showOnMap: "Show events on map?",
    noEvents: "No events available...",
    openOverlay: "Events",
    buildingDestroyed: "Building destroyed!",
    buildingConquered: "Building conquered!",
    lostUnit: "Unit lost!",
    wonFight: "You won a fight!",
    own: {
      UNIT_MOVED: "You moved a {unitType} to {x}, {y}.",
      UNIT_CREATED: "You created a {unitType} at {x}, {y}.",
      BUILDING_CREATED: "You created a building at {x}, {y}.",
      BEER_COLLECTED: "You collected beer at {x}, {y}.",
      BUILDING_CONQUERED: "You conquered a building at {x}, {y}.",
      BUILDING_DESTROYED: "You destroyed a building at {x}, {y}.",
      LOST_UNIT: "You lost a {unitType} at {x}, {y}.",
    },
    other: {
      UNIT_MOVED: "{playerName} moved a {unitType} to {x}, {y}.",
      UNIT_CREATED: "{playerName} created a {unitType} at {x}, {y}.",
      BUILDING_CREATED: "{playerName} created a building at {x}, {y}.",
      BEER_COLLECTED: "{playerName} collected beer at {x}, {y}.",
      BUILDING_CONQUERED: "{playerName} conquered a building at {x}, {y}.",
      BUILDING_DESTROYED: "{playerName} destroyed a building at {x}, {y}.",
      LOST_UNIT: "{playerName} lost a {unitType} at {x}, {y}.",
    },
  },
  tutorialAction: {
    complete: "Complete tutorial",
    FIRST_WORKER: "Click on a village and create a worker there.",
    FIRST_FARM:
      "Move the worker to a free meadow and build a farm. Ensure there is another meadow next to it for the brewery!",
    FIRST_BREWERY:
      "Create another worker, move him to a free meadow next to a farm and build a brewery.",
    FIRST_BEER_COLLECTED:
      "Tap the beer icon above the brewery and collect your first beer.",
    FIRST_CASTLE: "Build your first castle to be able to create new units.",
    FIRST_UNIT: "Create a unit in the castle to be able to defend yourself.",
  },
};
