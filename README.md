# Battleship Java

A classic Battleship game built with Java Swing GUI.

## Features

- Classic 10x10 grid with 5 ships (Carrier, Battleship, Cruiser, Submarine, Destroyer)
- Player vs AI with smart hunt/target AI logic
- Ship placement: click to place manually or use "Random Placement"; press R to rotate
- Visual feedback: skull and crossbones for hits, dots for misses, fire for sunk ships
- Ship status panels for both sides
- Win/loss detection with "Play Again" button

## Requirements

- Java 17+

## Building

```bash
mkdir -p out
javac -d out src/battleship/*.java
```

## Running

```bash
java -cp out battleship.Main
```

## Running from JAR

```bash
java -jar Battleship.jar
```

## How to Play

1. **Place Ships**: Click on the grid to place each ship. Press **R** to rotate between horizontal and vertical. Or click **Random Placement** for auto-placement.
2. **Fire**: Click on the enemy grid to fire at a cell. Hits show as skull and crossbones, misses show as dots.
3. **Win**: Sink all 5 enemy ships to win!
