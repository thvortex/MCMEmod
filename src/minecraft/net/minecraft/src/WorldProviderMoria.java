package net.minecraft.src;

public class WorldProviderMoria extends WorldProvider
{
	public WorldProviderMoria() {
		isNether = !mod_Moria.cfgSkylight; // Disables sky and clouds in RenderGlobal.java
	}

	// Return the background color used for fog and glClear() as function of celestial angle
	@Override
	public Vec3D func_4096_a(float f, float f1) {
		return mod_Moria.cfgSkylight ? super.func_4096_a(f, f1) : Vec3D.createVector(0, 0, 0);
	}

	// The sun's position affects the sky color returned by above function. Using 6000 for time-of-day
	// forces the sun's position to high noon, and makes the overridden skylight values provided by
	// NibbleArrayMoria show up properly as neutral white light (and not the blue hue of moonlight or
	// the red hue of torch light).
	@Override
	public float calculateCelestialAngle(long l, float f) {
		return super.calculateCelestialAngle(6000, f);
	}
	
	// WorldChunkManagerMoria will return BiomeGenMoria biomes that give a black sky color
	@Override
	protected void registerWorldChunkManager()
	{
		worldChunkMgr = new WorldChunkManagerMoria(worldObj);
	}
}
