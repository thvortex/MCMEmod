package net.minecraft.src;

import java.util.List;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.minecraft.client.Minecraft;

public class mod_Lothlorien extends BaseMod
{
	public static String SERVER = "mcme";
	public static int[] lothlorienX = { 5987, 5712, 5606, 5564, 5539, 5594,
		5682, 5670, 5718, 5707, 5916, 6017, 6250, 6387, 6752, 6847, 6677,
		6608, 6593, 6579, 6557, 6545, 6501, 6501, 6442, 6429, 6432, 6397,
		6253, 6195, 6072
	};
	public static int[] lothlorienZ = { -12417, -12421, -12504, -12576, -13154, -13375,
		-13526, -13556, -13637, -13745, -14090, -14171, -14238, -14217, -14225, -13944, -13785,
		-13675, -13655, -13609, -13595, -13561, -13535, -13497, -13414, -13325, -13270, -13026,
		-12676, -12587, -12445
	};
	
	public static Polygon lothlorienBorder = new Polygon(lothlorienX, lothlorienZ, 31);
	public static Rectangle lothlorienBorderBB = lothlorienBorder.getBounds();

	public TexturePackBase middleEarthPack;
	public TexturePackBase lothlorienPack;
	public int playerX, playerZ;

    public mod_Lothlorien()
    {
		Minecraft mc = ModLoader.getMinecraftInstance();
		
		// Locate the normal and Lothlorien texture packs by the first part of their filenames
		for(Object x : mc.texturePackList.availableTexturePacks()) {
			TexturePackBase pack = (TexturePackBase) x;
			
			if(pack.texturePackFileName.toLowerCase().startsWith("middle earth beta texture pack")) {
				middleEarthPack = pack;
			}
			if(pack.texturePackFileName.toLowerCase().startsWith("middle earth lothlorien beta")) {
				lothlorienPack = pack;
			}
		}
		
		// If both texture packs exist, then activate the plugin
		if(middleEarthPack != null && lothlorienPack != null) {
			ModLoader.SetInGameHook(this, true, true);
		} else {
			ModLoader.getLogger().warning("Cannot find MCME texture packs. Texture switch mod disabled.");
		}
    }
	
	@Override
	public boolean OnTickInGame(Minecraft mc) {

		Entity player = mc.thePlayer;		
		int x = (int) player.posX;
		int z = (int) player.posZ;
				
		// Only re-check bounds every whole integer position to reduce CPU load
		if(x == playerX && z == playerZ) {
			return true;
		}
		playerX = x;
		playerZ = z;

		// Perform cheaper bounding box check before full polygon check
		TexturePackBase desiredPack;
		if(lothlorienBorderBB.contains(x, z) && lothlorienBorder.contains(x, z)) {
			desiredPack = lothlorienPack;
		} else {
			desiredPack = middleEarthPack;
		}
		
		// Switch to the desired texture pack if it's not currently selected
		if(desiredPack != mc.texturePackList.selectedTexturePack) {
			mc.texturePackList.setTexturePack(desiredPack);
			mc.renderEngine.refreshTextures();
		}
		
		return true;
	}

    public String Version()
    {
        return "1.7.3-0.1";
    }
}