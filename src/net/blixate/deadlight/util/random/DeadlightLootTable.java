package net.blixate.deadlight.util.random;

import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.offerings.Offering;

public class DeadlightLootTable {
	
	public static enum Category {
		BLOOD, SOULS, ITEMS, OFFERING;
		
		public String toString() {
			switch(this) {
			case BLOOD: return "Blood";
			case SOULS: return "Souls";
			case ITEMS: return "Item";
			case OFFERING: return "Offering";
			}
			return "null";
		}
	}
	
	public static interface TableCount {
		public long getAmount();
	}
	
	public static class StaticTableCount implements TableCount {
		public long amount;
		
		public StaticTableCount(long amount) {
			this.amount = amount;
		}

		@Override
		public long getAmount() {
			return amount;
		}
	}
	
	public static class RandomTableCount implements TableCount {
		public long amount;
		public Random random;
		
		public RandomTableCount(long min, long max) {
			Random random = new Random();
			this.amount = min + (Math.abs(random.nextLong()) % (max-min));
		}

		@Override
		public long getAmount() {
			return amount;
		}
	}
	
	public static class RandomListTableCount implements TableCount {
		public long amount;
		public long[] options;
		public Random random;
		
		public RandomListTableCount(long[] options) {
			Random random = new Random();
			this.amount = options[random.nextInt() % options.length];
		}

		@Override
		public long getAmount() {
			return amount;
		}
	}
	
	public static class ItemDrop {
		public String name;
		public TableCount count;
		
		@Override
		public String toString() {
			return name + ":" + count.getAmount();
		}
	}
	
	public static class OfferingDrop extends ItemDrop {
		
	}
	
	public static class DropResult {
		public Category category;
		
		@Override
		public String toString() {
			return "{" + category.name() + "}";
		}
	}
	
	public static class FungibleDropResult extends DropResult {
		public long drop;
		
		@Override
		public String toString() {
			return drop + " " + category.toString();
		}
	}
	
	public static class ItemDropResult extends DropResult {
		public ItemDrop drop;
		
		@Override
		public String toString() {
			String itemName = Items.valueOf(drop.name).name;
			long amount = drop.count.getAmount();
			if(amount > 1) {
				return itemName + " (x" + amount + ")";
			}
			return itemName;
		}
	}
	
	public static class OfferingDropResult extends DropResult {
		public OfferingDrop drop;
		
		@Override
		public String toString() {
			String offeringName = Offering.valueOf(drop.name).getName() + " Offering";
			long amount = drop.count.getAmount();
			if(amount > 1) {
				return offeringName + " (x" + amount + ")";
			}
			return offeringName;
		}
	}
	
	JSONObject object;
	
	HashMap<Category, JSONObject> map;
	
	public DeadlightLootTable(JSONObject object) {
		this.object = object;
		this.map = new HashMap<>();
		for(Object key : object.keySet()) {
			Category cat = Category.valueOf(key.toString().toUpperCase());
			if(cat == null) {
				continue; // Unknown category!
			}
			map.put(Category.valueOf(key.toString().toUpperCase()), (JSONObject)object.get(key));
		}
	}
	
	public DropResult pick() {
		WeightedDropTable<Category> table = new WeightedDropTable<>();
		for(Category category : map.keySet()) {
			table.addElement(category, (int)getWeight(category));
		}
		// We now know what we are giving: Blood, Souls, Items, whatever.
		Category category = table.pick();
		Random random = new Random();
		// Now we need to decide what we are giving
		switch(category) {
		case SOULS:
		case BLOOD: {
			TableCount count = getCount(map.get(category).get("count"));
			FungibleDropResult drop = new FungibleDropResult();
			drop.category = category;
			drop.drop = count.getAmount();
			return drop;
		}
		case ITEMS: {
			ItemDropResult drop = new ItemDropResult();
			drop.category = category;
			JSONArray pool = (JSONArray)map.get(category).get("pool");
			JSONObject item = (JSONObject)pool.get(random.nextInt(pool.size()));
			drop.drop = new ItemDrop();
			drop.drop.name = (String)item.get("item");
			drop.drop.count = getCount(item.get("count"));
			return drop;
		}
		case OFFERING:
			OfferingDropResult drop = new OfferingDropResult();
			drop.category = category;
			JSONArray pool = (JSONArray)map.get(category).get("pool");
			JSONObject item = (JSONObject)pool.get(random.nextInt(pool.size()));
			drop.drop = new OfferingDrop();
			drop.drop.name = (String)item.get("offering");
			drop.drop.count = getCount(item.get("count"));
			return drop;
		default:
			break;
		
		}
		return null;
	}
	
	public TableCount getCount(Object object) {
		if(object instanceof Long) {
			return new StaticTableCount((Long)object);
		}
		if(object instanceof JSONObject) {
			JSONObject obj = (JSONObject)object;
			if(obj.containsKey("min") && obj.containsKey("max")) {
				long min = (long) obj.get("min");
				long max = (long) obj.get("max");
				return new RandomTableCount(min, max);
			}
			if(obj.containsKey("options")) {
				JSONArray array = (JSONArray)obj.get("options");
				long[] options = new long[array.size()];
				for(int i = 0; i < array.size(); i++) {
					options[i] = (long)array.get(i);
				}
				return new RandomListTableCount(options);
			}
		}
		return null;
	}
	
	public long getWeight(Category obj) {
		if(obj == null) {
			return -1;
		}
		return (Long) map.get(obj).get("weight");
	}
	
}
