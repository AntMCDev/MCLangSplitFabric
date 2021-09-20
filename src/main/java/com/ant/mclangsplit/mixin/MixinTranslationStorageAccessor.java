package com.ant.mclangsplit.mixin;

import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TranslationStorage.class)
public interface MixinTranslationStorageAccessor {
    @Final
    @Accessor
    Map<String, String> getTranslations();
}
