package moe.deco.sneedeor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.Script;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class EchoCommand extends Command {
    public EchoCommand() {
        super("echo", "Echoes back your input, starscript supported.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            String m = context.getArgument("message", String.class);
            Script script = MeteorStarscript.compile(m);

            if (script != null) {
                String message = MeteorStarscript.run(script);
                if (message != null) {
                    info(message);
                }
            }

            return SINGLE_SUCCESS;
        }));
    }
}
