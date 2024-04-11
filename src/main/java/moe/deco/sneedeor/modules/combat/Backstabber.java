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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;

import java.util.Random;

public class Backstabber extends Module {
    public Backstabber() {
        super(Categories.Combat, "backstabber", "*Teleports behind you*");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between teleports")
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Distance from target to initiate teleporting")
        .defaultValue(6)
        .min(0).sliderMax(10)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("Targetting priority")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );

    private final Setting<Double> randomness = sgGeneral.add(new DoubleSetting.Builder()
        .name("randomness")
        .description("How much randomness to add")
        .defaultValue(0)
        .min(0).max(3)
        .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 0, 255, 55))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 0, 255))
        .build()
    );

    int cooldown = 0;
    Entity target = null;
    Vec3d goal = null;

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

        Random r = new Random();

        target = TargetUtils.getPlayerTarget(range.get(), priority.get());
        if (target == null) return;

        Vec3d targetPos = target.getPos();
        float targetYaw = (target.getYaw() - 90F) / MathHelper.DEGREES_PER_RADIAN;
        Vec3d offsetVec = new Vec3d(range.get() / 2F * MathHelper.cos(targetYaw), 0, range.get() / 2F * MathHelper.sin(targetYaw));
        goal = targetPos.add(offsetVec);
        //info("%s", mc.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z)).getBlock());
        mc.player.updatePosition(goal.x + ((r.nextDouble() * 2 - 1) * randomness.get()), goal.y, goal.z + ((r.nextDouble() * 2 - 1) * randomness.get()));
    }

    @EventHandler
    public void render3D(Render3DEvent event) {
        if (target == null) return;
        event.renderer.sideHorizontal(goal.x - 0.25, goal.y, goal.z - 0.25, goal.x + 0.25, goal.z + 0.25, sideColor.get(), lineColor.get(), shapeMode.get());
    }
}
