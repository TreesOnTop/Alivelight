package net.blixate.deadlight.discord;

import java.util.Random;
import java.util.UUID;

public class VerificationCode {
	
	public static char[] CHARACTERS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890").toCharArray();
	
	public String code;
	private long generated;
	private UUID generater;
	
	public VerificationCode(UUID generatedBy, int codeLength) {
		this.generated = System.currentTimeMillis();
		this.code = "";
		this.generater = generatedBy;
		Random random = new Random();
		for(int i = 0; i < codeLength; i++) {
			this.code += CHARACTERS[random.nextInt(CHARACTERS.length)];
		}
	}
	
	public String toString() {
		return code;
	}

	public UUID getGenerator() {
		return generater;
	}

	public boolean isExpired() {
		return ((System.currentTimeMillis() - generated) > 1000 * 60 * 5);
	}
}
