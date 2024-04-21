package moe.deco.sneedeor.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.*;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import moe.deco.sneedeor.SneedeorAddon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CombatPlusHud extends HudElement {
    private static final Color GREEN = new Color(15, 255, 15);
    private static final Color RED = new Color(255, 15, 15);
    private static final Color BLACK = new Color(0, 0, 0, 255);

    public static final HudElementInfo<CombatPlusHud> INFO = new HudElementInfo<>(SneedeorAddon.HUD_GROUP, "combat+", "Displays information about your combat target with added features.", CombatPlusHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColor = settings.createGroup("Color");

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .onChanged(aDouble -> calculateSize())
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to target players.")
        .defaultValue(100)
        .min(1)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> displayDamage = sgGeneral.add(new BoolSetting.Builder()
        .name("damage")
        .description("Shows the estimated attack damage.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayPing = sgGeneral.add(new BoolSetting.Builder()
        .name("ping")
        .description("Shows the player's ping.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("distance")
        .description("Shows the distance between you and the player.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayDurability = sgGeneral.add(new BoolSetting.Builder()
        .name("durability")
        .description("Shows the player's item durability as a percentage.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<Enchantment>> displayedEnchantments = sgGeneral.add(new EnchantmentListSetting.Builder()
        .name("displayed-enchantments")
        .description("The enchantments that are shown on the combat HUD.")
        .defaultValue(getDefaultEnchantments())
        .build()
    );

    // Color

    private final Setting<SettingColor> primaryColor = sgColor.add(new ColorSetting.Builder()
        .name("primary-color")
        .description("Primary color.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> secondaryColor = sgColor.add(new ColorSetting.Builder()
        .name("secondary-color")
        .description("Secondary color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );


    private final Setting<SettingColor> backgroundColor = sgColor.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of background.")
        .defaultValue(new SettingColor(0, 0, 0, 64))
        .build()
    );

    private final Setting<SettingColor> enchantmentTextColor = sgColor.add(new ColorSetting.Builder()
        .name("enchantment-color")
        .description("Color of enchantment text.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> pingColor1 = sgColor.add(new ColorSetting.Builder()
        .name("ping-stage-1")
        .description("Color of ping text when under 75.")
        .defaultValue(new SettingColor(15, 255, 15))
        .visible(displayPing::get)
        .build()
    );

    private final Setting<SettingColor> pingColor2 = sgColor.add(new ColorSetting.Builder()
        .name("ping-stage-2")
        .description("Color of ping text when between 75 and 200.")
        .defaultValue(new SettingColor(255, 150, 15))
        .visible(displayPing::get)
        .build()
    );

    private final Setting<SettingColor> pingColor3 = sgColor.add(new ColorSetting.Builder()
        .name("ping-stage-3")
        .description("Color of ping text when over 200.")
        .defaultValue(new SettingColor(255, 15, 15))
        .visible(displayPing::get)
        .build()
    );

    private final Setting<SettingColor> distColor1 = sgColor.add(new ColorSetting.Builder()
        .name("distance-stage-1")
        .description("The color when a player is within 10 blocks of you.")
        .defaultValue(new SettingColor(255, 15, 15))
        .visible(displayDistance::get)
        .build()
    );

    private final Setting<SettingColor> distColor2 = sgColor.add(new ColorSetting.Builder()
        .name("distance-stage-2")
        .description("The color when a player is within 50 blocks of you.")
        .defaultValue(new SettingColor(255, 150, 15))
        .visible(displayDistance::get)
        .build()
    );

    private final Setting<SettingColor> distColor3 = sgColor.add(new ColorSetting.Builder()
        .name("distance-stage-3")
        .description("The color when a player is greater then 50 blocks away from you.")
        .defaultValue(new SettingColor(15, 255, 15))
        .visible(displayDistance::get)
        .build()
    );

    private final Setting<SettingColor> healthColor1 = sgColor.add(new ColorSetting.Builder()
        .name("health-stage-1")
        .description("The color on the left of the health gradient.")
        .defaultValue(new SettingColor(255, 15, 15))
        .build()
    );

    private final Setting<SettingColor> healthColor2 = sgColor.add(new ColorSetting.Builder()
        .name("health-stage-2")
        .description("The color in the middle of the health gradient.")
        .defaultValue(new SettingColor(255, 150, 15))
        .build()
    );

    private final Setting<SettingColor> healthColor3 = sgColor.add(new ColorSetting.Builder()
        .name("health-stage-3")
        .description("The color on the right of the health gradient.")
        .defaultValue(new SettingColor(15, 255, 15))
        .build()
    );

    private PlayerEntity playerEntity;

    public CombatPlusHud() {
        super(INFO);

        calculateSize();
    }

    private void calculateSize() {
        setSize(175 * scale.get(), 95 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            double x = this.x;
            double y = this.y;

            if (isInEditor()) playerEntity = mc.player;
            else playerEntity = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);

            if (playerEntity == null && !isInEditor()) return;

            // Background
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, getWidth(), getHeight(), backgroundColor.get());

            if (playerEntity == null) {
                if (isInEditor()) {
                    renderer.line(x, y, x + getWidth(), y + getHeight(), Color.GRAY);
                    renderer.line(x + getWidth(), y, x, y + getHeight(), Color.GRAY);
                    Renderer2D.COLOR.render(null); // i know, ill fix it soon
                }
                return;
            }
            Renderer2D.COLOR.render(null);

            // Player Model
            drawEntity(renderer.drawContext, (int) (x - 60 * scale.get()), (int) (y - 15 * scale.get()), (int) (30 * scale.get()), -playerEntity.prevYaw, -playerEntity.getPitch(), playerEntity);

            // Moving pos to past player model
            x += 50 * scale.get();
            y += 5 * scale.get();

            // Setting up texts
            String breakText = " | ";

            // Name
            String nameText = playerEntity.getName().getString();
            Color nameColor = PlayerUtils.getPlayerColor(playerEntity, primaryColor.get());

            // Ping
            int ping = EntityUtils.getPing(playerEntity);
            String pingText = ping + "ms";

            Color pingColor;
            if (ping <= 75) pingColor = pingColor1.get();
            else if (ping <= 200) pingColor = pingColor2.get();
            else pingColor = pingColor3.get();

            // Distance
            double dist = 0;
            if (!isInEditor()) dist = Math.round(mc.player.distanceTo(playerEntity) * 100.0) / 100.0;
            String distText = dist + "m";

            Color distColor;
            if (dist <= 10) distColor = distColor1.get();
            else if (dist <= 50) distColor = distColor2.get();
            else distColor = distColor3.get();

            // Health
            float health = playerEntity.getHealth();
            float absorb = playerEntity.getAbsorptionAmount();

            // Status Text
            PlayerCategory risk;
            String friendText;
            Color friendColor;

            // Held Item
            ItemStack heldItem = getItem(5);

            if (Friends.get().isFriend(playerEntity)) {
                friendText = "Friend";
                friendColor = Config.get().friendColor.get();
            } else {
                double armor = EntityAttributeHelper.getAttributeValue(playerEntity, EntityAttributes.GENERIC_ARMOR);
                int prot = 0;
                for (int i = 0; i <= 3; i++) {
                    prot += EnchantmentHelper.getLevel(Enchantments.PROTECTION, getItem(i));
                }
                if (armor == 20.0 && prot == 16) risk = PlayerCategory.Armored;
                else if (armor == 12.0 && prot == 12 && getItem(2).getItem() == Items.ELYTRA) risk = PlayerCategory.Wasp;
                else if (armor > 0.0) risk = PlayerCategory.Weak;
                else risk = PlayerCategory.Naked;
                if (((heldItem.getItem() instanceof SwordItem || heldItem.getItem() instanceof AxeItem) && risk == PlayerCategory.Armored)
                    || heldItem.getItem() == Items.END_CRYSTAL
                    || heldItem.getItem() == Items.RESPAWN_ANCHOR
                    || heldItem.getItem() instanceof BedItem) risk = PlayerCategory.Threat;

                friendText = risk.toString();
                friendColor = risk.color;
            }

            TextRenderer.get().begin(0.45 * scale.get(), false, true);

            double breakWidth = TextRenderer.get().getWidth(breakText);
            double nameWidth = TextRenderer.get().getWidth(nameText);
            double pingWidth = TextRenderer.get().getWidth(pingText);

            TextRenderer.get().render(nameText, x, y, nameColor != null ? nameColor : primaryColor.get());
            if (displayDamage.get() && heldItem.getItem() != Items.END_CRYSTAL) {
                Pair<Float, Float> damageValues = new Pair<>(DamageUtils.getAttackDamage(playerEntity, mc.player), DamageUtils.getAttackDamage(mc.player, playerEntity));
                String outboundText = String.format("+%.2f dmg", damageValues.getRight());
                friendText += String.format(" (+%.2f)", damageValues.getLeft());
                TextRenderer.get().render(breakText, x + nameWidth, y, secondaryColor.get());
                TextRenderer.get().render(outboundText, x + nameWidth + breakWidth, y, (damageValues.getLeft() > damageValues.getRight()) ? RED : GREEN);
            }

            double friendWidth = TextRenderer.get().getWidth(friendText);

            y += TextRenderer.get().getHeight();

            TextRenderer.get().render(friendText, x, y, friendColor);

            if (displayPing.get()) {
                TextRenderer.get().render(breakText, x + friendWidth, y, secondaryColor.get());
                TextRenderer.get().render(pingText, x + friendWidth + breakWidth, y, pingColor);

                if (displayDistance.get()) {
                    TextRenderer.get().render(breakText, x + friendWidth + breakWidth + pingWidth, y, secondaryColor.get());
                    TextRenderer.get().render(distText, x + friendWidth + breakWidth + pingWidth + breakWidth, y, distColor);
                }
            } else if (displayDistance.get()) {
                TextRenderer.get().render(breakText, x + friendWidth, y, secondaryColor.get());
                TextRenderer.get().render(distText, x + friendWidth + breakWidth, y, distColor);
            }

            TextRenderer.get().end();

            // Moving pos down for armor
            y += 10 * scale.get();

            double armorX;
            double armorY;
            int slot = 5;

            // Drawing armor
            MatrixStack matrices = RenderSystem.getModelViewStack();

            matrices.push();
            matrices.translate(0, 0, 600); // show text above item
            matrices.scale(scale.get().floatValue(), scale.get().floatValue(), 1);

            x /= scale.get();
            y /= scale.get();

            TextRenderer.get().begin(0.35, false, true);

            for (int position = 0; position < 6; position++) {
                armorX = x + position * 20;
                armorY = y;

                ItemStack itemStack = getItem(slot);
                if (displayDurability.get() && itemStack.isDamageable()) {
                    String durabilityText = String.format("%.0f%%", ((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage());
                    Color durabilityColor = new Color(itemStack.getItemBarColor()).a(255);

                    TextRenderer.get().render(durabilityText, armorX, armorY, durabilityColor, true);
                }

                renderer.item(itemStack, (int) (armorX * scale.get()), (int) (armorY * scale.get()), scale.get().floatValue(), true);

                armorY += 18;

                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);
                Map<Enchantment, Integer> enchantmentsToShow = new HashMap<>();

                for (Enchantment enchantment : displayedEnchantments.get()) {
                    if (enchantments.containsKey(enchantment)) {
                        enchantmentsToShow.put(enchantment, enchantments.get(enchantment));
                    }
                }

                for (Enchantment enchantment : enchantmentsToShow.keySet()) {
                    String enchantName = Utils.getEnchantSimpleName(enchantment, 3) + " " + enchantmentsToShow.get(enchantment);

                    double enchX = (armorX + 8) - (TextRenderer.get().getWidth(enchantName) / 2);

                    TextRenderer.get().render(enchantName, enchX, armorY, enchantment.isCursed() ? RED : enchantmentTextColor.get());
                    armorY += TextRenderer.get().getHeight();
                }
                slot--;
            }

            TextRenderer.get().end();

            y = (int) (this.y + 91 * scale.get());
            x = this.x;

            // Health bar

            x /= scale.get();
            y /= scale.get();

            float maxHealth = playerEntity.getMaxHealth();
            int maxAbsorb = 16;
            int maxTotal = (int) (maxHealth + maxAbsorb);

            int totalHealthWidth = (int) (176 * maxHealth / maxTotal);
            int totalAbsorbWidth = 176 * maxAbsorb / maxTotal;

            double healthPercent = health / maxHealth;
            double absorbPercent = absorb / maxAbsorb;

            int healthWidth = (int) (totalHealthWidth * healthPercent);
            int absorbWidth = (int) (totalAbsorbWidth * absorbPercent);

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, healthWidth, 4, healthColor1.get(), healthColor2.get(), healthColor2.get(), healthColor1.get());
            Renderer2D.COLOR.quad(x + healthWidth, y, absorbWidth, 4, healthColor2.get(), healthColor3.get(), healthColor3.get(), healthColor2.get());
            Renderer2D.COLOR.render(null);

            String healthText = String.format("%.1f", health + absorb);

            TextRenderer.get().begin(0.5, false, true);
            y -= TextRenderer.get().getHeight(true);
            TextRenderer.get().render(healthText, x + 1, y, secondaryColor.get(), true);
            TextRenderer.get().end();

            matrices.pop();
        });
    }

    private ItemStack getItem(int i) {
        if (isInEditor()) {
            return switch (i) {
                case 5 -> Items.END_CRYSTAL.getDefaultStack();
                case 4 -> Items.TOTEM_OF_UNDYING.getDefaultStack();
                case 0 -> Items.NETHERITE_BOOTS.getDefaultStack();
                case 1 -> Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 2 -> Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 3 -> Items.NETHERITE_HELMET.getDefaultStack();
                default -> ItemStack.EMPTY;
            };
        }

        if (playerEntity == null) return ItemStack.EMPTY;

        return switch (i) {
            case 4 -> playerEntity.getOffHandStack();
            case 5 -> playerEntity.getMainHandStack();
            default -> playerEntity.getInventory().getArmorStack(i);
        };
    }

    /**
     * Draws an entity to the screen. The default version provided by InventoryScreen has had its parameters changed
     * such that it's no longer appropriate for this use case. As the new version uses rotation based on the mouse
     * position relative to itself, it causes some odd angle positioning that may also look "stuck" to one corner,
     * and the model's facing may change depending on how we reposition the element.
     * Additionally, it uses OpenGL scissors, which causes the player model to get cut when the Minecraft GUI scale is not 1x.
     * This version of drawEntity should fix these issues.
     */
    private void drawEntity(DrawContext context, int x, int y, int size, float yaw, float pitch, LivingEntity entity) {

        float tanYaw = (float) Math.atan((yaw) / 40.0f);
        float tanPitch = (float) Math.atan((pitch) / 40.0f);

        // By default, the origin of the drawEntity command is the top-center, facing down and straight to the south.
        // This means that the player model is upside-down. We'll apply a rotation of PI radians (180 degrees) to fix this.
        // This does have the downside of setting the origin to the bottom-center corner, though, so we'll have
        // to compensate for this later.
        Quaternionf quaternion = new Quaternionf().rotateZ((float) Math.PI);

        // The drawEntity command draws the entity using some entity parameters, so we'll have to manipulate some of
        // those to draw as we want. But first, we'll save the previous values, so we can restore them later.
        float previousBodyYaw = entity.bodyYaw;
        float previousYaw = entity.getYaw();
        float previousPitch = entity.getPitch();
        float previousPrevHeadYaw = entity.prevHeadYaw; // A perplexing name, I know!
        float prevHeadYaw = entity.headYaw;

        // Apply the rotation parameters
        entity.bodyYaw = 180.0f + tanYaw * 20.0f;
        entity.setYaw(180.0f + tanYaw * 40.0f);
        entity.setPitch(-tanPitch * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();

        // Recall the player's origin is now the bottom-center corner, so we'll have to offset the draw by half the width
        // to get it to render in the center.
        // As for the y parameter, adding the element's height draws it at the bottom, but in practice we want the player
        // to "float" somewhat, so we'll multiply it by some constant to have it hover. It turns out 0.9 is a good value.
        // The vector3 parameter applies a translation to the player's model. Given that we're simply offsetting
        // the draw in the x and y parameters, we won't really need this, so we'll set it to default.
        // It doesn't seem like quaternionf2 does anything, so we'll leave it null to save some computation.
        InventoryScreen.drawEntity(context, x + getWidth() / 2, y + getHeight() * 0.9f, size, new Vector3f(), quaternion, null, entity);

        // Restore the previous values
        entity.bodyYaw = previousBodyYaw;
        entity.setYaw(previousYaw);
        entity.setPitch(previousPitch);
        entity.prevHeadYaw = previousPrevHeadYaw;
        entity.headYaw = prevHeadYaw;
    }

    public static List<Enchantment> getDefaultEnchantments() {
        List<Enchantment> enchantments = new ArrayList<>();

        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            enchantments.add(enchantment);
        }

        return enchantments;
    }

    public enum PlayerCategory {
        Threat(Color.RED),
        Armored(Color.ORANGE),
        Wasp(Color.YELLOW),
        Weak(Color.GREEN),
        Naked(Color.LIGHT_GRAY);

        private final Color color;

        PlayerCategory(Color color) {
            this.color = color;
        }
    }
}
