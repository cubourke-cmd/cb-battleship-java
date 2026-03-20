# Testing Battleship Java Swing GUI

## Build & Run

```bash
# Compile all sources
javac -d out src/battleship/*.java

# Run the game (requires X11 display)
DISPLAY=:0 java -cp /path/to/project/out battleship.Main

# Package as JAR
cd out && jar cfm ../Battleship.jar ../MANIFEST.MF battleship/*.class && cd ..
java -jar Battleship.jar
```

## Environment Notes

- Java 17+ required
- X11 display must be available. On Devin VMs, use `DISPLAY=:0` (not `:1`)
- No external dependencies — pure Java Swing
- No build system (Maven/Gradle) — uses raw `javac`

## Game Flow for Testing

1. **PLACEMENT phase**: Game opens here. Place ships manually (click grid cells) or use "Random Placement" button. Press R to rotate between horizontal/vertical.
2. **PLAYING phase**: Starts after all 5 ships are placed. Click cells on the AI board (right side) to fire. AI responds after ~600ms delay.
3. **GAME_OVER phase**: Triggered when all ships on one side are sunk. "Play Again" button appears.

## Key Test Scenarios

- **Header click guard**: Click on row labels (A-J) and column labels (1-10) — should NOT trigger any cell action. Headers occupy the first 24px (HEADER_SIZE) of the board panel.
- **Ship placement**: Verify manual placement with hover preview (green = valid, red = invalid). Verify random placement fills all 5 ships.
- **Hit/miss/sunk visuals**: Hits show skull (☠) on red, misses show dot (•) on dark blue, sunk ships show fire (🔥) on dark red.
- **Ship status panels**: Shows green dots for alive ships, red dots + strikethrough text for sunk ships.
- **AI timer**: After firing a shot, if you click "Play Again" within 600ms, verify no phantom AI shot occurs in the new game.
- **Play Again reset**: Both boards clear, phase returns to PLACEMENT, ship status resets, AI board hides.

## Known Cosmetic Issues

- Cursor may stay as hand pointer across phases because `setCursor` is called inside `paintComponent`. This is cosmetic only and doesn't affect gameplay.

## Devin Secrets Needed

None — this is a pure Java desktop app with no authentication or external services.
