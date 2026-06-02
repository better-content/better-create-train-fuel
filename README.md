# Create Train Fuel Scaling

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

## Config

Forge writes `create_train_fuel_scaling-common.toml`.

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
/create_train_fuel_scaling formula <speedBps>
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
./gradlew test
./gradlew runGameTestServer
./gradlew build
```

Manual smoke:

1. Install in a Forge 1.20.1 Create 6.0.8 instance.
2. Start a server and confirm `config/create_train_fuel_scaling-common.toml` is generated.
3. Run `/create_train_fuel_scaling formula 40`.
4. Fuel a powered train and compare long slow movement against high-speed movement.
