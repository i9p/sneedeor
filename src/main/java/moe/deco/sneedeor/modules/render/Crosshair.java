package moe.deco.sneedeor.modules.render;

import meteordevelopment.meteorclient.events.game.WindowResizedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.Perspective;
import net.minecraft.world.GameMode;

public class Crosshair extends Module {
    public Crosshair() {
        super(Categories.Render, "Crosshair", "Renders a customizable crosshair, similar to Counter Strike.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCrosshair = settings.createGroup("Crosshair");
    private final SettingGroup sgOutlines = settings.createGroup("Outlines");
    private final SettingGroup sgCooldown = settings.createGroup("Attack Cooldown");

    private final Setting<Boolean> spectator = sgGeneral.add(new BoolSetting.Builder()
            .name("spectator")
            .description("Show crosshair in spectator mode.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> thirdPerson = sgGeneral.add(new BoolSetting.Builder()
            .name("third-person")
            .description("Show crosshair in third person mode.")
            .defaultValue(false)
            .build()
    );

    // Crosshair

    private final Setting<SettingColor> color = sgCrosshair.add(new ColorSetting.Builder()
        .name("color")
        .description("Color of the crosshair.")
        .defaultValue(new SettingColor(0, 255, 0))
        .build()
    );

    private final Setting<Boolean> dot = sgCrosshair.add(new BoolSetting.Builder()
        .name("dot")
        .description("Show dot in the center of the crosshair.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> tShape = sgCrosshair.add(new BoolSetting.Builder()
        .name("t-shape")
        .description("Hides the top crosshair line, making it 'T' shaped.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> size = sgCrosshair.add(new IntSetting.Builder()
        .name("size")
        .description("The size of the individual crosshair lines.")
        .defaultValue(5)
        .sliderRange(0, 100)
        .build()
    );

    private final Setting<Integer> gap = sgCrosshair.add(new IntSetting.Builder()
        .name("gap")
        .description("The gap in pixels between the dot and the crosshair lines.")
        .defaultValue(4)
        .sliderRange(-100, 100)
        .build()
    );

    private final Setting<Integer> thickness = sgCrosshair.add(new IntSetting.Builder()
        .name("thickness")
        .description("The thickness of the crosshair.")
        .defaultValue(1)
        .sliderRange(0, 100)
        .build()
    );

    // Outline

    private final Setting<Boolean> outline = sgOutlines.add(new BoolSetting.Builder()
        .name("outline")
        .description("Whether to render an outline.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> outlineColor = sgOutlines.add(new ColorSetting.Builder()
        .name("outline-color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(0, 0, 0))
        .build()
    );

    private final Setting<Integer> outlineThickness = sgOutlines.add(new IntSetting.Builder()
        .name("outline-thickness")
        .description("Thickness of the outline.")
        .defaultValue(1)
        .sliderRange(0, 5)
        .visible(outline::get)
        .build()
    );

    // Attack Cooldown

    private final Setting<Boolean> cooldown = sgCooldown.add(new BoolSetting.Builder()
        .name("cooldown")
        .description("Whether to show an attack cooldown bar.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> cooldownColor = sgCooldown.add(new ColorSetting.Builder()
        .name("cooldown-color")
        .description("Color of the cooldown bar.")
        .defaultValue(new SettingColor(0, 255, 0))
        .visible(cooldown::get)
        .build()
    );

    private final Setting<Boolean> cooldownFull = sgCooldown.add(new BoolSetting.Builder()
        .name("hide-when-full")
        .description("Hide attack cooldown bar when full.")
        .visible(cooldown::get)
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> cooldownSize = sgCooldown.add(new IntSetting.Builder()
        .name("cooldown-size")
        .description("The size of the attack cooldown bar.")
        .defaultValue(35)
        .sliderRange(5, 100)
        .visible(cooldown::get)
        .build()
    );

    private final Setting<Integer> cooldownOffset = sgCooldown.add(new IntSetting.Builder()
        .name("cooldown-offset")
        .description("The distance of the attack cooldown bar from the center of the screen.")
        .defaultValue(24)
        .sliderRange(-100, 100)
        .visible(cooldown::get)
        .build()
    );

    private final Setting<Integer> cooldownThickness = sgCooldown.add(new IntSetting.Builder()
        .name("cooldown-thickness")
        .description("The thickness of the attack cooldown bar.")
        .defaultValue(1)
        .sliderRange(0, 5)
        .visible(cooldown::get)
        .build()
    );

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON && !thirdPerson.get()) return;
        if (mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR && !spectator.get()) return;

        int centerX = mc.getWindow().getWidth() / 2;
        int centerY = mc.getWindow().getHeight() / 2;

        int tLX = centerX - thickness.get() / 2;
        int tLY = centerY - thickness.get() / 2;

        Renderer2D.COLOR.begin();
        if (dot.get()) renderQuad(tLX, tLY, thickness.get(), thickness.get()); // Dot
        if (!tShape.get()) renderQuad(tLX, tLY - gap.get() - size.get(), thickness.get(), size.get()); // Top crosshair
        renderQuad(tLX - gap.get() - size.get(), tLY, size.get(), thickness.get()); // Right crosshair
        renderQuad(tLX, tLY + gap.get() + thickness.get(), thickness.get(), size.get()); // Bottom crosshair
        renderQuad(tLX + gap.get() + thickness.get(), tLY, size.get(), thickness.get()); // Left crosshair

        if (cooldown.get()) {
            double cooldownProgress = mc.player.getAttackCooldownProgress(0);
            if (cooldownProgress != 1 || !cooldownFull.get()) renderQuad(centerX - cooldownSize.get() / 2, tLY + cooldownOffset.get(), cooldownSize.get(), cooldownThickness.get(), cooldownColor.get(), cooldownProgress);
        }
        Renderer2D.COLOR.render(null);
    }

    private void renderQuad(double x, double y, double width, double height) {
        renderQuad(x, y, width, height, color.get(), 1);
    }

    private void renderQuad(double x, double y, double width, double height, Color color, double progress) {
        if (outline.get()) Renderer2D.COLOR.quad(x - outlineThickness.get(), y - outlineThickness.get(), width + 2 * outlineThickness.get(), height + 2 * outlineThickness.get(), outlineColor.get());
        Renderer2D.COLOR.quad(x, y, width * progress, height, color);
    }
}
