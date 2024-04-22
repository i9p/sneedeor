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
    private static final Pattern nwordRegex = Pattern.compile("nig(g(a|er)|let)"); // SHAMELESSLY stolen from atomic

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

        if (nwordRegex.matcher(message.getString().toLowerCase()).find()) {
            count++;
        }
    }
}
