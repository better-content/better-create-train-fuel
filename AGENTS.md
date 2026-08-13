# AGENTS.md

## Scope

This repository contains the Better Content-owned Forge mod **Create Train Fuel Scaling**.

- Canonical mod ID: `create_train_fuel_scaling`
- Canonical artifact: `create-train-fuel-scaling-<version>.jar`
- Maven group: `com.bettercontent`
- Java runtime: 17
- Minecraft/Forge baseline: 1.20.1 / 47.4.13

## Commit discipline

Commit after each coherent completed change. Run the documented validation before committing and push the current branch; do not leave completed work uncommitted or unpushed unless the user explicitly asks.

## Validation

Run `./gradlew verifyFast` for the deterministic CI-equivalent lane. Use `./gradlew verifyFull` when a change affects Forge runtime or GameTest behavior and the task defines that lane.

Do not commit build outputs, runtime worlds, logs, IDE state, downloaded dependency JARs, or generated caches.
