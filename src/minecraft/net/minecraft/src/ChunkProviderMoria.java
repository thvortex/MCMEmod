package net.minecraft.src;

public class ChunkProviderMoria implements IChunkProvider
{
	public IChunkProvider chunkProvider;

	public ChunkProviderMoria(IChunkProvider _chunkProvider) {
		chunkProvider = _chunkProvider;
	}

    public boolean chunkExists(int i, int j) {
		return chunkProvider.chunkExists(i, j);
	}

    public Chunk provideChunk(int i, int j) {
		Chunk chunk = chunkProvider.provideChunk(i, j);

		// NibbleArrayMoria will override the true skylight levels with ambient while inside Moria
		if(!(chunk.skylightMap instanceof NibbleArrayMoria)) {
			chunk.skylightMap = new NibbleArrayMoria(chunk.skylightMap);
			chunksSeen.put(chunk, true);
		}

		return chunk;
	}

    public Chunk loadChunk(int i, int j) {
		return chunkProvider.loadChunk(i, j);
	}

    public void populate(IChunkProvider ichunkprovider, int i, int j) {
		chunkProvider.populate(ichunkprovider, i, j);
	}

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
		return chunkProvider.saveChunks(flag, iprogressupdate);
	}

    public boolean unload100OldestChunks() {
		return chunkProvider.unload100OldestChunks();
	}

    public boolean canSave() {
		return chunkProvider.canSave();
	}

    public String makeString() {
		return chunkProvider.makeString();
	}
}
