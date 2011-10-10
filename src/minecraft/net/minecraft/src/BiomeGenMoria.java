package net.minecraft.src;

import java.awt.Color;

public class BiomeGenMoria extends BiomeGenBase
{
	public BiomeGenMoria() {
		super(255); // 255 is the biome ID; existing biomes are 0-10
		biomeList[255] = null; // Super constructor adds BiomeGenMoria here but this is not a real biome
		setBiomeName("Moria"); // The MCME mod pack has an info screen that shows biome name
	}

	// Returned color is a blend between real sky color and solid black when the player is looking out
	// of one of the Moria exits, and the color is solid black everywhere else in Moria.
	@Override
	public int getSkyColorByTemp(float f) {
		Color oldColor = new Color(super.getSkyColorByTemp(f));
		double blend = mod_Moria.skyBlend;
		
		float[] colors = oldColor.getComponents(null); 
		colors[0] = (float) blend * colors[0];
		colors[1] = (float) blend * colors[1];
		colors[2] = (float) blend * colors[2];
		Color newColor = new Color(colors[0], colors[1], colors[2]);

		return mod_Moria.cfgSkylight ? oldColor.getRGB() : newColor.getRGB();
	}
}
