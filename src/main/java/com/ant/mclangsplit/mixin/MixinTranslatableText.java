package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.TranslationStorageExtension;
import com.ant.mclangsplit.config.ConfigHandler;
import com.google.common.collect.ImmutableList;
import net.minecraft.text.*;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(TranslatableText.class)
public abstract class MixinTranslatableText {
    private static final List<String> IGNORE_DUAL_TRANSLATION_KEYS = new ArrayList<>();
    static {
        IGNORE_DUAL_TRANSLATION_KEYS.add("translation.test.invalid");
        IGNORE_DUAL_TRANSLATION_KEYS.add("translation.test.invalid2");
        IGNORE_DUAL_TRANSLATION_KEYS.add("options.on.composed");
        IGNORE_DUAL_TRANSLATION_KEYS.add("options.off.composed");
    }

    @Final
    @Mutable
    @Shadow
    private String key;

    @Final
    @Mutable
    @Shadow
    private List<StringVisitable> translations;

    @Shadow
    private Language languageCache;

    @Shadow
    abstract void forEachPart(String translation, Consumer<StringVisitable> partsConsumer);

    @Inject(at = @At("HEAD"), method = "updateTranslations", cancellable = true)
    private void updateTranslations(CallbackInfo ci) {
        boolean fromTooltip = false;
        if (this.key.startsWith("TOOLTIP")) {
            fromTooltip = true;
            this.key = this.key.replace("TOOLTIP", "");
        }

        Language language = Language.getInstance();

        if (language != this.languageCache || ConfigHandler.Client.ENABLE_EXPERIMENTAL_FEATURES) {
            this.languageCache = language;
            String string = language.get(this.key);

            try {
                ImmutableList.Builder<StringVisitable> builder = ImmutableList.builder();
                Objects.requireNonNull(builder);
                if (TranslationStorageExtension.translationMode == TranslationStorageExtension.Mode.SHOW_ORIGINAL || TranslationStorageExtension.translationMode == TranslationStorageExtension.Mode.SHOW_BOTH || (ConfigHandler.Client.IGNORE_TOOLTIPS && fromTooltip)) {
                    this.forEachPart(string, builder::add);
                }
                if (TranslationStorageExtension.altTranslations != null && (TranslationStorageExtension.translationMode == TranslationStorageExtension.Mode.SHOW_ALTERNATE || TranslationStorageExtension.translationMode == TranslationStorageExtension.Mode.SHOW_BOTH) && !shouldExcludeKey(this.key) && !(ConfigHandler.Client.IGNORE_TOOLTIPS && fromTooltip)) {
                    if (TranslationStorageExtension.translationMode == TranslationStorageExtension.Mode.SHOW_BOTH) {
                        builder.add(StringVisitable.plain(" "));
                    }
                    this.forEachPart(language.get(TranslationStorageExtension.altTranslations.get(this.key)), builder::add);
                }
                this.translations = builder.build();
            } catch (TranslationException var4) {
                this.translations.clear();
                this.translations.add(StringVisitable.plain(string));
            }
        }

        ci.cancel();
    }

    private boolean shouldExcludeKey(String key) {
        if (IGNORE_DUAL_TRANSLATION_KEYS.contains(this.key)) {
            return true;
        }

        if (ConfigHandler.Client.INCLUDE_KEYS.contains(key)) {
            return false;
        }

        for (String s : ConfigHandler.Client.INCLUDE_KEYS) {
            if (s.endsWith("*")) {
                if (key.startsWith(s.substring(0, s.length() - 1))) {
                    return false;
                }
            }
        }

        if (ConfigHandler.Client.IGNORE_KEYS.contains(key)) {
            return true;
        }

        for (String s : ConfigHandler.Client.IGNORE_KEYS) {
            if (s.endsWith("*")) {
                if (key.startsWith(s.substring(0, s.length() - 1))) {
                    return true;
                }
            }
        }

        return false;
    }
}
