package moe.deco.sneedeor.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
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

    private final Setting<Boolean> continuous = sgGeneral.add(new BoolSetting.Builder()
        .name("continuous")
        .description("Continuously teleports.")
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between teleports.")
        .visible(continuous::get)
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("keybind")
        .description("The keybind to teleport.")
        .visible(() -> !continuous.get())
        .action(this::teleport)
        .build()
    );

    private final Setting<Boolean> look = sgGeneral.add(new BoolSetting.Builder()
        .name("look")
        .description("Sets the client looking direction to the target.")
        .visible(() -> !continuous.get())
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

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .visible(renderPosition::get)
        .defaultValue(new SettingColor(255, 0, 255))
        .build()
    );

    int cooldown = 0;
    Entity target = null;
    Vec3d tp = null;

    Random random = new Random();

    @Override
    public void onActivate() {
        cooldown = 0;
        target = null;
    }

    @Override
    public String getInfoString() {
        if (target == null) return null;
        return EntityUtils.getName(target);
    }

    @EventHandler
    private void tick(TickEvent.Pre event) {
        target = TargetUtils.getPlayerTarget(range.get(), priority.get());
        if (target == null) return;

        Vec3d targetPos = target.getPos();
        Vec3d offsetVec = Vec3d.fromPolar(0, target.getYaw()).multiply(range.get() / 2).negate();
        tp = targetPos.add(offsetVec);

        if (continuous.get()) {
            if (cooldown > 0) {
                cooldown--;
                return;
            }
            cooldown = delay.get();
            teleport();
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
    }

    private void teleport() {
        if (target == null) return;
        if (randomness.get() == 0) {
            mc.player.updatePosition(tp.x, mc.player.getY(), tp.z);
        } else {
            double angle = 2 * Math.PI * random.nextDouble(); // Random angle
            double distance = randomness.get() * Math.sqrt(random.nextDouble()); // Random distance from the center
            double x = distance * Math.cos(angle);
            double z = distance * Math.sin(angle);
            mc.player.updatePosition(tp.x + x, mc.player.getY(), tp.z + z);
        }

        if (look.get() && !continuous.get()) {
            double dx = target.getX() - mc.player.getX();
            double dz = target.getZ() - mc.player.getZ();
            mc.player.setYaw((float) (Math.atan2(-dx, dz) * 180.0 / Math.PI));
            mc.player.setPitch(0);
        }
    }
}
