package net.minecraft.src;

public class BiomeGenMoria extends BiomeGenBase
{
	public BiomeGenMoria() {
		super(255); // 255 is the biome ID; existing biomes are 0-10
		biomeList[255] = null; // Super constructor adds BiomeGenMoria here but this is not a real biome
	}

	@Override
	public int getSkyColorByTemp(float f) {
		return mod_Moria.cfgSkylight ? super.getSkyColorByTemp(f) : 0;
	}
}