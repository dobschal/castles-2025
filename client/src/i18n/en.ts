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
    toGame: "Show game",
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
      "You can only build two breweries, one farm and one castle per village.",
    tooManyMoves: "The unit has reached its limit of moves for this hour.",
    notYourBuilding: "You can only destroy your own buildings.",
    lastVillage: "You cannot destroy your last village.",
    noFarm: "No farm nearby!",
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
    aboutTheGame: "More about the game",
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
    breweryOf:
      "Brewery of {playerName} that produces {beer} beer per hour and stores maximal {breweryBeerStorage} beer. {beerToCollect} beer can be collected.",
    noFarmNextTo: "No farm next to this brewery! No beer is produced.",
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
      GAME_OVER: "You lost your last village.",
    },
    other: {
      UNIT_MOVED: "{playerName} moved a {unitType} to {x}, {y}.",
      UNIT_CREATED: "{playerName} created a {unitType} at {x}, {y}.",
      BUILDING_CREATED: "{playerName} created a building at {x}, {y}.",
      BEER_COLLECTED: "{playerName} collected beer at {x}, {y}.",
      BUILDING_CONQUERED: "{playerName} conquered a building at {x}, {y}.",
      BUILDING_DESTROYED: "{playerName} destroyed a building at {x}, {y}.",
      LOST_UNIT: "{playerName} lost a {unitType} at {x}, {y}.",
      GAME_OVER: "{playerName} lost their last village.",
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
  wiki: {
    link: "Rules & Infos",
    description:
      "In Castles, you build your own empire of castles and defend it\n" +
      "      against other players and barbarians. Here you will find all the important information about\n" +
      "      the game and the rules.",
    title1: "The Map",
    paragraph1:
      "The map consists of four different types of fields: forest, meadow,\n" +
      "      water, and mountain. Buildings can only be placed on meadows. Units\n" +
      "      cannot walk over water. The map is almost endlessly large and\n" +
      "      updates in real-time during the game.",
    title2: "Castles, Villages, Breweries ...",
    paragraph2:
      "There are various buildings in the game that have different functions.\n" +
      "      Farm and brewery produce beer, which is needed for building units\n" +
      "      and buildings. Farm and brewery must be directly next to each other.\n" +
      "      In castles, you can create units for defense and attack.\n" +
      "      Villages can train workers and store your beer. The more villages you have,\n" +
      "      the more beer you can store.\n" +
      "      In case of a conquest, farm and brewery are destroyed. Villages and\n" +
      "      castles are taken over.",
    title3: "Units and Battles",
    paragraph3:
      "In castles and villages, you can build units. This costs you beer.\n" +
      "      Moving units also costs beer. There can only be one unit\n" +
      "      on a field at a time. If enemy units are on a field, a\n" +
      '      battle occurs. Battles work according to the "rock, paper, scissors" principle.\n' +
      "      Horseman wins against swordsman, swordsman wins against\n" +
      "      spearman, and spearman wins against horseman. The winner stays on\n" +
      "      the field, the loser is destroyed. Workers cannot attack and\n" +
      "      are destroyed by combat units.",
    title4: "Barbarians",
    paragraph4:
      "Barbarians appear randomly on the map. They attack you and\n" +
      "      other players. They destroy buildings and kill units. You can\n" +
      "      defeat barbarians by moving units onto them. The movements of\n" +
      "      barbarians are random. The number of barbarians depends on the\n" +
      "      number of players.",
    title5: "Dragons, Cities, and More",
    paragraph5:
      "I am constantly developing Castles and look forward to your\n" +
      "      support! Dragons, cities, and much more are planned. Stay\n" +
      "      tuned and feel free to write to me if you have ideas or suggestions.\n" +
      "      Thank you for testing and playing! 🍻",
    title6: "Costs",
    paragraph6:
      "The costs for buildings and units are calculated based on already created buildings and units." +
      "For example, if you already own a brewery, the next brewery will cost more beer. " +
      "The same applies to units and castles. Example: You own 3 units (type does not matter) and " +
      "the base price for a horseman is 100 beer. The next horseman would cost 100 x 2 x 2 x 2 = 800 beer.",
  },
  users: {
    link: "Players",
    description:
      "Players sorted by points. Per building, 2 points is awarded. Per unit, 1 point is awarded.",
  },
  userProfile: {
    link: "Profile",
    selectAvatar: "Select an avatar image:",
    showOnMap: "Show on map",
  },
  destroyBuilding: {
    button: "Destroy building",
    question: "Do you really want to destroy this building?",
  },
};
