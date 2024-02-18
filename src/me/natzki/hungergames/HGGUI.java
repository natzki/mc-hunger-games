package me.natzki.hungergames;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public class HGGUI {
	
	private Player player;
	public ArrayList<String> purchasedKits = new ArrayList<String>();
	public Inventory kitGUI = Bukkit.createInventory(null, 27, "Kits");

	public HGGUI(Player player) {
		this.player = player;
		
		for (String kitName : Main.returnUserDataConfig().getConfigurationSection(player.getName()).getStringList("kits")) {
			purchasedKits.add(kitName);
		}
	}
	
	public void addInventories() {
		/////////////////////////////
		/////////////////////////////
		//////KitGUI/////////////////
		/////////////////////////////
		/////////////////////////////
		for (Kit k : Kit.getKits()) {
			
			ItemStack is = k.icon;
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(k.name);
			ArrayList<String> lore = new ArrayList<String>();
			
			if (k.type.equals("premium")) {
				if (!purchasedKits.contains(k.name)) {
					lore.add(ChatColor.RED + "" + ChatColor.BOLD + "[Locked]");
					lore.add("Cost: " + ChatColor.YELLOW + k.cost + ChatColor.RESET + " coins");
					lore.add("Click to purchase");
				}
			}
			
			for (String name : k.kitData.keySet()) {
				
				if (k.kitData.get(name) instanceof ItemStack) {
					ItemStack stack = (ItemStack) k.kitData.get(name);
					
					Map<Enchantment, Integer> ench = stack.getItemMeta().getEnchants();
					
					String itemName = stack.getType().name().replace("_", " ").toLowerCase();
					itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
					lore.add(ChatColor.DARK_AQUA + "" + stack.getAmount() + "x " + itemName);
					
					if (ench != null) {
						for (Enchantment n : ench.keySet()) {
							String enchName = n.getKey().toString();
							enchName = enchName.replace("minecraft:", "");
							enchName = enchName.replace("_", " ");
							enchName = enchName.substring(0, 1).toUpperCase() + enchName.substring(1);
							lore.add(ChatColor.AQUA + "  " + enchName + " " + ench.get(n).intValue());
						}
					}
					
					Material m = stack.getType();
					
					if (m == Material.POTION || m == Material.SPLASH_POTION || m == Material.LINGERING_POTION) {
						PotionMeta meta = (PotionMeta) stack.getItemMeta();
						String potionName = meta.getCustomEffects().get(0).getType().getName().replace("_", " ").toLowerCase();
						potionName = potionName.substring(0, 1).toUpperCase() + potionName.substring(1);
						
						if (meta.getCustomEffects().get(0).getDuration() > 1) {
							lore.add(ChatColor.AQUA + "  " + potionName + " " + meta.getCustomEffects().get(0).getDuration() / 20 + " seconds");
						} else {
							lore.add(ChatColor.AQUA + "  " + potionName);
						}
					}
				} else if (k.kitData.get(name) instanceof PotionEffect) {
					PotionEffect pat = (PotionEffect) k.kitData.get(name);
					String effectName = pat.getType().getName().replace("_", " ").toLowerCase();;
					effectName = effectName.substring(0, 1).toUpperCase() + effectName.substring(1);
					lore.add(ChatColor.WHITE + "Initial effect: " + ChatColor.DARK_AQUA + effectName + " " + pat.getDuration()/20 + " seconds");
				} else if (k.kitData.get(name) instanceof String) {
					
					//Reserved
					
				} 
			}
			
			im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			im.setLore(lore);
			is.setItemMeta(im);
			
			kitGUI.addItem(is);
			kitGUI.setMaxStackSize(10); //Identifier for HGListener method since getName() is not supported anymore...
		}
		
		////////////////////
		////////////////////
		////////////////////
		////////////////////
	}
	
	public boolean openKitGUI() {
		
		player.openInventory(kitGUI);
		
		return true;
	}
}
