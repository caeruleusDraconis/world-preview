# World Preview datapack format

Similarly to Minecraft itself, *World Preview* can also be extended via datapacks.

This is done using the built-in Minecraft `ReloadListener`, allowing data from 3rd party datapacks and mods to be recognized.

The [resources provided by the mod for vanilla Minecraft](/src/main/resources/data/world_preview) can be used as reference for the creation of a custom datapack.


## Biomes

Colors for modded biomes can be defined in the `biome_preview` namespace.

All JSON files are recursively scanned in this namespace.
Contrary to most Minecraft data resources, the filenames / resource keys themselves carry no meaning.
Instead, each JSON file contains an arbitrarily large number of data entries that map biome keys to color definitions.

The format of these data entries is as follows:

```json
{

    "<mod namespace>:<biome1>": {
        "color": 12345,                // Decimal RGB color value (See explanation below)
        "cave": true,                  // Whether this is a cave biome or not
        "name": "<Your Display Name>"  // Custom display name
    },
    "<mod namespace>:<biome2>": {
        "color": 54321
    },
    // ...
}
```

Only the `color` entry is required. All other entries are optional.
The default value for `cave` is `false`.

Please note that the color value is in decimal representation, since JSON does not support hex formatted integers.
This means that `0 (0x000000)` corresponds to black and a value of `16777215 (0xFFFFFF)` corresponds to white.

This is why it is highly recommended to use the built-in color picker in `Settings -> Biomes` for the initial configuration.
The resulting configuration file (located in `.minecraft/config/world_preview/biome-colors.json`) could be directly repackaged as a data pack.






## Structures

Structure names and icons for modded structures can be defined in the `structure_preview` namespace.

Similar to the biome definitions, the filenames / resource keys themselves carry no meaning and each file holds an arbitrarily large number of data entries that map biome keys to structure definitions instead.

The format of these data entries is as follows:

```json
{
  "<mod namespace>:<structure 1>": {
    "icon": "world_preview:textures/structure1.png",  // A valid texture resource location
    "name": "Custom structure",                       // The display name
    "showByDefault": true                             // Whether to hide or show this structure by default
  },
  "<mod namespace>:<structure 2>": {
    "icon": "world_preview:textures/structure2.png",
    "name": "Extremely Common structure",
    "showByDefault": false
  },
  // ...
}
```

In this case, *all* fields are required.

As a rule of thumb, if a structure is extremely common, as is the case with `minecraft:mineshaft` or `minecraft:nether_fossil`, hiding them by default is advised.

**NOTE:** The recommended size for the structure icons is `16x16` pixels.


## Heightmap colors

New colormaps for the heightmap can be provided in the `colormap_preview` namespace.

Each colormap must have its own file with the following format:

```json
{
  "name": "<Your Display Name>",      // The display name

  // The definition of the colormap:
  //   - The `data` array MUST at least have 2 entries
  //   - Each entry in the `data` array MUST itself be an array of size 3
  //   - Each element in an entry MUST be a value from 0.0 to 1.0
  //   -
  "data": [
    [0.0, 0.0, 1.0], // Red, Green and Blue values (0% = 0.0 to 100% = 1.0)
    [0.0, 1.0, 1.0],
    [0.0, 1.0, 0.0],
    [1.0, 1.0, 0.0],
    [1.0, 0.0, 0.0]
  ]
}
```


## Heightmap presets

Additional heightmap presets can be provided via the `heightmap_preview_presets` namespace.
The main use-case for heightmap presets is to provide a quick way for the user to select the recommended visual heightmap range for a given dimension.

This range associates y-values on the heightmap with colors on the colormap:
- The lower value, called `minY`, associates this y-value with the first entry in the color array (the left-most part on a colormap when visualized in the mod). 
Any y-values below `minY` will be colored the same as `minY`.
- The higher value, called `maxY`, does the same for the last entry in the color array (the right-most part of the colormap when visualized in the mod). 
Any y-values above `maxY` will be colored the same as `maxY`.

Each heightmap preset must have its own file with the following format:

```json
{
  "name": "<Your display name>",  // The display name in the World Preview UI
  "minY": -16,                    // Minimum y-value for the visual heightmap range
  "maxY": 64                      // Maximum y-value for the visual heightmap range
}
```
