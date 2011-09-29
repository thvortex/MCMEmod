package net.minecraft.src;

public class NibbleArrayMoria extends NibbleArray
{
	public NibbleArrayMoria(NibbleArray array) {
		super(array.data, 7); // The 7 hardcodes this for 128 map height
	}
	
	@Override
    public int getNibble(int i, int j, int k) {
		int value = super.getNibble(i, j, k);
		
		// If in Moria, don't return light levels lower than ambient
		if(mod_Moria.inMoria == 1 && value < mod_Moria.ambient) {
			value = mod_Moria.ambient;
		}
		
		return value;
	}
}