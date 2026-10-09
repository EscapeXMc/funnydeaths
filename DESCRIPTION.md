<div align="center">

# 💀 FunnyDeaths

**Funny, configurable death messages — one jar for every server and every version.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.8.9%20%E2%86%92%2026.3-blueviolet?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-8%20%E2%86%92%2025-orange?style=for-the-badge)
![Platforms](https://img.shields.io/badge/Spigot-Paper-Folia-Purpur-Leaves-success?style=for-the-badge)
![Version](https://img.shields.io/badge/version-2.0-informational?style=for-the-badge)
![Author](https://img.shields.io/badge/by-EscapeX-ff69b4?style=for-the-badge)

*Replace boring vanilla death messages with hilarious, fully configurable ones.*

</div>

---

## ✨ Why FunnyDeaths?

Because "Player was blown up by a Creeper" deserves better.

FunnyDeaths intercepts every player death, kills the vanilla message, and fires a random
funny one instead — picked from messages written **for that specific death cause** (fall,
fire, drowning, PvP, void…) or **for the mob that landed the killing blow** (zombie,
skeleton, creeper, ghast…), with `%player%` and `%killer%` placeholders and a little
particle puff at the death spot for flavour.

And unlike most death-message plugins, this is a **true universal build**: the same jar
runs on every Bukkit-family server from **Minecraft 1.8.9 to 26.3+**, on Java 8 through 25.

---

## 🚀 Features

| | |
|---|---|
| 🎯 | **Cause-aware messages** — separate message pools per damage cause (`FALL`, `FIRE`, `DROWNING`, `ENTITY_ATTACK`, …) |
| 🧟 | **Mob-aware messages** — a dedicated pool per killer mob (`ZOMBIE`, `CREEPER`, `GHAST`, `WITHER_SKELETON`, …) |
| 🎲 | **Random picks** with generic fallbacks, so a death is never message-less |
| 🎨 | **Colour codes** — `&` codes, § codes and `&#RRGGBB` hex colours on 1.16+ |
| 🧩 | **Your config survives** — same `config.yml` name, same keys, same messages |
| 🪶 | **Lightweight** — zero dependencies, no config migration, no database |
| 🖥️ | **Admin friendly** — `/fd toggle`, `/fd reload`, one `funnydeath.admin` permission |
| 🌐 | **Network-wide** — optional companion plugin for Velocity / Waterfall broadcasts the message to the whole network |
| 🔮 | **Future proof** — reflective version handling keeps it working on versions that don't exist yet |

---

## 🧱 Supported Platforms

**One jar, all of them:** Bukkit · CraftBukkit · Spigot · **Paper** · **Folia** · **Purpur** ·
**Leaves** · **Pufferfish** · **Arclight** · and any other Spigot/Paper fork.

| Server software | Supported | Notes |
|:---|:---:|:---|
| CraftBukkit / Spigot | ✅ | 1.8.9 → 26.3+ |
| Paper / Paper forks | ✅ | incl. **Folia** (region-safe scheduling), **Purpur**, **Leaves**, **Pufferfish**, **Arclight** |
| Bukkit | ✅ | 1.8.9 → 26.3+ |
| Velocity (proxy) | ✅ | companion jar, clients 1.8.9 → 26.3+ |
| Waterfall / BungeeCord | ✅ | companion jar, clients 1.8.9 → 26.3+ |

> Mojang's **vanilla** server cannot load plugins at all, so no plugin — this one included —
> can customise death messages there. Install Paper, Spigot or Folia instead.

---

## 📥 Installation

1. Download the jar for your loader from `by-loader/`:
   `FunnyDeaths-1.8.9-26.3-Paper.jar`, `…-Folia.jar`, `…-Spigot.jar`, … (all identical,
   pick the name that matches your server).
2. Drop it into your server's `plugins/` folder.
3. Restart. Done — `config.yml` is created automatically with all messages.
4. *(Optional, for proxy networks)* put `FunnyDeaths-1.8.9-26.3-Velocity.jar` in your
   Velocity `plugins/` folder, or `FunnyDeaths-1.8.9-26.3-Waterfall.jar` in Waterfall's.
   Death messages are then broadcast to the whole network instead of one server.

```bash
# typical layout
server/plugins/FunnyDeaths-1.8.9-26.3-Paper.jar
proxy/plugins/FunnyDeaths-1.8.9-26.3-Velocity.jar
```

No dependencies. No setup. Your old `config.yml` is picked up as-is.

---

## 🎮 Commands & Permissions

| Command | Description | Permission |
|:---|:---|:---|
| `/fd toggle` | Toggle the plugin on/off | `funnydeath.admin` |
| `/fd toggle on` \| `off` | Set the state explicitly | `funnydeath.admin` |
| `/fd reload` | Reload `config.yml` without restarting | `funnydeath.admin` |

`funnydeath.admin` defaults to **OP**. Tab completion is included.

---

## ⚙️ Configuration

```yaml
# Plugin settings
enabled: true

# Used when no specific cause or mob matches
generic-messages:
  - "&c%player% &7forgot how to breathe!"
  - "&c%player% &7uninstalled life.exe!"
  - "&#FFA500%player% &7learned about gravity the hard way!"   # hex on 1.16+

# Messages per damage cause
damage-causes:
  FALL:
    - "&c%player% &7discovered gravity... it works!"
  ENTITY_ATTACK:
    - "&c%player% &7was deleted by &e%killer%&7!"

# Messages per killer mob
mob-messages:
  CREEPER:
    - "&c%player% &7hugged a creeper... BOOM!"
  GHAST:
    - "&c%player% &7played cricket with ghast... and lost!"
```

<details>
<summary><b>Placeholders & colour codes</b></summary>

| Placeholder | Replaced with |
|:---|:---|
| `%player%` | the player who died |
| `%killer%` | the player who killed them (omitted lines keep working) |

| Code | Works on |
|:---|:---|
| `&c` `&7` `&l` … | every version |
| `&#RRGGBB` | 1.16 and newer |

</details>

<details>
<summary><b>Old names still work on new servers</b></summary>

Config keys written for an old server keep working on a new one, and vice versa —
`PIG_ZOMBIE`/`ZOMBIFIED_PIGLIN`, `MUSHROOM_COW`/`MOOSHROOM`, `SNOWMAN`/`SNOW_GOLEM`,
`LAVA`/`HOT_FLOOR`, `EXPLOSION`/`BLOCK_EXPLOSION` are resolved automatically.
Unknown keys are logged as a warning instead of breaking your config.

</details>

---

## 🌐 Network-wide death messages (proxy)

The proxy companions use the internal `funnydeaths` plugin-message channel, discovered
automatically — no configuration needed:

```text
Backend server (Paper/Folia/Spigot/…)  ──plugin message──▶  Velocity / Waterfall
                                                                    │
                                                          broadcasts to the whole network
```

If no companion proxy plugin is installed, the backend simply broadcasts locally,
exactly like a standalone server.

---

## ❓ FAQ

<details>
<summary><b>Really the same jar on 1.8.9 and 26.3?</b></summary>

Yes. It is compiled against the 1.8.8 Bukkit API with Java 8 bytecode, and every
version-sensitive API (particles, killer lookup, scheduler, entity and damage-cause
renames) is resolved reflectively at runtime. The build is verified by recompiling the
identical source against the newest API (26.3) in CI-style checks.
</details>

<details>
<summary><b>Folia / Leaves?</b></summary>

Fully supported — the plugin detects regionised servers and switches from the legacy
Bukkit scheduler to the entity/global region scheduler automatically.
</details>

<details>
<summary><b>Will it conflict with another death-message plugin?</b></summary>

Both plugins would try to override the death message; only one should be active at a time.
</details>

---

<div align="center">

**Made with 💀 by EscapeX** · `com.escapex.funnydeaths` · version 2.0

*Death never sounded this funny.*

</div>
