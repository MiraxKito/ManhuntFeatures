# Changelog

## 1.3.0 — Multi-version builds

Added:

- Separate Java 8 / Spigot API 1.16.1 build.
- Separate Java 17 / Spigot API 1.20.1 build.
- Version-specific `plugin.yml` metadata.
- GitHub Actions builds both JAR files.
- Java 8-compatible common source code.


## 1.2.0 — Tab roles and release packaging

Added:

- Hunter and runner labels in the player list (tab), without a scoreboard.
- Configurable tab prefixes.
- Plugin version and API information in `/mhfeatures status`.
- README documentation in English and Russian.
- MIT license.
- Release changelog.
- GitHub Actions build workflow.

## 1.1.0 — Named compass and staged reset

Added:

- Named Manhunt Compass with an internal item marker.
- Ordinary compasses no longer trigger tracking.
- Automatic compass restoration for hunters.
- Compass replacement when the hunter inventory is full.
- Safe staged world reset through a server restart.

Fixed:

- Synchronous world generation no longer runs during active gameplay.
- Reset no longer causes a Spigot watchdog shutdown during world creation.

## 1.0.0 — Initial release

- Hunter and runner roles.
- Nearest runner tracking.
- Separate Manhunt worlds.
- Player teleport commands.
- World reset foundation.
