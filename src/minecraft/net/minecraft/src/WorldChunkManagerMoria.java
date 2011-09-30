package net.minecraft.src;

public class WorldChunkManagerMoria extends WorldChunkManager
{
	public static BiomeGenBase moriaBiome = new BiomeGenMoria();

	public WorldChunkManagerMoria(World world) {
		super(world);
	}
	
	@Override
    public BiomeGenBase getBiomeGenAt(int i, int j)
    {
		return mod_Moria.cfgSkylight ? super.getBiomeGenAt(i, j) : moriaBiome;
    }
}
