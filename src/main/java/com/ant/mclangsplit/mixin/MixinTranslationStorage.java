package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.MCLangSplit;
import com.ant.mclangsplit.config.ConfigHandler;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.resource.metadata.LanguageResourceMetadata;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

@Mixin(TranslationStorage.class)
public abstract class MixinTranslationStorage {
    private static final List<String> IGNORE_DUAL_TRANSLATION_KEYS = new ArrayList<>();
    static {
        IGNORE_DUAL_TRANSLATION_KEYS.add("translation.test.invalid");
        IGNORE_DUAL_TRANSLATION_KEYS.add("translation.test.invalid2");
        IGNORE_DUAL_TRANSLATION_KEYS.add("options.on.composed");
        IGNORE_DUAL_TRANSLATION_KEYS.add("options.off.composed");
    }

    @Inject(at = @At("RETURN"), method = "load", cancellable = true)
    private static void load(ResourceManager resourceManager, List<LanguageDefinition> definitions, CallbackInfoReturnable<TranslationStorage> cir) {
        Map<String, String> map = Maps.newHashMap();
        Map<String, String> map1 = ((MixinTranslationStorageAccessor)cir.getReturnValue()).getTranslations();
        Map<String, String> map2 = Maps.newHashMap();
        boolean bl = false;
        Iterator var4 = definitions.iterator();

        while(var4.hasNext()) {
            LanguageDefinition languageDefinition = (LanguageDefinition)var4.next();
            bl |= languageDefinition.isRightToLeft();
            String string = String.format("lang/%s.json", languageDefinition.getCode());
            Iterator var7 = resourceManager.getAllNamespaces().iterator();

            while(var7.hasNext()) {
                String string2 = (String)var7.next();

                try {
                    Identifier identifier = new Identifier(string2, string);
                    load((List)resourceManager.getAllResources(identifier), (Map)map1);
                } catch (FileNotFoundException var10) {
                } catch (Exception var11) {
                    MCLangSplit.LOGGER.warn("Skipped language file: {}:{} ({})", string2, string, var11.toString());
                }
            }
        }

        var4 = definitions.iterator();
        boolean found = false;
        while (var4.hasNext()) {
            LanguageDefinition languageDefinition = (LanguageDefinition)var4.next();
            if (languageDefinition.getCode().equals(ConfigHandler.Client.SECOND_LANGUAGE)) {
                found = true;
            }
        }

        if (!found) {
            Map<String, LanguageDefinition> langMap = loadAvailableLanguages(resourceManager.streamResourcePacks());
            if (langMap.containsKey(ConfigHandler.Client.SECOND_LANGUAGE)) {
                LanguageDefinition language = langMap.get(ConfigHandler.Client.SECOND_LANGUAGE);
                bl |= language.isRightToLeft();
                String string = String.format("lang/%s.json", language.getCode());

                Iterator var7 = resourceManager.getAllNamespaces().iterator();
                while(var7.hasNext()) {
                    String string2 = (String)var7.next();

                    try {
                        Identifier identifier = new Identifier(string2, string);
                        load((List)resourceManager.getAllResources(identifier), (Map)map2);
                    } catch (FileNotFoundException var10) {
                    } catch (Exception var11) {
                        MCLangSplit.LOGGER.warn("Skipped language file: {}:{} ({})", string2, string, var11.toString());
                    }
                }
            }
        }

        for (String s : map1.keySet()) {
            String str = map1.get(s);
            if (!ConfigHandler.Client.IGNORE_KEYS.contains(s) && !IGNORE_DUAL_TRANSLATION_KEYS.contains(s) && map2.containsKey(s) && !specialEquals(map1.get(s), map2.get(s))) {
                String s1 = map2.get(s);
                if (s1.contains("%s") || s1.contains("$s")) {
                    int i = 1;
                    String tmp = str;
                    while (tmp.contains("%s")) {
                        int tmpi = tmp.indexOf("%s");
                        tmp = tmp.substring(0, tmpi) + "%" + i++ + "$s" + tmp.substring(tmpi + 2);
                    }
                    List<String> mappingList = new ArrayList<>();
                    while (tmp.contains("$s")) {
                        int tmpi = tmp.indexOf("$s");
                        mappingList.add(tmp.substring(tmpi-2, tmpi+2));
                        tmp = tmp.substring(tmpi+2);
                    }
                    i = 0;
                    while (s1.contains("%s")) {
                        int index = s1.indexOf("%s");
                        try {
                            s1 = s1.substring(0, index) + mappingList.get(i++) + s1.substring(index + 2);
                        } catch (IndexOutOfBoundsException ex) {
                            MCLangSplit.LOGGER.error(ex.getMessage() + "; " + str + " " + s1);
                        }
                    }
                }
                str += " " + s1;
            }
            map.put(s, str);
        }

        try {
            Constructor<?> constructor = TranslationStorage.class.getDeclaredConstructor(Map.class, Boolean.TYPE);
            if (Modifier.isPrivate(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }
            cir.setReturnValue((TranslationStorage) constructor.newInstance(ImmutableMap.copyOf(map), bl));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            MCLangSplit.LOGGER.error("Could not access constructor in ClientLanguageMap injection", ex);
        }
    }

    private static void load(List<Resource> resources, Map<String, String> translationMap) {
        Iterator var2 = resources.iterator();

        while(var2.hasNext()) {
            Resource resource = (Resource)var2.next();

            try {
                InputStream inputStream = resource.getInputStream();

                try {
                    Objects.requireNonNull(translationMap);
                    Language.load(inputStream, translationMap::put);
                } catch (Throwable var8) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException var9) {
                MCLangSplit.LOGGER.warn("Failed to load translations from {}", resource, var9);
            }
        }

    }

    private static Map<String, LanguageDefinition> loadAvailableLanguages(Stream<ResourcePack> packs) {
        Map<String, LanguageDefinition> map = Maps.newHashMap();
        packs.forEach((pack) -> {
            try {
                LanguageResourceMetadata languageResourceMetadata = (LanguageResourceMetadata)pack.parseMetadata(LanguageResourceMetadata.READER);
                if (languageResourceMetadata != null) {
                    Iterator var3 = languageResourceMetadata.getLanguageDefinitions().iterator();

                    while(var3.hasNext()) {
                        LanguageDefinition languageDefinition = (LanguageDefinition)var3.next();
                        map.putIfAbsent(languageDefinition.getCode(), languageDefinition);
                    }
                }
            } catch (IOException | RuntimeException var5) {
                MCLangSplit.LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", pack.getName(), var5);
            }

        });
        return ImmutableMap.copyOf(map);
    }

    private static final Map<String, String> SPECIAL_REPLACE = new HashMap<>();
    static {
        SPECIAL_REPLACE.put("\uFF1A", ": ");
    }

    private static boolean specialEquals(String s1, String s2) {
        for (String s : SPECIAL_REPLACE.keySet()) {
            String sr = SPECIAL_REPLACE.get(s);
            s1 = s1.replace(s, sr);
            s2 = s2.replace(s, sr);
        }
        return s1.equals(s2);
    }
}
