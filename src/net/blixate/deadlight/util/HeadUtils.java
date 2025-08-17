package net.blixate.deadlight.util;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.blixate.deadlight.Deadlight;

public final class HeadUtils {
	
	private HeadUtils() {}
	
	public static final UUID PUBLIC_SKULL_UUID = UUID.fromString("291ef30f-de51-332b-83be-5e8c035eca37");
	
	public static ItemStack createPlayerHead(String playerSkullTexture) {
		
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		if(playerSkullTexture == null || playerSkullTexture.isEmpty()) {
			return item;
		}
		SkullMeta meta = (SkullMeta)item.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", playerSkullTexture));
		Field profileField = null;
		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack createStackablePlayerHead(String playerSkullTexture) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		if(playerSkullTexture == null || playerSkullTexture.isEmpty()) {
			return item;
		}
		SkullMeta meta = (SkullMeta)item.getItemMeta();
		GameProfile profile = new GameProfile(PUBLIC_SKULL_UUID, null);
		profile.getProperties().put("textures", new Property("textures", playerSkullTexture));
		Field profileField = null;
		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public static String getHead(String name) {
		return Deadlight.inst.getConfig().getString("heads." + name);
	}
	
	// skin stuff
	
	/*public static void changeSkin(DLUser player, String skinPlayerUuid) {
		JSONObject profile = WebUtils.getProfile(UUID.fromString(skinPlayerUuid));
		JSONArray properties = (JSONArray) profile.get("properties");
		JSONObject texturesProperty = (JSONObject) properties.get(0);
		Property skinProperty = new Property(
				(String)texturesProperty.get("name"),
				(String)texturesProperty.get("value"),
				(String)texturesProperty.get("signature"));
		changeSkin(player, WrappedSignedProperty.fromHandle(skinProperty));
	}
	
	public static void changeSkin(DLUser player, WrappedSignedProperty property) {
		WrappedGameProfile ourProfile = WrappedGameProfile.fromPlayer(player.getPlayer());
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		PacketContainer addTab = getTabPacket(player, ourProfile, PlayerInfoAction.ADD_PLAYER);
		PacketContainer removeTab = addTab.shallowClone();
		removeTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
		
		ourProfile.getProperties().removeAll("textures");
        ourProfile.getProperties().put("textures", property);
		try {
			for(Player other : Bukkit.getOnlinePlayers()) {
				manager.sendServerPacket(other, removeTab);
	            manager.sendServerPacket(other, addTab);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		player.changingSkinLocation = player.getLocation();
		player.getPlayer().setHealth(0);
	}
	
	public static void changeSkin(DLUser player) {
		changeSkin(player, getSkin());
	}
	
	public static WrappedSignedProperty getSkin() {
		return new WrappedSignedProperty("textures", "eyJ0aW1lc3RhbXAiOjE0ODI1MjE4MTU4MTQsInByb2ZpbGVJZCI6IjQzYTgzNzNkNjQyOTQ1MTBhOWFhYjMwZjViM2NlYmIzIiwicHJvZmlsZU5hbWUiOiJTa3VsbENsaWVudFNraW42Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82ODQ1NzU2ODI5YjZiY2E1MTZiNWJmOTI1MWFlMzFjNzljZDZkZGJjM2M1N2YxMTkzNzBiMGNjZDhkNmY1YTEifX19", "SKrjFrc/FJfl+xSG1/gsjcPMkEWhHkme527T3dTZIXtofzunAQ8VqZcPu9NJmwOCOlvqRL8T0STwSGaNZctHkh5+5xVKsS6w8/oe3rbdz+7g854C/p5Op7xCH0H/HhEB1HgVqQx5ZOWFuaGn2EaPIdx1v/9z//TG/SyCeNeYsiafJcETDFTeFLxl+L+RpyMxjBlTwPD1vQPrt2VZ8PLfpMlndbQUuquPhCeoYRNil9fqjYjNJHSnc9URjGfpBVZk/XCb+F6i3ljkbv9OChSgPUhli9ktckVnyFQmkImOq1eviyThh2pjg9qV7peaU9dxNyNazpf40B82X4Wztor62Y14DJXaGzUZcQN6oMbr/L8xjEwXRXuWBt9Szemi7ZZIpGXR00GSqeEW20+C5ZiwbsjzmuLxDw876FG/w76U2T9Z1joEf4ef+c13Byc+9KVXBX3ybhTerrXkW+oXbx2XBRZ5K5cOHmlcFT7rmR9iiXTcA2smB4eFxyHqgITLK/28aWSGyQFjZJMLxSr0EP0I0yrMil8tXggTZFq7kFeO32Ehr1unqGQet3kjCnk6z3vNUAnW6PmzmATwEaGEvOeIpBufq7EnylBK/UzpqoGTkn47zEXBHXY7m+BT2PwFSEF0D0X63h8SHx+G5hiSbcH6BRG1i1OLi7GofuadMHEPddI=");
	}
	
	public static PacketContainer getTabPacket(DLUser user, WrappedGameProfile profile, PlayerInfoAction action) {
        PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

        addTab.getPlayerInfoAction().write(0, action);
        addTab.getPlayerInfoDataLists().write(0, Collections.singletonList(
            new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(user.getFormattedName()))));

        return addTab;
    }
	*/
}