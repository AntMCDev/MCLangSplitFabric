package com.ant.mclangsplit;

import com.ant.mclangsplit.config.ConfigHandler;
import com.ant.mclangsplit.mixin.MixinKeyBindingAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class MCLangSplit implements ClientModInitializer {
	public static final String MOD_NAME = "mclangsplit";
	public static final String MOD_IDENTIFIER = "mclangsplit:mclangsplit";

	public static final Logger LOGGER = LogManager.getLogger();
	public static KeyBinding keyBinding;

	@Override
	public void onInitializeClient() {
		if (isPhysicalClient()) {
			new ConfigHandler.Client();
		}

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mclangsplit.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category.mclangsplit"));

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenKeyboardEvents.allowKeyPress(screen).register((screen2, key, scancodes, modifiers) -> {
				return true;
			});

			ScreenKeyboardEvents.beforeKeyPress(screen).register((screen2, key, scancodes, modifiers) -> {
				if (keyBinding.matchesKey(key, scancodes)) {
					keyBinding.setPressed(true);
					((MixinKeyBindingAccessor) keyBinding).setTimesPressed(((MixinKeyBindingAccessor) keyBinding).getTimesPressed() + 1);
				}
			});

			ScreenKeyboardEvents.beforeKeyRelease(screen).register((screen2, key, scancodes, modifiers) -> {
				if (keyBinding.matchesKey(key, scancodes)) {
					keyBinding.setPressed(false);
				}
			});

			ScreenEvents.beforeTick(screen).register(screen2 -> {
				boolean b = false;
				while (keyBinding.wasPressed()) {
					b = true;
				}

				if (b) {
					TranslationStorageExtension.nextMode();
				}
			});
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean b = false;
			while (keyBinding.wasPressed()) {
				b = true;
			}

			if (b) {
				TranslationStorageExtension.nextMode();
			}
		});
	}

	public static boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static boolean isLogicalClient(World world) {
		return world.isClient;
	}

	public static boolean isPhysicalServer() {
		return !isPhysicalClient();
	}

	public static boolean isLogicalServer(World world) {
		return !isLogicalClient(world);
	}

	public static void printToChat(String msg) {
		if (isPhysicalClient()) {
			MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText(msg), MinecraftClient.getInstance().player.getUuid());
		}
	}
}
