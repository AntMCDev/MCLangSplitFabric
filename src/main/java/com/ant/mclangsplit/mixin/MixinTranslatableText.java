package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.TranslationStorageExtension;
import com.ant.mclangsplit.config.ConfigHandler;
import net.minecraft.text.*;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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
    @Shadow
    private String key;

    @Final
    @Shadow
    private List<StringVisitable> translations;

    @Shadow
    private Language languageCache;

    @Shadow
    abstract void setTranslation(String translation);

    @Inject(at = @At("HEAD"), method = "updateTranslations", cancellable = true)
    private void updateTranslations(CallbackInfo ci) {
        boolean fromTooltip = false;
        if (this.key.startsWith("TOOLTIP")) {
            fromTooltip = true;
            this.key = this.key.replace("TOOLTIP", "");
        }

        Language language = Language.getInstance();
        if (language != this.languageCache) {
            this.languageCache = language;
            this.translations.clear();
            String string = language.get(this.key);

            try {
                this.setTranslation(string);
                if (TranslationStorageExtension.altTranslations != null && !shouldExcludeKey(this.key) && !(ConfigHandler.Client.IGNORE_TOOLTIPS && fromTooltip)) {
                    System.out.println(this.key);
                    this.translations.add(StringVisitable.plain(" "));
                    this.setTranslation(language.get(TranslationStorageExtension.altTranslations.get(this.key)));
                }
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
