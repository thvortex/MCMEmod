package net.minecraft.src;

import java.util.List;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.minecraft.client.Minecraft;

public class mod_Moria extends BaseMod
{
	public static String SERVER = "176.9.10.227";
	
	public static int[] moriaX = { 6036, 5876, 5693, 5527, 5382, 5329,
		5352, 5491, 5524, 5969, 6105, 6429, 6920, 6990, 7012, 7046, 6988,
		6653, 6654, 6363, 6167
	};

	public static int[] moriaZ = { -12230, -12209, -12230, -12238, -12323, -12434,
		-13164, -13749, -13960, -14494, -14497, -14442, -14426, -14343, -14222, -13865, -13713,
		-13137, -12974, -12305, -12217
	};
	
	public static Polygon moriaBorder = new Polygon(moriaX, moriaZ, moriaX.length);
	public static Rectangle moriaBorderBB = moriaBorder.getBounds();
	
	@MLProp(name = "gamma", info = "brightness adjustment: =1.0 no change, <1.0 darker, >1.0 lighter")
	public static double cfgGamma = 1;

	public int playerX, playerZ;
	public boolean inGui = false;
	public boolean inMoria = false;
	
	public mod_Moria() {
		ModLoader.SetInGUIHook(this, true, true);
	}
	
	@Override
	public boolean OnTickInGame(Minecraft mc) {
		Entity player = mc.thePlayer;

		// If the GUI was just closed (like after connecting to a server from the main menu),
		// check if we're connected to the MCME server.
		if(inGui) {
			inGui = false;
			ModLoader.SetInGUIHook(this, true, true);

			if(!(player instanceof EntityClientPlayerMP) || !mc.gameSettings.lastServer.startsWith(SERVER)) {
				return false; // Don't run in game hook again until rescheduled by GUI hook
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
				// MCP World.worldProvider is World.t in minecraft.jar
				ModLoader.setPrivateValue(World.class, mc.theWorld, "t", provider);
			}
			catch(NoSuchFieldException e) {
				// Disable mod by cancelling all further GUI and in game ticks
				ModLoader.getLogger().severe("Cannot set World.worldProvider: " + e);
				ModLoader.SetInGUIHook(this, false, true);
				return false; // No more game ticks
			}
		}
		inMoria = inMoriaNow;
		
		return true; // Keep game hook running
	}

	@Override
	public boolean OnTickInGUI(Minecraft mc, GuiScreen screen) {
		// Re-enable in game hook each time a GUI screen is entered. The game hook will
		// re-check if we're connected to the MCME server once the GUI screen closes.
		inGui = true;
		ModLoader.SetInGameHook(this, true, true);

		return false; // Don't run GUI hook again until re-enabled by game hook
	}
	
	public String Version() {
		return "1.7.3-0.1";
	}
}
