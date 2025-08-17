package net.blixate.deadlight.maps.structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.util.file.FileReader;
import net.blixate.deadlight.util.file.FileWriter;

/**
 * This uses the new format, which saves space and should be more efficent.
 */
public class StructureFile extends MapFile {
	
	public static final int FORMAT_VERSION = 0; // Every single update to this format should increment this number.
	
	/*
	 * File Flags
	 */
	public static final int FLAG_NULL_STRING 		= 0b00000001;
	public static final int FLAG_PALLETTE_16_BIT	= 0b00001000; // This also means block data is 16-bit.
	public static final int FLAG_STRING_16_BIT		= 0b00010000;
	public static final int FLAG_32_BIT_MODE		= 0b00100000;
	public static final int FLAG_NO_BLOCK_DATA		= 0b01000000;
	// Unused bit. Can be used for modified formats = 0b00000010;
	// Unused bit. Can be used for modified formats = 0b00000100;
	
	public static final char[] MAGIC_NUMBER = {0x42, 0x4c, 0x44};
	private int version = 0, flags;
	private int[] blocks;
	private String[] pallette;
	private StructureBlockData[] blockData;
	
	/* These dont get saved, and are only for efficency */
	private Material[] materials; // Copy of pallette, but the material version :P
	private HashMap<Integer, StructureBlockData> quickBlockData;
	private File file;
	
	public StructureFile(File f) {
		this.file = f;
	}
	
	public void raster(Location pos, int sx, int sy, int sz) throws IOException {
		sizeX = sx;
		sizeY = sy;
		sizeZ = sz;
		blocks = new int[sx * sy * sz];
		int totalBlockRaster = 0;
		ArrayList<String> palette = new ArrayList<>();
		ArrayList<StructureBlockData> sbdarray = new ArrayList<>();
		for(int y = pos.getBlockY(); y < pos.getBlockY() + sizeY; y++) {
			for(int x = pos.getBlockX(); x < pos.getBlockX() + sizeX; x++) {
				for(int z = pos.getBlockZ(); z < pos.getBlockZ() + sizeZ; z++) {
					Location l = new Location(pos.getWorld(), x, y, z);
					/* Save all the meta-data about this block */
					Block block = l.getBlock();
					String material = block.getType().name();
					if(!palette.contains(material)) {
						if(material.length() > 0xff && !is16BitStrings()) {
							flags |= FLAG_STRING_16_BIT;
						}
						palette.add(material);
					}
					StructureBlockData bd = new StructureBlockData(totalBlockRaster, block.getBlockData());
					if(bd.hasData()) {
						if(bd.blockDataString.length() > 0xff && !is16BitStrings()) {
							flags |= FLAG_STRING_16_BIT;
						}
						sbdarray.add(bd);
					}
					var i = palette.indexOf(material);
					blocks[totalBlockRaster] = i;
					totalBlockRaster++;
				}
			}
		}
		// Save all serializable data, and set important flags.
		pallette = palette.toArray(new String[0]);
		if(pallette.length > 0xff) {
			flags |= FLAG_PALLETTE_16_BIT;
		}
		if(sbdarray.isEmpty()) {
			flags |= FLAG_NO_BLOCK_DATA;
		}
		blockData = sbdarray.toArray(new StructureBlockData[0]);
	}
	
	/** Load the important data into memory. */
	public StructureFile load() throws IOException {
		// Open file
		FileReader reader = new FileReader(file).open();
		// Read the magic number
		String magicString = reader.nextString(3);
		if(!magicString.equals(String.valueOf(MAGIC_NUMBER))) {
			throw new RuntimeException("Magic number does not match! This is not a BLD file.");
		}
		// Read the file's version
		version = reader.nextInt();
		if(version != FORMAT_VERSION) {
			throw new RuntimeException("Version does not match: Got " + version + ", expected " + FORMAT_VERSION);
		}
		// Read size
		sizeX = reader.nextUnsignedByte();
		sizeY = reader.nextUnsignedByte();
		sizeZ = reader.nextUnsignedByte();
		Deadlight.debug("File Map Size: " + sizeX + ", " + sizeY + ", " + sizeZ);
		// Read flags
		flags = reader.nextByte(); // We can now check file flags!
		// Read in the pallette
		pallette = new String[readIntIf(reader, FLAG_PALLETTE_16_BIT)];
		Deadlight.debug("Pallette Size: " + pallette.length);
		for(int i = 0; i < pallette.length; i++) {
			int stringLength = readIntIf(reader, FLAG_STRING_16_BIT);
			pallette[i] = reader.nextString(stringLength);
		}
		// TODO: Make this follow flag rules
		// Read in the blocks
		blocks = reader.nextByteArray(sizeX * sizeY * sizeZ);
		Deadlight.debug("Loaded " + (sizeX * sizeY * sizeZ) + " blocks");
		// If this file has block data, read it in!
		if(hasBlockData()) {
			ArrayList<StructureBlockData> blockD = new ArrayList<>();
			while(!reader.isEOF()) {
				int index = reader.nextInt();
				String dataString = reader.nextString();
				StructureBlockData data = new StructureBlockData(index, dataString);
				blockD.add(data);
			}
			blockData = blockD.toArray(new StructureBlockData[0]);
			Deadlight.debug("Loaded " + blockData.length + " additional block data fields");
		}
		return this;
	}
	
