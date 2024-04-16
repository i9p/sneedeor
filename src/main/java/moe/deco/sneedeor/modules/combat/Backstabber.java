package moe.deco.sneedeor.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;

import java.util.Random;

public class Backstabber extends Module {
    public Backstabber() {
        super(Categories.Combat, "backstabber", "Sets your position behind a target, most effective on players with low latency.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between teleports.")
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to begin teleporting. The distance from the target is half this value.")
        .defaultValue(6)
        .min(0).sliderMax(10)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );

    private final Setting<Double> randomness = sgGeneral.add(new DoubleSetting.Builder()
        .name("randomness")
        .description("Adds some random deviance from the teleport point. May lose tracking if set too high.")
        .defaultValue(0)
        .min(0).max(3)
        .build()
    );

    // Render

    private final Setting<Boolean> renderPosition = sgRender.add(new BoolSetting.Builder()
        .name("render-position")
        .description("Show the position of where to teleport the player")
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .visible(() -> renderPosition.get())
        .defaultValue(ShapeMode.Both)
        .build()
    );

    /*private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .visible(() -> renderPosition.get())
        .defaultValue(new SettingColor(255, 0, 255, 55))
        .build()
    );*/

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .visible(() -> renderPosition.get())
        .defaultValue(new SettingColor(255, 0, 255))
        .build()
    );

    int cooldown = 0;
    Entity target = null;
    Vec3d tp = null;

    Random random = new Random();

    @Override
    public void onActivate() {
        int cooldown = 0;
        Entity target = null;
    }

    @Override
    public String getInfoString() {
        if (target == null) return null;
        return EntityUtils.getName(target);
    }

    @EventHandler
    private void tick(TickEvent.Pre event) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        cooldown = delay.get();

        target = TargetUtils.getPlayerTarget(range.get(), priority.get());
        if (target == null) return;

        Vec3d targetPos = target.getPos();
        float targetYaw = (target.getYaw() - 90F) / MathHelper.DEGREES_PER_RADIAN;
        Vec3d offsetVec = new Vec3d(range.get() / 2F * MathHelper.cos(targetYaw), 0, range.get() / 2F * MathHelper.sin(targetYaw));
        tp = targetPos.add(offsetVec);

        if (randomness.get() == 0) {
            mc.player.updatePosition(tp.x, tp.y, tp.z);
        } else {
            double angle = 2 * Math.PI * random.nextDouble(); // Random angle
            double distance = randomness.get() * Math.sqrt(random.nextDouble()); // Random distance from the center
            double x = distance * Math.cos(angle);
            double z = distance * Math.sin(angle);
            mc.player.updatePosition(tp.x + x, tp.y, tp.z + z);
        }
    }

    @EventHandler
    public void render3D(Render3DEvent event) {
        if (target == null || !renderPosition.get()) return;
        double x1, z1; // Starting point of the line
        double x2, z2; // Ending point of the line


        double radius = randomness.get() == 0 ? 0.5 : randomness.get();
        int segments = 60;

        double increment = 2 * Math.PI / segments;

        for (int i = 0; i < segments; i++) {
            // Calculate the starting point of the current segment
            x1 = tp.x + radius * Math.cos(i * increment);
            z1 = tp.z + radius * Math.sin(i * increment);

            // Calculate the ending point of the current segment
            x2 = tp.x + radius * Math.cos((i + 1) * increment);
            z2 = tp.z + radius * Math.sin((i + 1) * increment);

            // Draw the segment
            event.renderer.line(x1, tp.y, z1, x2, tp.y, z2, lineColor.get());
        }


        //event.renderer.sideHorizontal(tp.x - 0.1, tp.y, tp.z - 0.1, tp.x + 0.1, tp.z + 0.1, sideColor.get(), lineColor.get(), shapeMode.get());
    }
}
