package moe.deco.sneedeor.modules.render;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.EntityType;

import java.util.Set;

public class DamageIndicator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to draw indicators on.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the indicators.")
        .defaultValue(1.1)
        .min(0.1)
        .build()
    );

    public DamageIndicator() {
        super(Categories.Render, "damage-indicators", "Displays damage indicators on hit");
    }

    //@EventHandler
    //private void onDamage(DamageEvent event) {
    //    if (entities.get().contains(event.entity)) info("%s %s", event.entity, event.source);
    //}
}
