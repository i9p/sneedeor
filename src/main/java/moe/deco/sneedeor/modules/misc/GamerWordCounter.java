package moe.deco.sneedeor.modules.misc;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

public class GamerWordCounter extends Module {
    public GamerWordCounter() {
        super(Categories.Misc, "gamer-words", "Counts the number of gamer words in chat.");
    }

    private final SettingGroup sgCount = settings.createGroup("Count");

    private final Setting<Boolean> nWord = sgCount.add(new BoolSetting.Builder()
        .name("n-words")
        .description("Words which describes majority users of menthol Newport cigarettes.")
        .onChanged(val -> nCount = 0)
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> fWord = sgCount.add(new BoolSetting.Builder()
        .name("f-slurs")
        .description("Words pertained to discussions of bundle of sticks.")
        .onChanged(val -> fCount = 0)
        .defaultValue(false)
        .build()
    );

    private int nCount = 0;
    private static final Pattern nPattern = Pattern.compile(
        "(nig(g(er|ress|u([rh])|y|a([hr])?)?|([eo])r|r([ae])|ar|ette|nog|\\b)|neg(ro|er))", Pattern.CASE_INSENSITIVE);

    private int fCount = 0;
    private static final Pattern fPattern = Pattern.compile(
        "fag(got)?", Pattern.CASE_INSENSITIVE);

    @Override
    public String getInfoString() {
        if (nWord.get() && fWord.get()) return String.format("N-words: %s | F-slurs: %s", nCount, fCount);
        else if (nWord.get()) return String.valueOf(nCount);
        else if (fWord.get()) return String.valueOf(fCount);
        else return null;
    }

    @Override
    public void onActivate() {
        if (!nWord.get() && !fWord.get()) {
            error("Enable at least one counter!");
            this.toggle();
        }
        nCount = 0;
        fCount = 0;
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        Text message = event.getMessage();
        if (nWord.get()) nCount += (int) nPattern.matcher(message.getString()).results().count();
        if (fWord.get()) fCount += (int) fPattern.matcher(message.getString()).results().count();
    }
}
