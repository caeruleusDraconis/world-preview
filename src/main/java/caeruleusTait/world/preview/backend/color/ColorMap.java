package caeruleusTait.world.preview.backend.color;

import net.minecraft.resources.ResourceLocation;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Colormap implementation
 * <p>
 * This implementation is <i>heavily</i> inspired by <a href="https://github.com/mahdilamb/colormap/tree/main">mahdilamb/colormap</a>. Some
 * sections are copied and pasted, while others where slightly modified to remove caching / other edge cases.
 */
public class ColorMap {
    private final ResourceLocation key;
    private final String name;
    private final Color[] colors;

    public record Color(float r, float g, float b) {
    }

    public record RawColorMap(String name, List<List<Float>> data) {
    }

    public ColorMap(ResourceLocation key, String name, Color[] colors) {
        this.key = key;
        this.name = name;
        this.colors = colors;
    }

    public ResourceLocation key() {
        return key;
    }

    public String name() {
        return name;
    }

    public ColorMap(ResourceLocation key, RawColorMap raw) {
        this.key = key;
        this.name = raw.name;

        if (raw.data.size() < 2) {
            throw new InvalidParameterException(name + ": All colormaps MUST have at least 2 entries");
        }
        this.colors = new Color[raw.data.size()];
        for (int i = 0; i < raw.data.size(); ++i) {
            List<Float> entry = raw.data.get(i);
            if (entry.size() != 3) {
                throw new InvalidParameterException(name + ": All entries in a colormap data MUST have exactly 3 elements: [R, G, B]!");
            }
            if (entry.stream().anyMatch(x -> x < 0.0 || x > 1.0)) {
                throw new InvalidParameterException(name + ": All values in a colormap data MUST be between 0.0 and 1.0 (inclusive)");
            }
            colors[i] = new Color(entry.get(0), entry.get(1), entry.get(2));
        }
    }

    /**
     * Calculates the color for a given position in the colormap.
     *
     * @param position must be between 0 and 1 inclusive
     */
    public Color get(float position) {
        if (position < 0) {
            return colors[0];
        } else if (position > 1) {
            return colors[colors.length - 1];
        } else {
            float pos = position * (colors.length - 1);
            int floor = (int) pos;
            if (pos == floor) {
                return colors[floor];
            }
            if (pos == floor + 1) {
                return colors[floor + 1];
            }
            return lerp(colors[floor], colors[floor + 1], pos - floor);
        }
    }

    public int getARGB(float position) {
        final Color c = get(position);
        final int R = ((int) (c.r * 255f)) & 0xFF;
        final int G = ((int) (c.g * 255f)) & 0xFF;
        final int B = ((int) (c.b * 255f)) & 0xFF;
        return (R << 0) | (G << 8) | (B << 16) | (0xFF << 24);
    }

    public int[] bake(int yMin, int yMax, int yVisMin, int yVisMax) {
        int[] res = new int[yMax - yMin];

        float visRange = (float)yVisMax - (float)yVisMin;
        for (int i = yMin; i < yMax; ++i) {
            res[i - yMin] = getARGB((float)(i - yVisMin) / visRange);
        }

        return res;
    }

    static float[] RGBToXYZ(float[] out, float r, float g, float b) {
        final float R = (r > 0.04045 ? (float) Math.pow((r + 0.055f) / 1.055f, 2.4f) : r / 12.92f) * 100;
        final float G = (g > 0.04045 ? (float) Math.pow((g + 0.055f) / 1.055f, 2.4f) : g / 12.92f) * 100;
        final float B = (b > 0.04045 ? (float) Math.pow((b + 0.055f) / 1.055f, 2.4f) : b / 12.92f) * 100;

        out[0] = R * 0.4124564f + G * 0.3575761f + B * 0.1804375f;
        out[1] = R * 0.2126729f + G * 0.7151522f + B * 0.0721750f;
        out[2] = R * 0.0193339f + G * 0.1191920f + B * 0.9503041f;
        return out;
    }