	/** Turns any ArrayList or incorrect type list to a faster alternative */
	private void startQuickIndex() {
		Deadlight.debug("Creating quick indexing service...");
		if(materials == null) {
			// Turn pallette in material array
			materials = new Material[pallette.length];
			int i = 0;
			for(String p : pallette) {
				materials[i++] = Material.matchMaterial(p);
			}
		}
		if(quickBlockData == null) {
			/* Make indexing as quick as possible */
			if(hasBlockData()) {
				quickBlockData = new HashMap<>();
				for(StructureBlockData sbd : blockData) {
					quickBlockData.put(sbd.rasterIndex, sbd);
				}
			}
		}
	}
	
	/* Build the structure knowing what data we have. */
	public void build(Location pos) {
		startQuickIndex();
		int blockIdx = 0;
		World world = pos.getWorld();
		for(int y1 = 0; y1 < sizeY; y1++) {
			for(int x1 = 0; x1 < sizeX; x1++) {
				for(int z1 = 0; z1 < sizeZ; z1++) {
					Material mat = materials[blocks[blockIdx]];
					if(mat != null) {
						Location l = new Location(world, x1 + pos.getBlockX(), y1 + pos.getBlockY(), z1 + pos.getBlockZ());
						Block b = l.getBlock();
						b.setType(mat, false);
						if(hasBlockData()) {
							StructureBlockData matSbd = quickBlockData.get(blockIdx);
							if(matSbd != null) {
								b.setBlockData(matSbd.getBlockData(mat), false);
							}
						}
					}
					blockIdx++;
				}
			}
		}
		System.out.println("Final Block Index: " + blockIdx);
	}

	public void save() throws IOException {
		/* Load from the structure data */
		version = FORMAT_VERSION;
		// create an output stream
		FileWriter writer = new FileWriter(this.file).open();
		writer.writeCharArray(MAGIC_NUMBER); // save magic number
		writer.write32Bit(FORMAT_VERSION); // save current format version
		writer.writeBytes(new int[] { sizeX, sizeY, sizeZ }); // save size
		writer.writeByte(flags); // save flags
		
		/* Write out the pallette */
		writeIntIf(writer, pallette.length, FLAG_PALLETTE_16_BIT);
		for(String s : pallette) {
			writeString(writer, s);
		}
		/* Write out blocks */
		for(int i: blocks) {
			writeIntIf(writer, i, FLAG_PALLETTE_16_BIT);
		}
		/* Write out block data */
		for(StructureBlockData bd : blockData) {
			writer.write32Bit(bd.rasterIndex);
			writeString(writer, bd.getBareString());
		}
		writer.close();
	}
	
	public void setSize(int x, int y, int z) {
		this.sizeX=x;
		this.sizeY=y;
		this.sizeZ=z;
	}
	
	private int readIntIf(FileReader reader, int flag) {
		return ((flags & flag) != 0) ? (is32Bit() ? reader.nextInt(): reader.next16Bit()): reader.nextUnsignedByte();
	}
	
	private void writeIntIf(FileWriter writer, int i, int flag) throws IOException {
		if((flags & flag) != 0) {
			if(is32Bit()) {
				writer.write32Bit(i);
			}else{
				writer.write16Bit(i);
			}
			return;
		}
		writer.writeByte(i);
	}
	
	private void writeString(FileWriter writer, String data) throws IOException {
		if(!useNullStrings()) {
			writeIntIf(writer, data.length(), FLAG_STRING_16_BIT);
		}
		writer.writeCharArray(data.toCharArray());
		if(useNullStrings()) {
			writer.writeByte(0);
		}
	}
	
	/* Flags */
	private boolean is32Bit() {
		return (flags & FLAG_32_BIT_MODE) != 0;
	}
	
	private boolean is16BitStrings() {
		return (flags & FLAG_STRING_16_BIT) != 0;
	}
	
	private boolean useNullStrings() {
		return (flags & FLAG_NULL_STRING) != 0;
	}
	private boolean hasBlockData() {
		return (flags & FLAG_NO_BLOCK_DATA) == 0;
	}

	public int[] getSize() {
		return new int[] {sizeX, sizeY, sizeZ};
	}
}
