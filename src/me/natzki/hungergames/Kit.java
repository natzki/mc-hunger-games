package me.natzki.hungergames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class Kit {
	
	static ArrayList<Kit> kits = new ArrayList<Kit>();
	
	LinkedHashMap<String, Object> kitData = new LinkedHashMap<String, Object>();
	
	public String name;
	public String type;
	public int cost;
	public ItemStack icon;
	public ItemStack compass = new ItemStack(Material.COMPASS, 1);
	
	public Kit(String name, String type, int cost, Material icon) {
		this.name = name;
		this.type = type;
		this.cost = cost;
		this.icon = new ItemStack(icon, 1);
	}
	
	public static void addMainKits() {
		kits.add(new Kit("Rogue", "free", 0, Material.IRON_SWORD));
		kits.add(new Kit("Archer", "free", 0, Material.BOW));
		kits.add(new Kit("Mage", "free", 0, Material.POTION));
		kits.add(new Kit("Miner", "free", 0, Material.STONE_PICKAXE));
		kits.add(new Kit("Builder", "free", 0, Material.STONE));
		kits.add(new Kit("Jorge", "free", 0, Material.TROPICAL_FISH));
		kits.add(new Kit("Knight", "free", 0, Material.CHAINMAIL_CHESTPLATE));
		
		kits.add(new Kit("Lumberjack", "premium", 20, Material.IRON_AXE));
		kits.add(new Kit("Assassin", "premium", 20, Material.POTION));
		kits.add(new Kit("Zoo", "premium", 40, Material.BLAZE_SPAWN_EGG));
		kits.add(new Kit("Bomber", "premium", 40, Material.TNT));
		
	}
	
	/*
	 * With putting things into the HashMap, any name can be chosen for items, enchanted items, potions, potioneffects, abilities etc
	 * Initial effects are all PotionEffects
	 * Abilities are all Strings
	 */
	
	public static void setKitData() {
		HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		ItemStack smith;
		for (Kit k : kits) {
			switch (k.name) {
			case "Rogue":
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.KNOCKBACK, 1);
				
				k.kitData.put("primary", enchantItem(Material.IRON_SWORD, enchants));
				k.kitData.put("initial_effect", new PotionEffect(PotionEffectType.SLOW, 180*20, 0));
				break;
			case "Archer":
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.ARROW_DAMAGE, 1);
				
				k.kitData.put("primary", enchantItem(Material.BOW, enchants));
				k.kitData.put("secondary", new ItemStack(Material.ARROW, 1));
				k.kitData.put("initial_effect", new PotionEffect(PotionEffectType.SLOW, 150*20, 0));
				break;
			case "Mage":
				smith = getPotionItemStack(true, PotionType.INSTANT_HEAL, Material.POTION, 0, 1, k);
				k.kitData.put("secondary", smith);
				smith = new ItemStack(Material.AIR, 1);
				smith = getPotionItemStack(true, PotionType.REGEN, Material.POTION, 150, 1, k);
				k.kitData.put("primary", smith);
				break;
			case "Miner":
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.DURABILITY, 2);
				enchants.put(Enchantment.DIG_SPEED, 2);
				
				k.kitData.put("primary", enchantItem(Material.STONE_PICKAXE, enchants));
				break;
			case "Jorge":
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.KNOCKBACK, 1);
				enchants.put(Enchantment.LUCK, 3);
				
				k.kitData.put("primary", enchantItem(Material.FISHING_ROD, enchants));
				
				k.kitData.put("secondard", new ItemStack(Material.COOKED_SALMON, 6));
				k.kitData.put("tertiary", new ItemStack(Material.COOKED_COD, 6));
				k.kitData.put("quad", new ItemStack(Material.TROPICAL_FISH, 6));
				
				k.kitData.put("helmet", new ItemStack(Material.TURTLE_HELMET, 1));
				break;
			case "Knight":
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.LOOT_BONUS_MOBS, 1);
				
				k.kitData.put("primary", enchantItem(Material.STONE_SWORD, enchants));
				k.kitData.put("chest", new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
				k.kitData.put("initial_effect", new PotionEffect(PotionEffectType.SLOW, 180*20, 0));
				break;
			case "Bomber":
				k.kitData.put("primary", new ItemStack(Material.TNT, 2));
				k.kitData.put("second", new ItemStack(Material.FIRE_CHARGE, 1));
				break;
			case "Zoo":
				k.kitData.put("primary", new ItemStack(Material.WOLF_SPAWN_EGG, 1));
				k.kitData.put("secondary", new ItemStack(Material.BLAZE_SPAWN_EGG, 1));
				break;
			case "Lumberjack":
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.DIG_SPEED, 1);
				
				k.kitData.put("primary", enchantItem(Material.IRON_AXE, enchants));
				break;
			case "Builder":
				k.kitData.put("primary", new ItemStack(Material.STONE, 32));
				k.kitData.put("secondary", new ItemStack(Material.OAK_PLANKS, 16));
				
				enchants = new HashMap<Enchantment, Integer>();
				enchants.put(Enchantment.PROTECTION_FALL, 2);
				enchants.put(Enchantment.FROST_WALKER, 1);
				
				k.kitData.put("boots", enchantItem(Material.IRON_BOOTS, enchants));
				break;
			case "Assassin":
				smith = getPotionItemStack(true, PotionType.SPEED, Material.SPLASH_POTION, 100, 1, k);
				k.kitData.put("primary", smith);
				k.kitData.put("initial_effect", new PotionEffect(PotionEffectType.INVISIBILITY, 60*20, 0));
				break;
			default:
				Bukkit.getLogger().info("Error: Kit not found when adding data");	
				break;
			}
		}
	}

	public static ArrayList<String> getKitNames() {
		ArrayList<String> r = new ArrayList<String>();
		for (Kit k : kits) {
			r.add(k.name);
		}
		return r;
	}
	
	public static ArrayList<Kit> getKits() {
		ArrayList<Kit> r = new ArrayList<Kit>();
		for (Kit k : kits) {
			r.add(k);
		}
		return r;
	}
	
	public static Kit returnKit(String name) {
		for (Kit k : kits) {
			if (k.name.equals(name)) {
				return k;
			}
		}
		return null;
	}
	
	public static ItemStack enchantItem(Material m, HashMap<Enchantment, Integer> enchantments) {
		ItemStack smith = new ItemStack(m);
		ItemMeta enchant = smith.getItemMeta();
		
		for (Enchantment e : enchantments.keySet()) {
			enchant.addEnchant(e, enchantments.get(e), true);
		}

		smith.setItemMeta(enchant);
		
		return smith;
	}
	
	public static ItemStack getPotionItemStack(boolean kit, PotionType type, Material material, int duration, int level, Kit k){
		PotionEffectType pet = type.getEffectType();
		
		ItemStack potion = new ItemStack(material, 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        meta.addCustomEffect(new PotionEffect(pet, duration, 0), true);
        String name = pet.getName().toLowerCase().replace("_", " ");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        meta.setDisplayName("Potion of " + name);
        meta.setColor(pet.getColor());
        potion.setItemMeta(meta);
        
        if (kit)
        	k.icon = new ItemStack(potion);
        
        return potion;
    }
}
