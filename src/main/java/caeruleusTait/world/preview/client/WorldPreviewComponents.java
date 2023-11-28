package caeruleusTait.world.preview.client;

import net.minecraft.network.chat.Component;

public class WorldPreviewComponents {
    // Main view components
    public static final Component TITLE = Component.translatable("world_preview.preview.title");
    public static final Component TITLE_FULL = Component.translatable("world_preview.preview.title-full");
    public static final Component SEED_FIELD = Component.translatable("world_preview.preview.seed-field");
    public static final Component SEED_LABEL = Component.translatable("world_preview.preview.seed-label");
    public static final Component BTN_RANDOM = Component.translatable("world_preview.preview.btn-random");
    public static final Component BTN_SAVE_SEED = Component.translatable("world_preview.preview.btn-save-seed");
    public static final Component BTN_SETTINGS = Component.translatable("world_preview.preview.btn-settings");
    public static final Component BTN_CAVES = Component.translatable("world_preview.preview.btn-caves");
    public static final Component BTN_HOME = Component.translatable("world_preview.preview.btn-home");
    public static final Component BTN_SWITCH_STRUCT_DISABLED = Component.translatable("world_preview.preview.btn-cycle.structures.disabled.tooltip");
    public static final Component BTN_TOGGLE_STRUCTURES = Component.translatable("world_preview.preview.btn-toggle-structures");
    public static final Component BTN_TOGGLE_STRUCTURES_DISABLED = Component.translatable("world_preview.preview.btn-toggle-structures.disabled");
    public static final Component BTN_RESET_STRUCTURES = Component.translatable("world_preview.preview.btn-reset-structures");
    public static final Component BTN_RESET_STRUCTURES_TOOLTIP = Component.translatable("world_preview.preview.btn-reset-structures.tooltip");
    public static final Component BTN_TOGGLE_HEIGHTMAP = Component.translatable("world_preview.preview.btn-toggle-heightmap");
    public static final Component BTN_TOGGLE_HEIGHTMAP_DISABLED = Component.translatable("world_preview.preview.btn-toggle-heightmap.disabled");
    public static final Component BTN_TOGGLE_INTERSECT = Component.translatable("world_preview.preview.btn-toggle-intersect");
    public static final Component BTN_TOGGLE_INTERSECT_DISABLED = Component.translatable("world_preview.preview.btn-toggle-intersect.disabled");

    // Error message on setup
    public static final Component MSG_ERROR_SETUP_FAILED = Component.translatable("world_preview.preview.error.setup-failed");

    // Settings
    public static final Component SETTINGS_TITLE = Component.translatable("world_preview.settings.title");

    // - General settings
    public static final Component SETTINGS_GENERAL_TITLE = Component.translatable("world_preview.settings.general.title");
    public static final Component SETTINGS_GENERAL_HEAD = Component.translatable("world_preview.settings.general.head");
    public static final Component SETTINGS_GENERAL_THREADS = Component.translatable("world_preview.settings.general.threads");
    public static final Component SETTINGS_GENERAL_THREADS_TOOLTIP = Component.translatable("world_preview.settings.general.threads.tooltip");
    public static final Component SETTINGS_GENERAL_FC = Component.translatable("world_preview.settings.general.full.chunk");
    public static final Component SETTINGS_GENERAL_STRUCT = Component.translatable("world_preview.settings.general.struct");
    public static final Component SETTINGS_GENERAL_STRUCT_TOOLTIP = Component.translatable("world_preview.settings.general.struct.tooltip");
    public static final Component SETTINGS_GENERAL_HEIGHTMAP = Component.translatable("world_preview.settings.general.heightmap");
    public static final Component SETTINGS_GENERAL_HEIGHTMAP_TOOLTIP = Component.translatable("world_preview.settings.general.heightmap.tooltip");
    public static final Component SETTINGS_GENERAL_INTERSECT = Component.translatable("world_preview.settings.general.intersect");
    public static final Component SETTINGS_GENERAL_INTERSECT_TOOLTIP = Component.translatable("world_preview.settings.general.intersect.tooltip");
    public static final Component SETTINGS_GENERAL_FC_TOOLTIP = Component.translatable("world_preview.settings.general.full.chunk.tooltip");
    public static final Component SETTINGS_GENERAL_BG = Component.translatable("world_preview.settings.general.background");
    public static final Component SETTINGS_GENERAL_BG_TOOLTIP = Component.translatable("world_preview.settings.general.background.tooltip");
    public static final Component SETTINGS_GENERAL_CONTROLS = Component.translatable("world_preview.settings.general.controls");
    public static final Component SETTINGS_GENERAL_CONTROLS_TOOLTIP = Component.translatable("world_preview.settings.general.controls.tooltip");
    public static final Component SETTINGS_GENERAL_FRAMETIME = Component.translatable("world_preview.settings.general.frametime");
    public static final Component SETTINGS_GENERAL_FRAMETIME_TOOLTIP = Component.translatable("world_preview.settings.general.frametime.tooltip");

