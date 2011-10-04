package net.minecraft.src;

import java.util.List;
import java.net.Socket;
import java.net.InetAddress;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.minecraft.client.Minecraft;

public class mod_Moria extends BaseMod
{
	public static String[] SERVERS = { "176.9.10.227", "88.198.12.168" };
	
	// TODO: This should be a more exact polygon
	public static int[] moriaX = { 3810, 3810, 5531, 5531 };
	public static int[] moriaZ = { -6957, -11662, -11662, -6957 };
	
	public static Polygon moriaBorder = new Polygon(moriaX, moriaZ, moriaX.length);
	public static Rectangle moriaBorderBB = moriaBorder.getBounds();
	
	@MLProp(name = "ambient", info = "brightness adjustment: 0 for no change", min = 0, max = 15)
	public static int ambient = 0;
	@MLProp(name = "skylight", info = "if true, enable normal sky/fog colors")
	public static boolean cfgSkylight = false;

	public int playerX, playerZ;
	public boolean inGui = false;
	public static int inMoria = 0;
	
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

			// Permanently install ChunkManagerMoria in the MCME world if not already installed.
			// This class will enforce an ambient light level on every block while in Moria.
			IChunkProvider chunkProvider = mc.theWorld.chunkProvider;
			if(chunkProvider != null && !(chunkProvider instanceof ChunkProviderMoria)) {
				ModLoader.getLogger().fine("MCME server detected; Moria mod activated");
				mc.theWorld.chunkProvider = new ChunkProviderMoria(chunkProvider);
				
				// When first connecting to MCME, force a check of the player's coordinates. Also
				// force a switch of WorldProviders if we are already in Moria on server login.
				playerX = playerZ = inMoria = -1;
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
		int inMoriaNow;
		if(moriaBorderBB.contains(x, z) && moriaBorder.contains(x, z)) {
			inMoriaNow = 1;
		} else {
			inMoriaNow = 0;
		}
		
		if(inMoriaNow != inMoria) {
			WorldProvider provider;

			// Switch WorldProviders when entering or leaving Moria
			if(inMoriaNow == 1) {
				provider = new WorldProviderMoria();
			} else {
				int dimension = mc.theWorld.worldInfo.getDimension();
				provider = WorldProvider.getProviderForDimension(dimension);
			}
			provider.registerWorld(mc.theWorld);
						
			try {
				// MCP World.worldProvider is World.y in minecraft.jar
				ModLoader.setPrivateValue(World.class, mc.theWorld, "y", provider);				

				// Force WorldRenderers to update and use changed skylight provided by NibbleArrayMoria
				updateWorldRenderers(mc);
			}
			catch(NoSuchFieldException e) {
				// Disable mod by cancelling all further GUI and in game ticks
				ModLoader.getLogger().severe("Cannot access private field: " + e);
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
	
	public void updateWorldRenderers(Minecraft mc) throws NoSuchFieldException {
		// MCP RenderGlobal.worldRenderersToUpdate is "k" in obfuscated code
		List<WorldRenderer> toUpdate = (List) ModLoader.getPrivateValue(RenderGlobal.class, mc.renderGlobal, "k");

		// MCP RenderGlobal.worldRenderers is "m" in obfuscated code
		WorldRenderer[] rendererArray = (WorldRenderer[]) ModLoader.getPrivateValue(RenderGlobal.class, mc.renderGlobal, "m");
		
		// Force all chunks to re-render and use updated skylight values
		for(int i = 0; i < rendererArray.length; i++) {
			WorldRenderer renderer = rendererArray[i];
			if(!renderer.needsUpdate) {
				renderer.needsUpdate = true;
				toUpdate.add(renderer);
			}
		}
	}
	
	public String Version() {
		return "1.8.1-0.4";
	}
}
