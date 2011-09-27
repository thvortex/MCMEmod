package net.minecraft.src;

import java.util.List;
import java.net.Socket;
import java.net.InetAddress;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.minecraft.client.Minecraft;

public class mod_Moria extends BaseMod
{
	public static String SERVER = "176.9.10.227";
	
	// TODO: This should be a more exact polygon
	public static int[] moriaX = { 3810, 3810, 5531, 5531 };
	public static int[] moriaZ = { -6957, -11662, -11662, -6957 };
	
	public static Polygon moriaBorder = new Polygon(moriaX, moriaZ, moriaX.length);
	public static Rectangle moriaBorderBB = moriaBorder.getBounds();
	
	@MLProp(name = "gamma", info = "brightness adjustment: =1.0 no change, <1.0 darker, >1.0 lighter")
	public static double cfgGamma = 1;
	@MLProp(name = "skylight", info = "if true, enable skylight and dark blue background color")
	public static boolean cfgSkylight = false;

	public int playerX, playerZ;
	public boolean inGui = false;
	public boolean inMoria = false;
	
	public mod_Moria() {
		ModLoader.SetInGUIHook(this, true, true);
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
				return false; // Don't run in game hook again until rescheduled by GUI hook
			}

			try {
				if(!isMCMEServer((EntityClientPlayerMP) player)) {
					return false; // Don't run in game hook again until rescheduled by GUI hook
				}
			}
			catch(NoSuchFieldException e) {
				// Disable mod by cancelling all further GUI and in game ticks
				ModLoader.getLogger().severe("Cannot get private field: " + e);
				ModLoader.SetInGUIHook(this, false, true);
				return false;
			}			
		}

		int x = (int) player.posX;
		int z = (int) player.posZ;

		// Only re-check bounds every whole integer position to reduce CPU load
		if(x == playerX && z == playerZ) {
			return true; // Keep game hook running
		}
		playerX = x;
		playerZ = z;

		// Perform cheaper bounding box check before full polygon check
		boolean inMoriaNow;
		if(moriaBorderBB.contains(x, z) && moriaBorder.contains(x, z)) {
			inMoriaNow = true;
		} else {
			inMoriaNow = false;
		}
		
		// Switch WorldProviders when entering or leaving Moria
		if(inMoriaNow != inMoria) {
			WorldProvider provider;

			if(inMoriaNow) {
				provider = new WorldProviderMoria();
			} else {
				int dimension = mc.theWorld.worldInfo.getDimension();
				provider = WorldProvider.getProviderForDimension(dimension);
			}
			provider.registerWorld(mc.theWorld);

			try {
				// MCP World.worldProvider is World.y in minecraft.jar
				ModLoader.setPrivateValue(World.class, mc.theWorld, "y", provider);
			}
			catch(NoSuchFieldException e) {
				// Disable mod by cancelling all further GUI and in game ticks
				ModLoader.getLogger().severe("Cannot set World.worldProvider: " + e);
				ModLoader.SetInGUIHook(this, false, true);
				return false;
			}
		}
		inMoria = inMoriaNow;
		
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

		InetAddress address = socket.getInetAddress();
		return address != null && address.getHostAddress().equals(SERVER);
	}
	
	public String Version() {
		return "1.8.1-0.3";
	}
}
