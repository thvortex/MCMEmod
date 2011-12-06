package net.minecraft.src;

import java.util.List;
import java.net.Socket;
import java.net.InetAddress;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.minecraft.client.Minecraft;

public class mod_Lothlorien extends BaseMod
{
	public static String[] SERVERS = { "176.9.10.227", "88.198.12.168" };
	
	public static int[] lothlorienX = { 6036, 5876, 5693, 5527, 5382, 5329,
		5352, 5491, 5524, 5969, 6105, 6429, 6920, 6990, 7012, 7046, 6988,
		6653, 6654, 6363, 6167
	};

	public static int[] lothlorienZ = { -12230, -12209, -12230, -12238, -12323, -12434,
		-13164, -13749, -13960, -14494, -14497, -14442, -14426, -14343, -14222, -13865, -13713,
		-13137, -12974, -12305, -12217
	};
	
	public static Polygon lothlorienBorder = new Polygon(lothlorienX, lothlorienZ, lothlorienX.length);
	public static Rectangle lothlorienBorderBB = lothlorienBorder.getBounds();

	public TexturePackBase middleEarthPack;
	public TexturePackBase lothlorienPack;
	public int playerX, playerZ;
	public boolean inGui = false;
	public static int inLothlorien = -1;

	@Override
	public void load()
	{
		Minecraft mc = ModLoader.getMinecraftInstance();
		
		// Locate the normal and Lothlorien texture packs by the first part of their filenames
		for(Object x : mc.texturePackList.availableTexturePacks()) {
			TexturePackBase pack = (TexturePackBase) x;
			String packFile = pack.texturePackFileName.toLowerCase();
			
			// If there are multiple versions of each texture pack, pick the one with the largest
			// version number which comes last alphabetically.
			if(packFile.startsWith("middle earth beta texture pack")) {
				if(middleEarthPack == null || packFile.compareToIgnoreCase(middleEarthPack.texturePackFileName) > 0) {
					middleEarthPack = pack;
				}
			}
			if(packFile.startsWith("middle earth lothlorien beta")) {
				if(lothlorienPack == null || packFile.compareToIgnoreCase(lothlorienPack.texturePackFileName) > 0) {
					lothlorienPack = pack;
				}
			}
		}
		
		// If both texture packs exist, then activate the plugin
		if(middleEarthPack != null && lothlorienPack != null) {
			ModLoader.SetInGUIHook(this, true, true);
		} else {
			ModLoader.getLogger().warning("Cannot find MCME texture packs. Texture switch mod disabled.");
		}
	}
	
	@Override
	public boolean OnTickInGame(float tick, Minecraft mc) {
		Entity player = mc.thePlayer;

		// If the GUI was just closed (like after connecting to a server from the main menu),
		// check if we're connected to the MCME server.
		if(inGui) {
			inGui = false;
			ModLoader.SetInGUIHook(this, true, true);

			if(!(player instanceof EntityClientPlayerMP)) {
				inLothlorien = -1; // Force texture switch on server reconnect
				return false; // Don't run in game hook again until rescheduled by GUI hook
			}			
			
			try {
				if(!isMCMEServer((EntityClientPlayerMP) player)) {
					inLothlorien = -1; // Force texture pack switch on server reconnect
					return false; // Don't run in game hook again until rescheduled by GUI hook
				}
			}
			catch(NoSuchFieldException e) {
				// Disable mod by cancelling all further GUI and in game ticks
				ModLoader.getLogger().severe("Cannot get private field: " + e);
				ModLoader.SetInGUIHook(this, false, true);
				return false;
			}
			
			if(inLothlorien == -1) {
				ModLoader.getLogger().fine("MCME server detected; Lothlorien mod activated");
			}
		}

		int x = (int) player.posX;
		int z = (int) player.posZ;

		// Only re-check bounds every whole integer position to reduce CPU load
		if(x == playerX && z == playerZ && inLothlorien != -1) {
			return true; // Keep game hook running
		}
		playerX = x;
		playerZ = z;

		// Perform cheaper bounding box check before full polygon check
		int inLothlorienNow;
		if(lothlorienBorderBB.contains(x, z) && lothlorienBorder.contains(x, z)) {
			inLothlorienNow = 1;
		} else {
			inLothlorienNow = 0;
		}

		// Only perform texture switch when crossing the Lothlorien boundry or when
		// first connected to the MCME server.
		if(inLothlorien != inLothlorienNow) {
			TexturePackBase desiredPack = inLothlorienNow == 1 ? lothlorienPack : middleEarthPack;
			
			// Switch to the desired texture pack if it's not currently selected
			if(desiredPack != mc.texturePackList.selectedTexturePack) {
				mc.texturePackList.setTexturePack(desiredPack);
				mc.renderEngine.refreshTextures();
			}
			
			inLothlorien = inLothlorienNow;
		}
		
		return true; // Keep game hook running
	}

	@Override
	public boolean OnTickInGUI(float tick, Minecraft mc, GuiScreen screen) {
		// Re-enable in game hook each time a GUI screen is entered. The game hook will
		// re-check if we're connected to the MCME server once the GUI screen closes.
		inGui = true;
		ModLoader.SetInGameHook(this, true, true);
		
		return false; // Don't run GUI hook again until re-enabled by game hook
	}
	
	public boolean isMCMEServer(EntityClientPlayerMP player) throws NoSuchFieldException {
		// NetClientHandler.netManager in MCP is "g" in obfuscated code
		NetworkManager manager = (NetworkManager) ModLoader.getPrivateValue(NetClientHandler.class, player.sendQueue, "g");
		
		// NetworkManager.networkSocket in MCP is "h" in obfuscated code
		Socket socket = (Socket) ModLoader.getPrivateValue(NetworkManager.class, manager, "h");

		// Returned address can be null if socket is no longer connected
		InetAddress address = socket.getInetAddress();
		if(address != null) {
			String hostAddress = address.getHostAddress();
			for(int i = 0; i < SERVERS.length; i++) {
				if(hostAddress.equals(SERVERS[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-0.7";
	}
}
