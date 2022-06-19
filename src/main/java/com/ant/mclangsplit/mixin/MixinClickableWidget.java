package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.config.ConfigHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickableWidget.class)
public abstract class MixinClickableWidget extends DrawableHelper implements Drawable, Element, Selectable {
    @Shadow
    abstract int getYImage(boolean hovered);
    @Shadow
    abstract void renderBackground(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY);
    @Shadow
    abstract Text getMessage();
    @Shadow
    abstract boolean isHovered();
    @Shadow
    protected int width;
    @Shadow
    protected int height;
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    protected float alpha = 1.0F;
    @Shadow
    public boolean active = true;
    @Shadow
    public boolean visible = true;

    @Inject(at = @At("HEAD"), method = "renderButton", cancellable = true)
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBackground(matrices, minecraftClient, mouseX, mouseY);
        int j = this.active ? 16777215 : 10526880;

        Text buttonText = this.getMessage();
        int strWidth = minecraftClient.textRenderer.getWidth(buttonText);
        float scaleFactor = strWidth > width - 6 ? 1f / (strWidth / (width - 6f)) : 1f;

        matrices.push();
        matrices.scale(scaleFactor, 1f, 1f);
        drawCenteredText(matrices, textRenderer, this.getMessage().copy(), (int)(((float)this.x + (float)this.width / 2f) * (1f / scaleFactor)), this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        matrices.pop();

        ci.cancel();
    }
}
