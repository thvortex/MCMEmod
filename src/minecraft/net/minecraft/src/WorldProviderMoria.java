package net.minecraft.src;

public class WorldProviderMoria extends WorldProvider
{
	public WorldProviderMoria() {
		isNether = true; // Disables sky and clouds in RenderGlobal.java
	}

	// Return the background color used for fog and glClear() as function of celestial angle
	public Vec3D func_4096_a(float f, float f1) {
		return Vec3D.createVector(0, 0, 0);
	}
}
