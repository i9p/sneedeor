package moe.deco.sneedeor;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import moe.deco.sneedeor.commands.EchoCommand;
import moe.deco.sneedeor.hud.ActiveModulesPlusHud;
import moe.deco.sneedeor.hud.CombatPlusHud;
import moe.deco.sneedeor.hud.EquipmentHud;
import moe.deco.sneedeor.modules.combat.Backstabber;
import moe.deco.sneedeor.modules.misc.GamerWordCounter;
import moe.deco.sneedeor.utils.SneedeorStarscript;
import org.slf4j.Logger;

public class SneedeorAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    //public static final Category CATEGORY = new Category("");
    public static final HudGroup HUD_GROUP = new HudGroup("sneedeor");

    @Override
    public void onInitialize() {
        LOG.info("Initializing sneedeor");

        SneedeorStarscript.init();
        MeteorStarscript.ss.set("quote", SneedeorStarscript::quote)
            .set("sneedeor", Value.map(new ValueMap()
                .set("thrembo", SneedeorStarscript::thrembo)
            ));

        // Modules
        Modules modules = Modules.get();
        modules.add(new Backstabber());
        modules.add(new GamerWordCounter());

        // Commands
        Commands.add(new EchoCommand());

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
