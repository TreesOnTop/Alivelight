package net.blixate.deadlight.commands;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.MsgConfig;
import net.blixate.deadlight.advertising.MinehutRank;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.time.TimeParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class InfoCommands extends DLCmd {
	
	String[] ranks = { "&7", "&a[VIP] ", "&b[PRO] ", "&6[LEGEND] ", "&3[PATRON] " };
	
	@Override
	public void execute() {
		String label = this.getLabel();
		if(label.startsWith("deadlight:")) {
			label = label.substring("deadlight:".length());
		}
		switch(label) {
		case "discord": {
			String discordLink = Deadlight.inst.getConfig().getString("global.discord link");
			TextComponent text = new TextComponent(discordLink);
			text.setColor(ChatColor.BLUE);
			text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordLink));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(FormatUtil.color("&b&lClick here to join our discord!"))));
			user.getPlayer().spigot().sendMessage(text);
		}
			break;
		case "ad":
		case "admsg": {
			MinehutRank rank = user.getMinehutRank();
			boolean defaultRank = rank.id.equals("DEFAULT");
			
			String adString = Deadlight.inst.getConfig().getString("ad message." + (defaultRank? "default" : "colored"));
			
			String preview = "&d[AD] " + rank.formatPlayer(user.getName()) + (defaultRank ? "&7" : "&f") + ": /ad Deadlight " + adString;
			TextComponent text = new TextComponent("► Click here to copy our advertising message!");
			text.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/ad Deadlight " + adString));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(FormatUtil.color("&7Preview:\n" + preview + "\n&9Click here to copy to your clipboard"))));
			text.setColor(ChatColor.GREEN);
			user.getPlayer().spigot().sendMessage(text);
			//user.getPlayer().sendMessage(ChatColor.BLUE+"Pick an ad message for your rank, and paste it in Minehut's Lobby. If you only have a legacy rank, choose Default.");
		}
			break;
		case "rules":
			if(user.getLobby() == null) {
				user.showBook(MsgConfig.getMessageList("rules"));
				return;
			}
			user.send("rules");
			break;
		case "tut":
		case "help":
		// either /guide or /tutorial count as reading the tutorial
		case "tutorial": {
			user.showBook(MsgConfig.getMessageList("tutorial"));
			if(!user.hasReadTutorial) {
				user.hasReadTutorial = true;
				user.addScoreEvent(new ScoreEvent("starter_blood", ScoreType.BLOOD));
				user.savePlayerData();
			}
			/*if(user.isInMatch()) {
				user.send("must_be_at_spawn");
			}
			ConfigurationSection locSec = Deadlight.cfg().getConfigurationSection("tutorial spawn");
			Location loc = new Location(Deadlight.getWorld(), locSec.getDouble("x"), locSec.getDouble("y"), locSec.getDouble("z"), (float)locSec.getDouble("yaw"), (float)locSec.getDouble("pitch"));
			user.getPlayer().teleport(loc);
			user.send("tutorial_message");*/
		}
			break;
		case "guide": {
			user.showBook(MsgConfig.getMessageList("guide"));
			if(!user.hasReadTutorial) {
				user.hasReadTutorial = true;
				user.addScoreEvent(new ScoreEvent("starter_blood", ScoreType.BLOOD));
				user.savePlayerData();
			}
		}
			break;
		case "pt":
		case "playtime":
		{
			DLData player = (DLData) this.user;
			if(this.args.size() > 0) {
				player = PlayerManager.getOfflineUser(this.args.get(0));
				if(player == null) {
					user.send("bad_argument", this.args.get(0));
					break;
				}
			}
			long playtime = player.getPlaytime();
			user.send(this.args.size() > 0 ? "playtime_other" : "playtime_self", TimeParser.toFancyTime(playtime), player.getName());
		}
			break;
		case "kills":
		case "statistics":
		case "stats": {
			if(this.hasArg(1)) {
				DLData o = PlayerManager.getOfflineUser(this.getArg(0));
				if(o == null) {
					user.send("bad_argument", this.args.get(0));
					break;
				}
				user.send("stats_other", o.kills + "", o.deaths + "", o.escapes + "", o.escapeStreak + "", o.killerWeight + "", TimeParser.toFancyTime(o.getPlaytime()), o.getName());
			}else {
				user.send("stats_self", user.kills + "", user.deaths + "", user.escapes + "", user.escapeStreak + "", user.killerWeight + "", TimeParser.toFancyTime(user.getPlaytime()));
			}
		}
			break;
		case "pong":
		case "ping":
			if(this.hasArg(1)) {
				DLUser o = PlayerManager.getUser(this.getArg(0));
				if(o == null) {
					user.send("bad_argument", this.args.get(0));
					break;
				}
				user.send("ping_other", ""+o.getPing(), o.getName());
			}else {
				user.send("ping_self", ""+user.getPing());
			}
			break;
		}
	}

}
