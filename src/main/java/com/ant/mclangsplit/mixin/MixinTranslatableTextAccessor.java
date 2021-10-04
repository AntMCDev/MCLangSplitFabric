package com.ant.mclangsplit.mixin;

import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TranslatableText.class)
public interface MixinTranslatableTextAccessor {
    @Mutable
    @Accessor("key")
    void setKey(String key);
}
