package net.blixate.deadlight.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class NameGenerator {
	
	private static String[] defaultNames = { "Barry", "Jerry", "Larry", "Kerry", "Mary", "Harry", "Garry", "Terry", "Perry" };
	
	private String[] names;
	private int next;
	
	public NameGenerator() {
		this.next = 0;
	}
	
	public void read(File file) throws IOException {
		String content = String.valueOf(Files.readAllBytes(file.toPath()));
		names = content.split("\n");
	}
	
	public String pickName() {
		
		if(names == null || names.length == 0) {
			if(next >= defaultNames.length) {
				next = 0;
			}
			return defaultNames[next++];
		}
		if(next >= names.length) {
			next = 0;
		}
		return names[next++];
	}
	
}
