package com.ant.mclangsplit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class MixinControlsListWidgetKeyBindingEntry {
    @Final
    @Shadow
    private KeyBinding binding;
    @Final
    @Shadow
    private Text bindingName;
    @Final
    @Shadow
    private ButtonWidget editButton;
    @Final
    @Shadow
    private ButtonWidget resetButton;

    @Shadow(aliases = { "this$0", "field_2742" })
    private ControlsListWidget this$0;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) throws IllegalAccessException, NoSuchFieldException {
        KeybindsScreen controlsScreen = ((MixinControlsListWidgetAccessor)this$0).getParent();
        int maxNameWidth = ((MixinControlsListWidgetAccessor)this$0).getMaxKeyNameLength();

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        float width = minecraftClient.currentScreen.width / 2;
        int strWidth = minecraftClient.textRenderer.getWidth(bindingName);
        float scaleFactor = strWidth > width - 6 ? 1f / (strWidth / (width - 6f)) : 1f;

        boolean bl = controlsScreen.selectedKeyBinding == this.binding;
        TextRenderer var10000 = minecraftClient.textRenderer;
        Text var10002 = this.bindingName;
        float var10003 = (float) (x + 90 - maxNameWidth);
        int var10004 = y + entryHeight / 2;
        Objects.requireNonNull(minecraftClient.textRenderer);
        matrices.push();
        matrices.scale(scaleFactor, 1f, 1f);
        var10000.draw(matrices, var10002.copy(), 6f, (float) (var10004 - 9 / 2), 16777215);
        matrices.pop();
        this.resetButton.x = x + 190;
        this.resetButton.y = y;
        this.resetButton.active = !this.binding.isDefault();
        this.resetButton.render(matrices, mouseX, mouseY, tickDelta);
        this.editButton.x = x + 105;
        this.editButton.y = y;
        this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
        boolean bl2 = false;
        if (!this.binding.isUnbound()) {
            KeyBinding[] var13 = minecraftClient.options.allKeys;
            int var14 = var13.length;

            for (int var15 = 0; var15 < var14; ++var15) {
                KeyBinding keyBinding = var13[var15];
                if (keyBinding != this.binding && this.binding.equals(keyBinding)) {
                    bl2 = true;
                    break;
                }
            }
        }

        if (bl) {
            this.editButton.setMessage((Text.literal("> ")).append(this.editButton.getMessage().copy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));
        } else if (bl2) {
            this.editButton.setMessage(this.editButton.getMessage().copy().formatted(Formatting.RED));
        }

        this.editButton.render(matrices, mouseX, mouseY, tickDelta);

        ci.cancel();
    }
}
