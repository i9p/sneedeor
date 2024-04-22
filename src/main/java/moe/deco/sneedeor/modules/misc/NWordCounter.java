package moe.deco.sneedeor.modules.misc;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

public class NWordCounter extends Module {
    public NWordCounter() {
        super(Categories.Misc, "n-word-counter", "Counts the number of n-words in chat.");
    }

    private int count = 0;
    private static final Pattern nWordPattern = Pattern.compile(
        "(nig(g(er|ress|u([rh])|y|a([hr])?)?|([eo])r|r([ae])|ar|ette|nog| |$)|neg(ro|er))");

    @Override
    public String getInfoString() {
        return String.valueOf(count);
    }

    @Override
    public void onActivate() {
        count = 0;
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        Text message = event.getMessage();
        count += (int) nWordPattern.matcher(message.getString().toLowerCase()).results().count();
    }
}
