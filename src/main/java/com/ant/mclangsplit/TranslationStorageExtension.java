package com.ant.mclangsplit;

import net.minecraft.client.resource.language.TranslationStorage;

public class TranslationStorageExtension {
    public enum Mode {
        SHOW_ORIGINAL, SHOW_ALTERNATE, SHOW_BOTH
    }

    public static TranslationStorage altTranslations = null;
    public static Mode translationMode = Mode.SHOW_BOTH;

    public static void nextMode() {
        switch (translationMode) {
            case SHOW_BOTH:
                translationMode = Mode.SHOW_ORIGINAL;
                break;
            case SHOW_ORIGINAL:
                translationMode = Mode.SHOW_ALTERNATE;
                break;
            case SHOW_ALTERNATE:
                translationMode = Mode.SHOW_BOTH;
                break;
        }
    }
}
