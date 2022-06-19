package com.ant.mclangsplit.mixin;

import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TranslatableTextContent.class)
public interface MixinTranslatableTextContentAccessor {
    @Mutable
    @Accessor("key")
    void setKey(String key);
}
