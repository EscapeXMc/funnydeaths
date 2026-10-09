<div align="center">

# 💀 FunnyDeaths

**Funny, configurable death messages — one jar for every server and every version.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.8.9%20%E2%86%92%2026.3-blueviolet?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-8%20%E2%86%92%2025-orange?style=for-the-badge)
![Platforms](https://img.shields.io/badge/Spigot-Paper-Folia-Purpur-Leaves-success?style=for-the-badge)
![Version](https://img.shields.io/badge/version-2.0-informational?style=for-the-badge)
![Author](https://img.shields.io/badge/by-EscapeX-ff69b4?style=for-the-badge)
![License](https://img.shields.io/badge/license-Ask%20owner%20first-red?style=for-the-badge)

*Death never sounded this funny.*

</div>

---

## 📖 What is this?

FunnyDeaths replaces the boring vanilla death message with a random, fully configurable
funny line from `config.yml`, chosen for the **damage cause** (fall, fire, drowning, PvP…)
or the **killer mob** (zombie, creeper, ghast…), with a small particle puff at the death
spot and `%player%` / `%killer%` placeholders.

It is a universal rebuild of the original `FunnyDeath` plugin, rebranded as
`com.escapex.funnydeaths`.

**One jar runs everywhere:** Bukkit, CraftBukkit, Spigot, Paper, Folia, Purpur, Leaves,
Pufferfish, Arclight and their forks — Minecraft **1.8.9 → 26.3+** on Java 8 → 25.

## ✨ Highlights

| | |
|---|---|
| 🎯 | Message pools per damage cause **and** per killer mob, with generic fallbacks |
| 🎨 | `&` codes, `§` codes and `&#RRGGBB` hex colours (1.16+) |
| 🧩 | Existing `config.yml` files keep working — same keys, same messages |
| 🪶 | Zero dependencies, zero config migration |
| 🌐 | Optional Velocity / Waterfall companion for **network-wide** messages |
| 🔭 | Reflective version handling → also works on versions released after 26.3 |
| 💻 | `/fd toggle` / `/fd reload`, permission `funnydeath.admin` (default OP) |

## 🧱 Platform support

| Server | Status | Notes |
|:---|:---:|:---|
| CraftBukkit / Spigot | ✅ | 1.8.9 → 26.3+ |
| Paper + forks | ✅ | incl. **Folia**, **Purpur**, **Leaves**, **Pufferfish**, **Arclight** |
| Bukkit | ✅ | 1.8.9 → 26.3+ |
| Velocity (proxy) | ✅ | companion jar, clients 1.8.9 → 26.3+ |
| Waterfall / BungeeCord | ✅ | companion jar, clients 1.8.9 → 26.3+ |

> Mojang **vanilla** cannot load plugins at all, so no plugin can change death messages
> there — install Paper, Spigot or Folia instead.

## 📥 Install

1. Grab the jar named for your loader: `FunnyDeaths-1.8.9-26.3-Paper.jar`,
   `…-Folia.jar`, `…-Spigot.jar`, … (all identical builds, pick the matching name).
2. Drop it into `plugins/` and restart.
3. *(Optional)* proxy networks: add `FunnyDeaths-1.8.9-26.3-Velocity.jar` or
   `FunnyDeaths-1.8.9-26.3-Waterfall.jar` to your proxy's `plugins/` folder.

## 🛠️ Building from source

Requires **JDK 21+** on the PATH (`javac` / `jar`). Everything else is fetched
automatically on the first build — no Maven, no Gradle, no manual setup.

```bash
git clone https://github.com/EscapeXMc/funnydeaths.git
cd funnydeaths
./build.sh            # -> out/*.jar and out/by-loader/*.jar
```

`build.sh` downloads the compile-time API jars (Spigot 1.8.8, Velocity 3.4.0 + Adventure,
Waterfall 26.1) into `lib/`, then compiles three artifacts:

| Output | For |
|:---|:---|
| `FunnyDeaths-Universal-Bukkit-2.0.jar` | all Bukkit-family servers |
| `FunnyDeaths-Velocity-2.0.jar` | Velocity proxies |
| `FunnyDeaths-Waterfall-2.0.jar` | Waterfall / BungeeCord proxies |

`out/by-loader/` additionally contains one copy per loader, named
`FunnyDeaths-1.8.9-26.3-<Loader>.jar`.

### Why it works on every version

* Compiled against the **1.8.8 Bukkit API with Java 8 bytecode** → loads on Java 8 through 25.
* Verified by recompiling the **identical source against the 26.3 API** (must stay warning-free).
* Version-sensitive APIs are resolved **reflectively** at runtime:
  * `Particle` (absent on 1.8 → effect skipped), `Player#getKiller()` (absent before 1.9),
  * legacy scheduler vs. **Folia** entity / global-region schedulers,
  * entity and damage-cause renames (`PIG_ZOMBIE`/`ZOMBIFIED_PIGLIN`, `LAVA`/`HOT_FLOOR`, …).

## 🎮 Commands

| Command | Description |
|:---|:---|
| `/fd toggle [on\|off]` | toggle the plugin |
| `/fd reload` | reload `config.yml` |

## 📁 Repository layout

```
src/bukkit/        universal plugin source + plugin.yml / config.yml
src/velocity/      Velocity companion source + velocity-plugin.json
src/waterfall/     Waterfall companion source + plugin.yml
build.sh           builds all three jars (auto-downloads API jars)
lib/               downloaded compile-time dependencies (git-ignored)
out/               build output (git-ignored)
```

## ⚖️ License

**Proprietary — ask the owner first.** See [`LICENSE.txt`](LICENSE.txt).

Using, copying, modifying or redistributing this project without prior permission from
the owner (**EscapeX**) is prohibited and may be dangerous — it is done entirely at your
own risk, with no warranty and no support. This repository is public for viewing and
permission requests only.

<details>
<summary><b>Upload / listing text</b></summary>

Ready-made text for plugin hosting sites lives in
[`DESCRIPTION.md`](DESCRIPTION.md) (full description) and
[`DESCRIPTION-SUMMARY.md`](DESCRIPTION-SUMMARY.md) (1–2 paragraph summary).

</details>

<div align="center">

*Made with 💀 by EscapeX · `com.escapex.funnydeaths` · version 2.0*

</div>
