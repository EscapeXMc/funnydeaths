#!/usr/bin/env bash
# Builds the three FunnyDeaths artifacts:
#   1. FunnyDeaths-Universal-Bukkit-2.0.jar  -> Bukkit/CraftBukkit/Spigot/Paper/Folia/Purpur/Leaves/Pufferfish, MC 1.8.9 - 26.3+
#   2. FunnyDeaths-Velocity-2.0.jar         -> Velocity, clients 1.8.9 - 26.3+
#   3. FunnyDeaths-Waterfall-2.0.jar        -> Waterfall/BungeeCord, clients 1.8.9 - 26.3+
set -euo pipefail
cd "$(dirname "$0")"

JAVAC="${JAVAC:-javac}"
JAR="${JAR:-jar}"
VERSION="2.0"
RM="${RM:-rm}"

CP_BUKKIT="lib/spigot-api-1.8.8.jar"
CP_VELOCITY="lib/velocity-api-3.4.0.jar:lib/adventure-api-4.26.1.jar:lib/adventure-key-4.26.1.jar:lib/adventure-text-serializer-legacy-4.26.1.jar:lib/examination-api-1.3.0.jar:lib/javax.inject-1.jar"
CP_WATERFALL="lib/waterfall-api-26.1.jar:lib/waterfall-chat-26.1.jar:lib/waterfall-event-26.1.jar"

REPO_PAPER="https://repo.papermc.io/repository/maven-public"
REPO_CENTRAL="https://repo1.maven.org/maven2"

fetch() { # url target
    if [ -f "$2" ]; then
        return 0
    fi
    echo "   downloading $(basename "$2")"
    curl -sSL --max-time 300 -o "$2" "$1" || { echo "   FAILED: $1" >&2; exit 1; }
    case "$2" in
        *.jar) unzip -t "$2" >/dev/null 2>&1 || { echo "   corrupt download: $2" >&2; $RM -f "$2"; exit 1; } ;;
    esac
}

# compile-time API jars (downloaded once, then cached in lib/)
fetch "$REPO_PAPER/org/spigotmc/spigot-api/1.8.8-R0.1-SNAPSHOT/spigot-api-1.8.8-R0.1-20160221.082514-43.jar" lib/spigot-api-1.8.8.jar
fetch "$REPO_PAPER/com/velocitypowered/velocity-api/3.4.0/velocity-api-3.4.0.jar" lib/velocity-api-3.4.0.jar
fetch "$REPO_CENTRAL/net/kyori/adventure-api/4.26.1/adventure-api-4.26.1.jar" lib/adventure-api-4.26.1.jar
fetch "$REPO_CENTRAL/net/kyori/adventure-key/4.26.1/adventure-key-4.26.1.jar" lib/adventure-key-4.26.1.jar
fetch "$REPO_CENTRAL/net/kyori/adventure-text-serializer-legacy/4.26.1/adventure-text-serializer-legacy-4.26.1.jar" lib/adventure-text-serializer-legacy-4.26.1.jar
fetch "$REPO_CENTRAL/net/kyori/examination-api/1.3.0/examination-api-1.3.0.jar" lib/examination-api-1.3.0.jar
fetch "$REPO_CENTRAL/javax/inject/javax.inject/1/javax.inject-1.jar" lib/javax.inject-1.jar
fetch "$REPO_PAPER/io/github/waterfallmc/waterfall-api/26.1-R0.1-SNAPSHOT/waterfall-api-26.1-R0.1-20260616.153316-6.jar" lib/waterfall-api-26.1.jar
fetch "$REPO_PAPER/io/github/waterfallmc/waterfall-chat/26.1-R0.1-SNAPSHOT/waterfall-chat-26.1-R0.1-20260616.153316-6.jar" lib/waterfall-chat-26.1.jar
fetch "$REPO_PAPER/io/github/waterfallmc/waterfall-event/26.1-R0.1-SNAPSHOT/waterfall-event-26.1-R0.1-20260616.153316-6.jar" lib/waterfall-event-26.1.jar

echo "==> dependencies ready"

$RM -rf build out
mkdir -p build/bukkit build/velocity build/waterfall out

manifest() {
    local title="$1"
    cat > "build/$title.mf" <<EOF
Manifest-Version: 1.0
Implementation-Title: FunnyDeaths
Implementation-Version: $VERSION
Implementation-Vendor: EscapeX
Built-Jdk-Spec: 8
EOF
}

echo "==> [1/3] compiling universal Bukkit plugin (targets Java 8 / MC 1.8.9 API)"
$JAVAC --release 8 -encoding UTF-8 -nowarn -cp "$CP_BUKKIT" -d build/bukkit $(find src/bukkit/java -name '*.java')
cp -r src/bukkit/resources/. build/bukkit/
manifest bukkit
$JAR cfm "out/FunnyDeaths-Universal-Bukkit-$VERSION.jar" build/bukkit.mf -C build/bukkit .

echo "==> [2/3] compiling Velocity companion plugin"
$JAVAC --release 8 -encoding UTF-8 -nowarn -cp "$CP_VELOCITY" -d build/velocity $(find src/velocity/java -name '*.java')
cp -r src/velocity/resources/. build/velocity/
manifest velocity
$JAR cfm "out/FunnyDeaths-Velocity-$VERSION.jar" build/velocity.mf -C build/velocity .

echo "==> [3/3] compiling Waterfall companion plugin"
$JAVAC --release 8 -encoding UTF-8 -nowarn -cp "$CP_WATERFALL" -d build/waterfall $(find src/waterfall/java -name '*.java')
cp -r src/waterfall/resources/. build/waterfall/
manifest waterfall
$JAR cfm "out/FunnyDeaths-Waterfall-$VERSION.jar" build/waterfall.mf -C build/waterfall .

echo "==> build finished"
ls -la out/

echo "==> emitting per-loader copies (identical content, one file per platform)"
BUKKIT_JAR="out/FunnyDeaths-Universal-Bukkit-$VERSION.jar"
VELOCITY_JAR="out/FunnyDeaths-Velocity-$VERSION.jar"
WATERFALL_JAR="out/FunnyDeaths-Waterfall-$VERSION.jar"
LOADERS_BUKKIT="Bukkit CraftBukkit Spigot Paper Folia Purpur Leaves Pufferfish Arclight"
mkdir -p out/by-loader
for LOADER in $LOADERS_BUKKIT; do
    cp "$BUKKIT_JAR" "out/by-loader/FunnyDeaths-1.8.9-26.3-$LOADER.jar"
done
cp "$VELOCITY_JAR" "out/by-loader/FunnyDeaths-1.8.9-26.3-Velocity.jar"
cp "$WATERFALL_JAR" "out/by-loader/FunnyDeaths-1.8.9-26.3-Waterfall.jar"
cp "$WATERFALL_JAR" "out/by-loader/FunnyDeaths-1.8.9-26.3-BungeeCord.jar"
echo "==> per-loader files:"
ls -la out/by-loader/