    // - Sampling settings
    public static final Component SETTINGS_SAMPLE_TITLE = Component.translatable("world_preview.settings.sample.title");
    public static final Component SETTINGS_SAMPLE_HEAD = Component.translatable("world_preview.settings.sample.head");
    public static final Component SETTINGS_SAMPLE_PIXELS_TITLE_1 = Component.translatable("world_preview.settings.sample.numChunk.title1");
    public static final Component SETTINGS_SAMPLE_PIXELS_TITLE_2 = Component.translatable("world_preview.settings.sample.numChunk.title2");
    public static final Component SETTINGS_SAMPLE_SAMPLE_TITLE_1 = Component.translatable("world_preview.settings.sample.sampler.title1");
    public static final Component SETTINGS_SAMPLE_SAMPLE_TITLE_2 = Component.translatable("world_preview.settings.sample.sampler.title2");

    // - Heightmap settings
    public static final Component SETTINGS_HEIGHTMAP_TITLE = Component.translatable("world_preview.settings.heightmap.title");
    public static final Component SETTINGS_HEIGHTMAP_DISABLED = Component.translatable("world_preview.settings.heightmap.disabled");
    public static final Component SETTINGS_HEIGHTMAP_PRESETS = Component.translatable("world_preview.settings.heightmap.presets");
    public static final Component SETTINGS_HEIGHTMAP_COLORMAP = Component.translatable("world_preview.settings.heightmap.colormap");
    public static final Component SETTINGS_HEIGHTMAP_MIN_Y = Component.translatable("world_preview.settings.heightmap.minY");
    public static final Component SETTINGS_HEIGHTMAP_MAX_Y = Component.translatable("world_preview.settings.heightmap.maxY");
    public static final Component SETTINGS_HEIGHTMAP_MIN_Y_TOOLTIP = Component.translatable("world_preview.settings.heightmap.minY.tooltip");
    public static final Component SETTINGS_HEIGHTMAP_MAX_Y_TOOLTIP = Component.translatable("world_preview.settings.heightmap.maxY.tooltip");
    public static final Component SETTINGS_HEIGHTMAP_VISUAL = Component.translatable("world_preview.settings.heightmap.visual");
    public static final Component SETTINGS_HEIGHTMAP_VISUAL_TOOLTIP = Component.translatable("world_preview.settings.heightmap.visual.tooltip");

    // - Dimensions settings
    public static final Component SETTINGS_DIM_TITLE = Component.translatable("world_preview.settings.dimensions.title");
    public static final Component SETTINGS_DIM_HEAD = Component.translatable("world_preview.settings.dimensions.head");

    // - Biome color chooser
    public static final Component SETTINGS_BIOMES_TITLE = Component.translatable("world_preview.settings.biomes.title");

    public static final Component COLOR_HUE = Component.translatable("world_preview.color.picker.hue");
    public static final Component COLOR_SAT = Component.translatable("world_preview.color.picker.saturation");
    public static final Component COLOR_VAL = Component.translatable("world_preview.color.picker.value");
    public static final Component COLOR_R = Component.translatable("world_preview.color.picker.r");
    public static final Component COLOR_G = Component.translatable("world_preview.color.picker.g");
    public static final Component COLOR_B = Component.translatable("world_preview.color.picker.b");

    public static final Component COLOR_CAVE = Component.translatable("world_preview.settings.biomes.cave");
    public static final Component COLOR_RESET = Component.translatable("world_preview.settings.biomes.reset");
    public static final Component COLOR_APPLY = Component.translatable("world_preview.settings.biomes.apply");
    public static final Component COLOR_LIST_FILTER = Component.translatable("world_preview.settings.biomes.filter");



}
