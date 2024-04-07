package moe.deco.sneedeor;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import moe.deco.sneedeor.commands.StarscriptCommand;
import moe.deco.sneedeor.hud.CombatPlusHud;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import moe.deco.sneedeor.modules.render.DamageIndicatorHud;
import org.slf4j.Logger;

public class SneedeorAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    //public static final Category CATEGORY = new Category("");
    public static final HudGroup HUD_GROUP = new HudGroup("sneedeor");

    @Override
    public void onInitialize() {
        LOG.info("Initializing sneedeor");

        // Modules
        Modules.get().add(new DamageIndicatorHud());

        // Commands
        Commands.add(new StarscriptCommand());

        // HUD
        Hud.get().register(CombatPlusHud.INFO);
    }

    /*@Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }*/

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
