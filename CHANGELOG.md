# Changelog

This document contains a list of notable changes for world-preview.

## [1.1.6] - 2024-01-01

### 🐛 Bug Fixes

-  Fix incorrect access widener issue [[#20]](https://github.com/caeruleusDraconis/world-preview/issues/20)


### 📝 Documentation

-  Added CHANGELOG.md and git-cliff config


## [1.1.5] - 2023-12-17

### ✨ Features

-  Show player and spawn positions [[#16]](https://github.com/caeruleusDraconis/world-preview/issues/16)


### 🐛 Bug Fixes

-  Fix mod compatibility issue with WF's Cave Overhaul

-  Black icons on resize

-  Forge fixup


## [1.1.4] - 2023-12-07

### ✨ Features

-  Compatibility with MC 1.20.3

### 📝 Documentation

-  Add note about compatibility with TerraFirmaCraft


### 🐛 Bug Fixes

-  Clean up when closing ingame Preview [[#15]](https://github.com/caeruleusDraconis/world-preview/issues/15)


### 🔧 Refactor

-  Make use of the improved builtin list


## [1.1.3] - 2023-11-29

### ✨ Features

-  Show an error if there is an issue with setting up the preview


### 🐛 Bug Fixes

-  Fix initialisation order problem with Biolith

-  Second fixup for Biolith


## [1.1.2] - 2023-11-27

### ✨ Features

-  Added option to toggle the world-preview button in pause menu

-  Update to 1.20.2


### 🐛 Bug Fixes

-  Fixed Background renderer in game

-  Compatibility with Biolith based mods

-  LICENSE | Use Apache 2.0 as it was originally intended

-  Prevent NPE when mixins are called before the mod is initialized


## [1.1.0] - 2023-08-17

### ✨ Features

-  Add option to only sample visual y-range

-  Added y-intersections

-  See through the one air layer

-  Working in game preview map

-  Add support for worldtypes

-  **CHANGE:** Moved the reset structure visibility button

### 📝 Documentation

-  Update Readme with the new features

-  Update README to link to modrinth + CurseForge

### 🐛 Bug Fixes

-  Fixed [[#1]](https://github.com/caeruleusDraconis/world-preview/issues/1)

-  Updated y-intersection tooltip

-  NullPointerException in hoveredBiome()

-  Minor improvements to vanilla structure icons

-  Remove debug log entry

-  Fix concurrent access exception during structure generation

-  Always use dirt background for preview settings

-  Seed is no longer editable in game

-  Seed list fixup for ingame preview


### 🔧 Refactor

-  Initial switch to the new biome / structure format discussed in [[#5]](https://github.com/caeruleusDraconis/world-preview/issues/5)

-  Render structure icons / items directly to screen

-  Half Y_BLOCK_STRIDE (16 --> 8)

-  Sample the full Y height in the background

-  Use common tags for cave biomes

-  Split the main PreviewTab


<!-- generated by git-cliff -->
