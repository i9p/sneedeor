package moe.deco.sneedeor;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import moe.deco.sneedeor.commands.StarscriptCommand;
import moe.deco.sneedeor.hud.ActiveModulesPlusHud;
import moe.deco.sneedeor.hud.CombatPlusHud;
import moe.deco.sneedeor.hud.EquipmentHud;
import moe.deco.sneedeor.modules.combat.Backstabber;
import moe.deco.sneedeor.modules.misc.NWordCounter;
import org.slf4j.Logger;

public class SneedeorAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    //public static final Category CATEGORY = new Category("");
    public static final HudGroup HUD_GROUP = new HudGroup("sneedeor");

    @Override
    public void onInitialize() {
        LOG.info("Initializing sneedeor");

        // Modules
        Modules modules = Modules.get();
        modules.add(new Backstabber());
        modules.add(new NWordCounter());

        // Commands
        Commands.add(new StarscriptCommand());

        // HUD
        Hud.get().register(CombatPlusHud.INFO);
        Hud.get().register(ActiveModulesPlusHud.INFO);
        Hud.get().register(EquipmentHud.INFO);
    }

    /*@Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }*/

    @Override
    public String getPackage() {
        return "moe.deco.sneedeor";
    }
}
