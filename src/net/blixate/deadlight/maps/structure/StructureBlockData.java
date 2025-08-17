package net.blixate.deadlight.maps.structure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class StructureBlockData {
	/** Regex for removing any useless string data. */
	static Pattern BLOCK_DATA_PATTERN = Pattern.compile("(minecraft[:].*?)\\[(.*?)\\]");
	
	int rasterIndex;
	BlockData data;
	String blockDataString;
	
	StructureBlockData(int idx, BlockData bd) {
		this.rasterIndex = idx;
		this.data = bd;
		Matcher m = BLOCK_DATA_PATTERN.matcher(bd.getAsString());
		if(m.find()) {
			this.blockDataString = m.group(2);
		}
	}
	
	StructureBlockData(int idx, String str) {
		this.rasterIndex = idx;
		this.blockDataString = str;
	}
	
	public boolean hasData() {
		return this.blockDataString != null;
	}
	
	public BlockData getBlockData(Material mat) {
		return mat.createBlockData("[" + blockDataString + "]");
	}
	
	public String getBareString() {
		return blockDataString;
	}
}
