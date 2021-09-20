package com.ant.mclangsplit;

import com.ant.mclangsplit.config.ConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCLangSplit implements ClientModInitializer {
	public static final String MOD_NAME = "mclangsplit";
	public static final String MOD_IDENTIFIER = "mclangsplit:mclangsplit";

	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitializeClient() {
		if (isPhysicalClient()) {
			new ConfigHandler.Client();
		}
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
