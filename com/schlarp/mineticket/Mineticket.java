package com.schlarp.mineticket;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Mineticket.ID, name = Mineticket.NAME, version = Mineticket.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class Mineticket {
	public final static String ID = "Mineticket";
	public final static String NAME = "Mineticket";
	public final static String VERSION = "2.0.2";

	@Instance(ID)
	public static Mineticket instance;

	public static String mineticketServer;
	public static String apiKey;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		mineticketServer = config
				.get("server", "mineticket_server", "http://mineticket.alienmc.co").getString();
		apiKey = config.get("server", "api_key", "").getString();

		config.save();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new MineticketCommand());
		GameRegistry.registerPlayerTracker(new MineticketPlayer());
	}
}