package net.minecraft.src;

public class WorldProviderMoria extends WorldProvider
{
	// Return the background color used for fog and glClear() as function of celestial angle. The
	// returned color is a blend between real fog color and solid black when the player is looking out
	// of one of the Moria exits, and the color is solid black everywhere else in Moria.
	@Override
	public Vec3D getFogColor(float f, float f1) {
		Vec3D color = super.getFogColor(f, f1);
		double blend = mod_Moria.skyBlend;
		
		double red   = blend * color.xCoord;
		double green = blend * color.yCoord;
		double blue  = blend * color.zCoord;

		isNether = !mod_Moria.cfgSkylight && blend == 0.0; // isNether=true disables sky and clouds
		return mod_Moria.cfgSkylight ? color : Vec3D.createVector(red, green, blue);
	}

	// The sun's position affects the sky color returned by above function. Using 6000 for time-of-day
	// forces the sun's position to high noon, and makes the overridden skylight values provided by
	// NibbleArrayMoria show up properly as neutral white light (and not the blue hue of moonlight or
	// the red hue of torch light). When the player is looking out, we have to use real time-of-day
	// since it affects sky color.
	@Override
	public float calculateCelestialAngle(long l, float f) {
		if(mod_Moria.cfgSkylight || mod_Moria.skyBlend == 0.0) {
			l = 6000;
		}
		return super.calculateCelestialAngle(l, f);
	}
	
	// WorldChunkManagerMoria will return BiomeGenMoria biomes that give a black sky color
	@Override
	protected void registerWorldChunkManager() {
		worldChunkMgr = new WorldChunkManagerMoria(worldObj);
	}
}
