package moe.deco.sneedeor.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import moe.deco.sneedeor.SneedeorAddon;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ActiveModulesPlusHud extends HudElement {
    public static final HudElementInfo<ActiveModulesPlusHud> INFO = new HudElementInfo<>(SneedeorAddon.HUD_GROUP, "active-modules+", "Displays your active modules but prettier.", ActiveModulesPlusHud::new);

    private static final Color WHITE = new Color();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Module>> hiddenModules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("hidden-modules")
        .description("Which modules not to show in the list.")
        .build()
    );

    private final Setting<Sort> sort = sgGeneral.add(new EnumSetting.Builder<Sort>()
        .name("sort")
        .description("How to sort active modules.")
        .defaultValue(Sort.Biggest)
        .build()
    );

    private final Setting<Boolean> activeInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("additional-info")
        .description("Shows additional info from the module next to the name in the active modules list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> moduleInfoColor = sgGeneral.add(new ColorSetting.Builder()
        .name("module-info-color")
        .description("Color of module info text.")
        .defaultValue(new SettingColor(175, 175, 175))
        .visible(activeInfo::get)
        .build()
    );

    private final Setting<ColorMode> colorMode = sgGeneral.add(new EnumSetting.Builder<ColorMode>()
        .name("color-mode")
        .description("What color to use for active modules.")
        .defaultValue(ColorMode.Rainbow)
        .build()
    );

    private final Setting<SettingColor> flatColor = sgGeneral.add(new ColorSetting.Builder()
        .name("flat-color")
        .description("Color for flat color mode.")
        .defaultValue(new SettingColor(225, 25, 25))
        .visible(() -> colorMode.get() == ColorMode.Flat)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    private final Setting<Boolean> background = sgGeneral.add(new BoolSetting.Builder()
        .name("background")
        .description("Whether or not to render a background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> bgColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color for the background.")
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .visible(background::isVisible)
        .build()
    );

    private final Setting<Outlines> outlines = sgGeneral.add(new EnumSetting.Builder<Outlines>()
        .name("outlines")
        .description("Whether or not to render outlines.")
        .defaultValue(Outlines.None)
        .build()
    );

    private final Setting<Integer> outlineWidth = sgGeneral.add(new IntSetting.Builder()
        .name("outline-width")
        .description("Outline width")
        .defaultValue(2)
        .min(1)
        .sliderMin(1)
        .visible(() -> outlines.get() != Outlines.None)
        .build()
    );

    private final Setting<Boolean> customScale = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies custom text scale rather than the global one.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    private final Setting<Double> rainbowSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("Rainbow speed of rainbow color mode.")
        .defaultValue(0.05)
        .sliderMin(-0.5)
        .sliderMax(0.5)
        .decimalPlaces(4)
        .visible(() -> colorMode.get() == ColorMode.Rainbow || colorMode.get() == ColorMode.Astolfo)
        .build()
    );

    private final Setting<Double> rainbowSpread = sgGeneral.add(new DoubleSetting.Builder()
        .name("rainbow-spread")
        .description("Rainbow spread of rainbow color mode.")
        .defaultValue(0.01)
        .sliderMin(0.001)
        .sliderMax(0.05)
        .decimalPlaces(4)
        .visible(() -> colorMode.get() == ColorMode.Rainbow || colorMode.get() == ColorMode.Astolfo)
        .build()
    );

    private final Setting<Double> rainbowSaturation = sgGeneral.add(new DoubleSetting.Builder()
        .name("rainbow-saturation")
        .defaultValue(1.0d)
        .sliderRange(0.0d, 1.0d)
        .visible(() -> colorMode.get() == ColorMode.Rainbow || colorMode.get() == ColorMode.Astolfo)
        .build()
    );

    private final Setting<Double> rainbowBrightness = sgGeneral.add(new DoubleSetting.Builder()
        .name("rainbow-brightness")
        .defaultValue(1.0d)
        .sliderRange(0.0d, 1.0d)
        .visible(() -> colorMode.get() == ColorMode.Rainbow || colorMode.get() == ColorMode.Astolfo)
        .build()
    );

    private final List<Module> modules = new ArrayList<>();

    private final Color rainbow = new Color(255, 255, 255);
    private double rainbowHue1;
    private double rainbowHue2;

    private double prevX;
    private double prevTextLength;
    private Color prevColor = new Color();

    public ActiveModulesPlusHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        modules.clear();

        for (Module module : Modules.get().getActive()) {
            if (!hiddenModules.get().contains(module)) modules.add(module);
        }

        if (modules.isEmpty()) {
            if (isInEditor()) {
                setSize(renderer.textWidth("Active Modules", shadow.get(), getScale()), renderer.textHeight(shadow.get(), getScale()));
            }
            return;
        }

        modules.sort((e1, e2) -> switch (sort.get()) {
            case Alphabetical -> e1.title.compareTo(e2.title);
            case Biggest -> Double.compare(getModuleWidth(renderer, e2), getModuleWidth(renderer, e1));
            case Smallest -> Double.compare(getModuleWidth(renderer, e1), getModuleWidth(renderer, e2));
        });

        double width = 0;
        double height = 0;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            width = Math.max(width, getModuleWidth(renderer, module));
            height += renderer.textHeight(shadow.get(), getScale());
            if (i > 0) height += 2;
        }

        setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;

        if (modules.isEmpty()) {
            if (isInEditor()) {
                renderer.text("Active Modules", x, y, WHITE, shadow.get(), getScale());
            }
            return;
        }

        rainbowHue1 += rainbowSpeed.get() * renderer.delta;
        if (rainbowHue1 > 1) rainbowHue1 -= 1;
        else if (rainbowHue1 < -1) rainbowHue1 += 1;

        rainbowHue2 = rainbowHue1;

        prevX = x;

        for (int i = 0; i < modules.size(); i++) {
            double offset = alignX(getModuleWidth(renderer, modules.get(i)), alignment.get());
            renderModule(renderer, modules, i, x + offset, y);

            prevX = x + offset;
            y += 2 + renderer.textHeight(shadow.get(), getScale());
        }
    }

    private void renderModule(HudRenderer renderer, List<Module> modules, int index, double x, double y) {
        Module module = modules.get(index);
        Color color;
        Color bg = bgColor.get();

        switch (colorMode.get()) {
            case Random -> color = prevColor = module.color;
            case Rainbow -> {
                rainbowHue2 += rainbowSpread.get();
                int c = java.awt.Color.HSBtoRGB((float) rainbowHue2, rainbowSaturation.get().floatValue(), rainbowBrightness.get().floatValue());
                rainbow.r = Color.toRGBAR(c);
                rainbow.g = Color.toRGBAG(c);
                rainbow.b = Color.toRGBAB(c);
                color = rainbow;
            }
            case Astolfo -> {
                rainbowHue2 += rainbowSpread.get();
                int c = java.awt.Color.HSBtoRGB(MathHelper.abs((float) (MathHelper.floorMod(rainbowHue2, 1) - 0.5F)) + 0.5F, rainbowSaturation.get().floatValue(), rainbowBrightness.get().floatValue());
                rainbow.r = Color.toRGBAR(c);
                rainbow.g = Color.toRGBAG(c);
                rainbow.b = Color.toRGBAB(c);
                color = rainbow;
            }
            default -> color = prevColor = flatColor.get();
        }
        if (index == 0) prevColor = color;

        renderer.text(module.title, x, y, color, shadow.get(), getScale());

        double emptySpace = renderer.textWidth(" ", shadow.get(), getScale());
        double textHeight = renderer.textHeight(shadow.get(), getScale());
        double textLength = renderer.textWidth(module.title, shadow.get(), getScale());

        if (activeInfo.get()) {
            String info = module.getInfoString();
            if (info != null) {
                renderer.text(info, x + emptySpace + textLength, y, moduleInfoColor.get(), shadow.get(), getScale());
                textLength += emptySpace + renderer.textWidth(info, shadow.get(), getScale());
            }
        }

        if (background.get()) renderer.quad(x - 2, y - 2, textLength + 4, textHeight + 2, bg, bg, bg, bg);
        if (outlines.get() != Outlines.None) {
            if (outlines.get() != Outlines.Right) // Left quad
                renderer.quad(x - 2 - outlineWidth.get(), y - 2, outlineWidth.get(), textHeight + 2, prevColor, prevColor, color, color); // Left quad
            if (outlines.get() != Outlines.Left) // Right quad
                renderer.quad(x + 2 + textLength, y - 2, outlineWidth.get(), textHeight + 2, prevColor, prevColor, color, color); // Right quad
            if (outlines.get() == Outlines.All) {
                // Top and bottom quads
                if (index == 0) {
                    renderer.quad(x - 2 - outlineWidth.get(), y - 2 - outlineWidth.get(), textLength + 4 + (outlineWidth.get() * 2), outlineWidth.get(), prevColor, prevColor, color, color); // Top quad
                    if (index == modules.size() - 1) renderer.quad(x - 2 - outlineWidth.get(), y + textHeight, textLength + 4 + (outlineWidth.get() * 2), outlineWidth.get(), prevColor, prevColor, color, color); // Bottom quad
                } else if (index == modules.size() - 1) {
                    renderer.quad(x - 2 - outlineWidth.get(), y + textHeight, textLength + 4 + (outlineWidth.get() * 2), outlineWidth.get(), color, color, color, color); // Bottom quad
                }

                // Intermediate quads
                if (index != 0) {
                    renderer.quad(prevX - 2 - outlineWidth.get(),y-2,x - prevX, outlineWidth.get(), prevColor, prevColor, prevColor, prevColor);
                    renderer.quad(x + 2 + textLength + outlineWidth.get(),y-2, (prevX + prevTextLength) - (x + textLength), outlineWidth.get(), prevColor, prevColor, prevColor, prevColor);
                }
            }
        }

        prevTextLength = textLength;
        prevColor = new Color(color);
    }

    private double getModuleWidth(HudRenderer renderer, Module module) {
        double width = renderer.textWidth(module.title, shadow.get(), getScale());

        if (activeInfo.get()) {
            String info = module.getInfoString();
            if (info != null) width += renderer.textWidth(" ", shadow.get(), getScale()) + renderer.textWidth(info, shadow.get(), getScale());
        }

        return width;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : -1;
    }

    public enum Sort {
        Alphabetical,
        Biggest,
        Smallest
    }

    public enum ColorMode {
        Flat,
        Random,
        Rainbow,
        Astolfo
    }

    public enum Outlines {
        None,
        All,
        Both,
        Left,
        Right
    }
}
