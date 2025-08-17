package net.blixate.deadlight;

import java.time.LocalDate;
import java.util.Calendar;

public class TimeKeeper {
	
	public static enum MonthlyEvents {
		HALLOWEEN(Calendar.OCTOBER),
		CHRISTMAS(Calendar.DECEMBER),
		VALENTINE(Calendar.FEBRUARY);
		
		int month;
		
		MonthlyEvents(int month) {
			this.month = month;
		}
	}
	
	public static enum Events {
		BLOODBATH("bloodbath"),
		FISHING("fishing"),
		SURGE("plague surge");
		
		public String cfgName;
		
		Events(String cfg) {
			this.cfgName = cfg;
		}
	}
	
	public static int getDayOfTheWeek() {
		return getDayOfTheWeek(LocalDate.now());
	}
	
	/*public static boolean isMysteryShopOpen() {
		return isEvent(Events.MYSTERY_SHOP);
	}*/
	
	public static boolean isBloodBathEvent() {
		return isEvent(Events.BLOODBATH);
	}
	
	public static boolean isFishingFrenzyEvent() {
		return isEvent(Events.FISHING);
	}
	
	public static boolean isSurgeEvent() {
		return isEvent(Events.SURGE);
	}
	
	public static boolean isEvent(String event) {
		if(isAnyMonthlyEventActive()) {
			return false; // Mini events are always active while monthly events are active.
		}
		if(getDayByName(event) == -1) {
			return isEventEnabled(event);
		}
		return getDayOfTheWeek() == getDayByName(event) && isEventEnabled(event);
	}
	
	public static boolean isEvent(Events event) {
		return isEvent(event.cfgName);
	}
	
	public static boolean isEventEnabled(String event) {
		return Deadlight.inst.getConfig().getBoolean("events."+event+".enabled");
	}
	
	public static boolean isEventEnabled(Events event) {
		return isEventEnabled(event.cfgName);
	}
	
	public static boolean isMonthlyEventActive(MonthlyEvents event) {
		int month = LocalDate.now().getMonthValue() - 1;
		return month == event.month;
	}
	
	public static boolean isAnyMonthlyEventActive() {
		for(MonthlyEvents event : MonthlyEvents.values()) {
			if(isMonthlyEventActive(event)) {
				return true;
			}
		}
		return false;
	}
	
	public static int getDayByName(String event) {
		String configName = Deadlight.inst.getConfig().getString("events."+event+".weekday");
		if(configName == null) return -1;
		switch(configName) {
		case "sunday": return Calendar.SUNDAY;
		case "monday": return Calendar.MONDAY;
		case "tuesday": return Calendar.TUESDAY;
		case "wednesday": return Calendar.WEDNESDAY;
		case "thursday": return Calendar.THURSDAY;
		case "saturday": return Calendar.SATURDAY;
		}
		return -1;
	}
	
	public static int getDayOfTheWeek(LocalDate date) {
		switch(date.getDayOfWeek().getValue()) {
		case 1: return Calendar.MONDAY;
		case 2: return Calendar.TUESDAY;
		case 3: return Calendar.WEDNESDAY;
		case 4: return Calendar.THURSDAY;
		case 5: return Calendar.FRIDAY;
		case 6: return Calendar.SATURDAY;
		case 7: return Calendar.SUNDAY;
		}
		return -1;
	}
}
