# Astra Tech — Fabric 1.20.1

Space-future-tech mod: a Moon dimension, end-game Astral gear, six mobs, a summonable boss (Nova Overmind), and an in-game Codex with a Showcase tab.

## Requirements
Minecraft 1.20.1, Fabric Loader 0.15.11+, Fabric API 0.92.2+1.20.1, Java 17.

## Build
`./gradlew build` (JDK 17) → jar in `build/libs/`. Or push to GitHub: the included workflow (`.github/workflows/build.yml`) builds the jar as a downloadable artifact.

## Play
1. Spawn: you receive the **Astra Codex** (also `/astratech codex`; craft: book + redstone).
2. Craft 10+ **Gateway Frame** blocks, build a Nether-style rectangular frame (min 2x3 interior), light it with the **Ion Igniter**.
3. On the Moon mine Astrium and Plasma Crystal ore; craft Astral armour, tools, Plasma Blade, Pulse Blaster.
4. Craft a **Quantum Core** + **Summoning Terminal**; right-click the terminal with the core on the Moon to summon the boss.
5. Showcase tab (creative / op level 2 / singleplayer) spawns any item, mob, or warps you.

Jetpack (Astral Chestplate): hold jump in air; sneak+jump hovers. Dedicated servers need `allow-flight=true`.

## What is in 1.1
- Plasma Blade is larger in hand; right-click fires the **Photon Lance**, a piercing laser beam.
- Pulse Blaster fires faster bolts and holds level, barrel forward.
- Structures: Outposts, Helios Stations, Lumen Spires (tree-shaped tech towers), Landed Spaceships, Radar Arrays, and Overmind Sanctums (a ready-made Summoning Terminal plus a Quantum Core chest).
- **Companion Remote** (craft: ACA / RNR / AAA with A=Astrium Ingot, C=Plasma Crystal, R=Redstone, N=Nova Core): deploys your own Astro Bot that follows and fights for you.
- Jetpack: hold **R** (rebindable under Controls > Astra Tech) to boost forward where you look.
- HD (64x64) tool and armour icons; 128x64 armour textures.

## Status
Written without a compiler available; not built or run. If the build reports errors, paste the log and they can be fixed quickly.
