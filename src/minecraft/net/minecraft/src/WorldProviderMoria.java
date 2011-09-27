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

	// The sun's position affects the sky color returned by above function. Using 0.5 here makes it
	// the darkest possible. This also disables all skylight on the map, which is a problem for
	// certain locations in Moria that have light shafts in the ceiling. If the "skylight" config
	// option is true, using 6000 for time-of-day forces the sun's position to high noon, and will
	// enable full skylight (even if the server's time is night) along with a blue/gray background color.
	@Override
	public float calculateCelestialAngle(long l, float f) {
		return mod_Moria.cfgSkylight ? super.calculateCelestialAngle(6000, f) : 0.5F;
	}
	
	@Override
	protected void generateLightBrightnessTable() {
		super.generateLightBrightnessTable();
		lightBrightnessTable[0] = 0.05F; // Minecraft 1.8 made this 0 which breaks gamma for darkest blocks
		for(int i = 0; i <= 15; i++) {
			double exp = 1.0 / mod_Moria.cfgGamma;
			lightBrightnessTable[i] = (float) Math.pow(lightBrightnessTable[i], exp);
		}
	}
}
