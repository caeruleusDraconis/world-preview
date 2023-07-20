#!/bin/bash

FILES=(
  'c|build.gradle'
  'c|gradle.properties'
  'c|settings.gradle'
  'c|src/main/java/caeruleusTait/world/preview/WorldPreview.java'
  'c|src/main/java/caeruleusTait/world/preview/client/WorldPreviewClient.java'
  'o|src/main/resources/META-INF/mods.toml'
  'o|src/main/resources/fabric.mod.json'
  'o|src/main/resources/pack.mcmeta'
)


cd "$(dirname "$(readlink -fn "$0")")"

cat << EOF

       WorldGen Mod loader switcher
       ============================

  Please select the desired Minecraft Mod loader:

    1: Fabric
    2: Forge

EOF

read -p 'Please enter the number of the desired mod loader: ' LOADER
read -p 'Use symlinks? [Y/n]: ' LINK

case $LOADER in
  1)
    echo -n "Switching to Fabric"
    ENDING='fabric'
    ;;
  2)
    echo -n "Switching to Forge"
    ENDING='forge'
    ;;
  *)
    echo "Invalid selection: $LOADER"
    exit 1
esac

[ -z "$LINK" ] && LINK=y

case $LINK in
  Y|y|Yes|YES|yes)
    echo " by linking"
    CP='ln -s'
    ;;
  N|n|No|NO|no)
    echo " by copying"
    CP=cp
    ;;
  *)
    echo "Unrecognized input: $LINK"
    exit 1
esac

for f in "${FILES[@]}"; do
  flags="${f//|*/}"
  f="${f//*|/}"
  rm -f "$f"
  dest="$PWD/$f.$ENDING"
  [[ ! -e "$dest" && "$flags" == *"o"* ]] && continue
  [[ ! -e "$dest" && "$flags" == *"c"* ]] && touch "$dest"
  $CP "$dest" "$f"
done
