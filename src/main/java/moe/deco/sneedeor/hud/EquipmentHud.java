package moe.deco.sneedeor.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import moe.deco.sneedeor.SneedeorAddon;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EquipmentHud extends HudElement {
    public static final HudElementInfo<EquipmentHud> INFO = new HudElementInfo<>(SneedeorAddon.HUD_GROUP, "equipment", "Displays your armor and offhand.", EquipmentHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDurability = settings.createGroup("Durability");
    private final SettingGroup sgScale = settings.createGroup("Scale");

    // General

    private final Setting<Boolean> mainHand = sgGeneral.add(new BoolSetting.Builder()
        .name("show-main-hand")
        .description("Displays main hand item.")
        .defaultValue(true)
        .onChanged(val -> calculateSize())
        .build()
    );

    private final Setting<Boolean> offHand = sgGeneral.add(new BoolSetting.Builder()
        .name("show-off-hand")
        .description("Displays off hand item.")
        .defaultValue(true)
        .onChanged(val -> calculateSize())
        .build()
    );

    // Durability

    private final Setting<Durability> display = sgDurability.add(new EnumSetting.Builder<Durability>()
        .name("display")
        .description("How to display the durability text")
        .defaultValue(Durability.Percentage)
        .onChanged(val -> calculateSize())
        .build()
    );

    private final Setting<Boolean> durabilityColor = sgDurability.add(new BoolSetting.Builder()
        .name("bar-color")
        .description("Text color is based on item durability.")
        .visible(() -> display.get() != Durability.None)
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> textColor = sgDurability.add(new ColorSetting.Builder()
        .name("text-color")
        .description("The color of the durability text.")
        .visible(() -> display.get() != Durability.None && !durabilityColor.get())
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<Boolean> shadow = sgDurability.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Displays shadow behind text.")
        .visible(() -> display.get() != Durability.None)
        .onChanged(val -> calculateSize())
        .defaultValue(true)
        .build()
    );

    // Scale

    private final Setting<Integer> scale = sgScale.add(new IntSetting.Builder()
        .name("item-scale")
        .description("The scale of the items.")
        .defaultValue(2)
        .onChanged(val -> calculateSize())
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Double> textScale = sgScale.add(new DoubleSetting.Builder()
        .name("text-scale")
        .description("The scale of the durability text.")
        .defaultValue(1)
        .onChanged(val -> calculateSize())
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    private boolean firstTick = true;
    private double textHeight = 0;

    public EquipmentHud() {
        super(INFO);
        calculateSize();
    }

    private void calculateSize() {
        setSize(16 * scale.get() * (4 + ((mainHand.get()) ? 1 : 0 ) + ((offHand.get()) ? 1 : 0 )), 16 * scale.get() + textHeight);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        textHeight = renderer.textHeight(shadow.get(), textScale.get());
        if (firstTick) {
            calculateSize();
            firstTick = false;
        }

        List<ItemStack> equippedItems = new ArrayList<>();
        if (isInEditor() || mc.player == null) {
            equippedItems.add(Items.NETHERITE_HELMET.getDefaultStack());
            equippedItems.add(Items.NETHERITE_CHESTPLATE.getDefaultStack());
            equippedItems.add(Items.NETHERITE_LEGGINGS.getDefaultStack());
            equippedItems.add(Items.NETHERITE_BOOTS.getDefaultStack());
            if (offHand.get()) equippedItems.add(Items.TOTEM_OF_UNDYING.getDefaultStack());
            if (mainHand.get()) equippedItems.add(Items.NETHERITE_SWORD.getDefaultStack());
        } else {
            equippedItems = new ArrayList<>(mc.player.getInventory().armor);
            Collections.reverse(equippedItems);
            if (offHand.get()) equippedItems.add(mc.player.getOffHandStack());
            if (mainHand.get()) equippedItems.add(mc.player.getMainHandStack());
        }

        for (ItemStack equippedItem : equippedItems) {
            if (equippedItem.isEmpty()) continue;

            renderer.item(equippedItem, (int) x, (int) Math.floor(y + textHeight), scale.get(), true);
            if (equippedItem.isDamageable() && display.get() != Durability.None) {
                String durability = switch (display.get()) {
                    case Percentage -> String.format("%.0f", ((equippedItem.getMaxDamage() - equippedItem.getDamage()) * 100f) / (float) equippedItem.getMaxDamage());
                    case Total -> Integer.toString(equippedItem.getMaxDamage() - equippedItem.getDamage());
                    default -> "err";
                };
                double textX = x + (16 * scale.get() - renderer.textWidth(durability, shadow.get(), textScale.get())) / 2;
                Color tColor = (durabilityColor.get()) ? new Color(equippedItem.getItemBarColor()) : textColor.get();
                renderer.text(durability, textX, y, tColor.a(255), shadow.get(), textScale.get());
            }
            x += 16 * scale.get();
        }
    }

    public enum Durability {
        None,
        Percentage,
        Total,
    }
}
