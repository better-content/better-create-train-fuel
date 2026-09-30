# Better Create Train Fuel

Standalone Forge 1.20.1 addon for Create 6.0.8 that replaces Create's flat powered-train fuel drain with speed-scaled fuel drain.

## Behavior

Vanilla Create drains one `fuelTicks` value per train tick while powered fuel is available. This mod keeps Create's `fuelTicks`, fuel inventory scan order, item extraction, container remainder handling, and movement logic, but changes the drain amount per tick.

The default formula is:

```text
cost = clamp(baseCost * exp(k * (speedBps / poweredTrainTopSpeedBps - 1)), minimumCost, maximumCost)
```

At the configured powered train top speed, the ratio is `1.0`, so the default cost is exactly `baseCost = 1.0`. That preserves Create's vanilla drain rate at normal powered maximum speed. Slower trains consume less fuel; faster trains consume exponentially more.

## Default Cost Table

Using Create's default powered top speed of `40 bps`, `baseCost = 1.0`, and `k = 2.0`:

| Speed | Ratio | Fuel ticks per tick |
| --- | ---: | ---: |
| 0 bps | 0.00 | 0.0000 when stopped drain is disabled |
| 10 bps | 0.25 | 0.2231 |
| 20 bps | 0.50 | 0.3679 |
| 40 bps | 1.00 | 1.0000 |
| 60 bps | 1.50 | 2.7183 |
| 80 bps | 2.00 | 7.3891 |
| 120 bps | 3.00 | 16.0000 (default cap) |

The current curve is intentionally unchanged: normal powered travel consumes 1.0 fuel tick per
tick, 80 bps consumes 7.3891, and the configured 16.0 maximum still permits 120 bps travel at a
substantially higher cost.

Create invokes `Train.burnFuel()` from its shared navigation path after its native braking and
station-distance checks. The mixin only changes fuel accounting there, so manual driving,
schedules, reverse travel, station approaches, coasting, braking, refueling, and recovery keep
Create's native movement decisions. Fuel still controls Create's powered max speed and acceleration;
when fuel is exhausted, Create falls back to its unpowered movement values until the next successful
native-order refuel.

## Config

Forge writes `better_create_train_fuel-common.toml`.

```toml
enabled = true
baseCost = 1.0
k = 2.0
minimumCost = 0.0
maximumCost = 16.0
consumeWhileStopped = false
minimumMovingSpeedBps = 0.05
resetDebtWhenFuelEmpty = true
debugLogging = false
```

Values are validated as non-negative, and `maximumCost` must be greater than or equal to `minimumCost`.

## Command

```text
/better_create_train_fuel formula <speedBps>
```

Prints the current Create powered train top speed, ratio, and computed cost using the live config.

## Compatibility

Built for Forge 1.20.1, Java 17, and Create 6.0.8. The mixin targets `com.simibubi.create.content.trains.entity.Train#burnFuel`, plus Create 6.0.8 train NBT read/write hooks for fractional fuel debt persistence.

Known limitations:

- Full automated rail-network train assembly GameTests are intentionally out of scope for this first version.
- If Create changes the `Train` read/write signatures, the NBT debt mixin must be updated with the Create upgrade.
- The mod does not change train speed, pathfinding, schedules, stations, signals, navigation, or Create config values.

## Tests

```sh
./gradlew verifyFast
./gradlew verifyFull
```

`verifyFast` runs the JVM lane. `verifyFull` adds the headless Forge GameTest pass.

Manual smoke:

1. Install in a Forge 1.20.1 Create 6.0.8 instance.
2. Start a server and confirm `config/better_create_train_fuel-common.toml` is generated.
3. Run `/better_create_train_fuel formula 40`.
4. Fuel a powered train and compare long slow movement against high-speed movement.

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Canonical identity

- Repository and Gradle project: `better-create-train-fuel`
- Mod ID and resource namespace: `better_create_train_fuel`
- Maven group: `com.bettercontent`
- Runtime artifact: `build/libs/better-create-train-fuel-<version>.jar`

The canonical identity is a clean break. Legacy mod IDs, resource namespaces, configuration paths, commands, network channels, and saved-data keys are not migrated or aliased.