    static float[] XYZToLab(float[] out, float x, float y, float z) {
        float a = x / 95.047f, b = y * .01f, c = z / 108.883f;
        final float j = 16f / 116f;
        a = a > 0.008856 ? (float) Math.cbrt(a) : (7.787f * a + j);
        b = b > 0.008856 ? (float) Math.cbrt(b) : (7.787f * b + j);
        c = c > 0.008856 ? (float) Math.cbrt(c) : (7.787f * c + j);

        out[0] = (116f * b) - 16f;
        out[1] = 500f * (a - b);
        out[2] = 200f * (b - c);
        return out;
    }

    static float[] LabToXYZ(float[] out, float L, float a, float b) {
        out[1] = (L + 16f) / 116f;
        out[0] = (a / 500f) + out[1];
        out[2] = out[1] - b / 200f;
        for (int i = 0; i < 3; i++) {
            if (out[i] > 0.20689303442f) {
                out[i] *= out[i] * out[i];
            } else {
                out[i] = (out[i] - 16f / 116f) / 7.787f;
            }
        }

        out[0] *= 95.047f;
        out[1] *= 100f;
        out[2] *= 108.883f;
        return out;
    }

    static float[] XYZToRGB(float[] out, float x, float y, float z) {
        final float X = x / 100;
        final float Y = y / 100;
        final float Z = z / 100;
        out[0] = X * 3.2404542f + Y * -1.5371385f + Z * -0.4985314f;
        out[1] = X * -0.9692660f + Y * 1.8760108f + Z * 0.0415560f;
        out[2] = X * 0.0556434f + Y * -0.2040259f + Z * 1.0572252f;
        for (int i = 0; i < 3; i++) {
            if (out[i] > 0.0031308) {
                out[i] = 1.055f * ((float) Math.pow(out[i], (1 / 2.4))) - 0.055f;
            } else {
                out[i] = 12.92f * out[i];
            }
        }
        return out;
    }

    /**
     * Convert a color from L*ab space to sRGB space
     *
     * @param L the L component
     * @param a the a component
     * @param b the b component
     * @return a 3-component float array containing the color in rgb space
     */
    public static float[] LabToRGB(float L, float a, float b) {
        final float[] out = new float[3];
        return XYZToRGB(LabToXYZ(out, L, a, b), out[0], out[1], out[2]);
    }

    /**
     * Convert a color from sRGB space to L*ab space
     *
     * @param r the red value (0-1)
     * @param g the green value (0-1)
     * @param b the blue value (0-1)
     * @return a 3-component float array with the color in L*ab space
     */
    public static float[] RGBToLab(float r, float g, float b) {
        final float[] out = new float[3];
        return XYZToLab(RGBToXYZ(out, r, g, b), out[0], out[1], out[2]);
    }

    /**
     * Return val clamped between min and max
     *
     * @param val Value to clamp
     * @param min Minimum the value can be
     * @param max Maximum the value can be
     * @return Value clamped between min and max
     */
    public static float clamp(final float val, final float min, final float max) {
        return Math.min(Math.max(val, min), max);
    }

    /**
     * Linear interpolation
     *
     * @param low  Right value
     * @param high Left value
     * @param amt  Amount of interpolation between 0 and 1;
     * @return A linearly interpolated value
     */
    public static float lerp(final float low, final float high, float amt) {
        amt = clamp(amt, 0f, 1f);
        return low * amt + high * (1 - amt);
    }

    /**
     * Linearly interpolate between colors in L*ab space
     *
     * @param lower  Lower color to interpolate from
     * @param upper  Higher color to interpolate to
     * @param amount The amount to interpolate between the two colors
     * @return A new color interpolated in L*ab space between lower and upper
     */
    public static Color lerp(final Color lower, final Color upper, final float amount) {
        final float[] lowerLab = RGBToLab(lower.r, lower.g, lower.b);
        final float[] upperLab = RGBToLab(upper.r, upper.g, upper.b);

        final float[] rgb = LabToRGB(
                lerp(upperLab[0], lowerLab[0], amount),
                lerp(upperLab[1], lowerLab[1], amount),
                lerp(upperLab[2], lowerLab[2], amount)
        );
        return new Color(clamp(rgb[0], 0f, 1f), clamp(rgb[1], 0f, 1f), clamp(rgb[2], 0f, 1f));
    }

}
