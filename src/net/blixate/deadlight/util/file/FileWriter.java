package net.blixate.deadlight.util.file;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileWriter {
	OutputStream stream;
	File file;
	
	public FileWriter(File f) {
		this.file = f;
	}
	
	public FileWriter open() throws IOException {
		stream = Files.newOutputStream(file.toPath());
		return this;
	}
	
	public void writeBytes(int[] bytes) throws IOException {
		for(int i : bytes) {
			stream.write(i);
		}
	}
	
	public void writeCharArray(char[] chars) throws IOException {
		for(char c : chars) {
			stream.write(c);
		}
	}
	
	public void writeByte(int i) throws IOException {
		stream.write(i & 0xff);
	}
	
	public void write16Bit(int i) throws IOException {
		stream.write(i & 0xff);
		stream.write(i >> 8 & 0xff);
	}
	
	public void write32Bit(int i) throws IOException {
		stream.write(i & 0xff);
		stream.write(i >> 8 & 0xff);
		stream.write(i >> 16 & 0xff);
		stream.write(i >> 24 & 0xff);
	}

	public void close() throws IOException {
		stream.close();
	}
}
