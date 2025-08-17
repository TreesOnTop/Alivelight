package net.blixate.deadlight.discord.commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map.Entry;

import net.blixate.deadlight.kit.offerings.Offering;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.kit.offerings.OfferingManager.ActiveOffering;
import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class OfferingsCommand implements DiscordCommand {

	@Override
	public void execute(String arg0, String[] arg1, Message arg2, User arg3, MessageChannelUnion channel) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Offerings");
		builder.setColor(Color.MAGENTA);
		HashMap<String, Integer> offeringList = new HashMap<>();
		
		for(ActiveOffering offering : OfferingManager.getActive()) {
			offeringList.put(Offering.valueOf(offering.name).getName(), offeringList.getOrDefault(Offering.valueOf(offering.name).getName(), 0) + 1);
			
		}
		String content = "";
		for(Entry<String, Integer> entry : offeringList.entrySet()) {
			content += entry.getValue() + "x " + entry.getKey() + "\n";
		}
		builder.addField(new Field("Active Offerings", content, true));
		builder.addField(new Field("Boosts", "Blood Boost " + OfferingManager.getBloodBoost() + "\nXP Boost " + OfferingManager.getXPBoost(), true));
		channel.sendMessageEmbeds(builder.build()).queue();
	}

}
