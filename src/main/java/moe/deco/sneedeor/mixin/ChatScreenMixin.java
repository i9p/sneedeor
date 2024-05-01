package moe.deco.sneedeor.mixin;

import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
    @Shadow protected TextFieldWidget chatField;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int color = 0;
        if (chatField.getText().startsWith(Config.get().prefix.get())) color = new Color(145, 61, 226).getPacked();
        if (BaritoneUtils.IS_AVAILABLE && chatField.getText().startsWith(BaritoneUtils.getPrefix())) color = new Color(Color.PINK).getPacked();

        if (color != 0) {
            context.fill(1,  this.height - 15, this.width - 1, this.height - 14, color);
            context.fill(1,  this.height - 2, this.width - 1, this.height - 1, color);
            context.fill(1,  this.height - 14, 2, this.height - 2, color);
            context.fill(this.width - 2,  this.height - 14, this.width - 1, this.height - 2, color);
        }
    }
}
