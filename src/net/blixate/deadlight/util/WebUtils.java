package net.blixate.deadlight.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WebUtils {
	public static String RESOURCE_VERISON_URL = "https://api.spiget.org/v2/resources/%resourceid/versions/latest";
	public static String MINEHUT_COSMETICS_URL = "https://api.minehut.com/cosmetics/profile/%uuid";
	private static String MOJANG_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%uuid?unsigned=false";
	
	private static JSONParser parser = new JSONParser();
	
	public static String getContent(String url) {
		URL versionurl;
		try {
			versionurl = new URL(url);
			URLConnection connection = versionurl.openConnection();
			InputStream stream = (InputStream)connection.getInputStream();
			String text = new String(stream.readAllBytes());
			stream.close();
			return text;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static JSONObject getNameHistory(String uuid) {
		return getJSON("https://api.mojang.com/user/profiles/"+uuid+"/names");
	}
	
	public static JSONObject getJSON(String url) {
		String content = getContent(url);
		JSONObject obj;
		try {
			obj = (JSONObject)parser.parse(content);
		} catch (ParseException e) {
			return null;
		}
		return obj;
	}
	
	public static JSONArray getJSONArray(String url) {
		String content = getContent(url);
		JSONArray obj;
		try {
			obj = (JSONArray)parser.parse(content);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return obj;
	}
	
	public static Object getResourceVersion(String resourceId) {
		return getJSON(RESOURCE_VERISON_URL.replace("%resourceid", resourceId)).get("name");
	}
	
	/** 
	 * do not use unless absolutely necessary, and please cache results.
	 */
	public static JSONObject getProfile(UUID uuid) {
		String content = getContent(MOJANG_PROFILE_URL.replace("%uuid", uuid.toString()));
		JSONObject obj;
		try {
			obj = (JSONObject)parser.parse(content);
		}catch(ParseException e) {
			return null;
		}
		return obj;
	}
}
