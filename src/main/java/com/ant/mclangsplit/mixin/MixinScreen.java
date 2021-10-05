package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.MCLangSplit;
import com.ant.mclangsplit.TranslationStorageExtension;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(at = @At("RETURN"), method = "getTooltipFromItem", cancellable = true)
    public void getTooltipFromItem(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> result = cir.getReturnValue();
        for (Text t : result) {
            modify(t);
        };
        cir.setReturnValue(result);
    }

    private void modify(Text t) {
        if (t instanceof TranslatableText) {
            ((MixinTranslatableTextAccessor)t).setKey("TOOLTIP"+((TranslatableText) t).getKey());
        }
        for (Text t2 : t.getSiblings()) {
            modify(t2);
        }
    }
}
