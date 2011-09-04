package net.minecraft.src;

public class WorldProviderMoria extends WorldProvider
{
	public WorldProviderMoria() {
		isNether = true; // Disables sky and clouds in RenderGlobal.java
	}

	// Return the background color used for fog and glClear() as function of celestial angle
	@Override
	public Vec3D func_4096_a(float f, float f1) {
		return Vec3D.createVector(0, 0, 0);
	}

	// The sun's position affects the sky color returned by above function. Using 0.5 here makes it
	// the darkest possible. This also disables all skylight on the map, but that's not a problem for
	// underground locations like Moria.
	@Override
	public float calculateCelestialAngle(long l, float f) {
		return 0.5F;
	}
	
	@Override
	protected void generateLightBrightnessTable() {
		super.generateLightBrightnessTable();
		for(int i = 0; i <= 15; i++) {
			double exp = 1.0 / mod_Moria.cfgGamma;
			lightBrightnessTable[i] = (float) Math.pow(lightBrightnessTable[i], mod_Moria.cfgGamma);
		}
	}
}
