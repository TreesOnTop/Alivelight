package net.blixate.deadlight.util.file;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public class FileReader {
	File file;
	byte[] bytes;
	int pointer;
	
	public FileReader(File f) {
		this.file = f;
	}
	
	public FileReader open() throws IOException {
		bytes = Files.toByteArray(this.file);
		return this;
	}
	
	public int getPointer() {
		return pointer;
	}
	
	public int getFileSize() {
		return this.bytes.length;
	}
	
	public boolean isEOF() {
		return getPointer() >= getFileSize();
	}
	
	public byte nextByte() {
		if(isEOF()) {
			return (byte)0x00;
		}
		return bytes[pointer++];
	}
	
	public int nextUnsignedByte() {
		return (int)(bytes[pointer++]) & 0xff;
	}
	
	public char nextChar() {
		if(isEOF()) {
			return '\0';
		}
		return (char)nextUnsignedByte();
	}
	
	public int nextInt() {
		int i = 0;
		i |= nextUnsignedByte() << 0;
		i |= nextUnsignedByte() << 8;
		i |= nextUnsignedByte() << 16;
		i |= nextUnsignedByte() << 24;
		return i;
	}
	
	public String nextString(int length) {
		return new String(nextCharArray(length));
	}
	public String nextString() {
		return new String(nextCharArray(nextUnsignedByte()));
	}
	
	public int[] nextByteArray(int length) {
		int[] bytes = new int[length];
		for(int i = 0; i < length; i++) {
			bytes[i] = nextUnsignedByte();
		}
		return bytes;
	}
	
	public byte[] nextBytes(int length) {
		byte[] bytes = new byte[length];
		for(int i = 0; i < length; i++) {
			bytes[i] = nextByte();
		}
		return bytes;
	}
	
	public char[] nextCharArray(int length) {
		if(length < 0) {
			return new char[0];
		}
		char[] chars = new char[length];
		for(int i = 0; i < length; i++) {
			chars[i] = nextChar();
		}
		return chars;
	}

	public int next16Bit() {
		int i = 0;
		i |= (((int)nextByte()) << 0);
		i |= (((int)nextByte()) << 8);
		return i;
	}
	
}
