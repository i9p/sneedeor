package moe.deco.sneedeor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class StarscriptCommand extends Command {
    public StarscriptCommand() {
        super("starscript", "Returns a Starscript formatted string");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("format", StringArgumentType.greedyString()).executes(context -> {
            String format = context.getArgument("format", String.class);
            Parser.Result result = Parser.parse(format);

            if (result.hasErrors()) {
                error("%s", result.errors.get(0).toString());
            }
            else info(MeteorStarscript.run(Compiler.compile(result)));
            return SINGLE_SUCCESS;
        }));
    }
}
