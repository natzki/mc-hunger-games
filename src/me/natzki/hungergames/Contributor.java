package me.natzki.hungergames;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class Contributor {
	private int taskID;
	private boolean taskRunning;
	
	public String name;
	
	private FileConfiguration userData = Main.returnUserDataConfig();
	private ConfigurationSection section;
	
	public String kit = "Rogue";
	public String ability = "";
	public int kills = 0;
	
	public boolean isOpting;
	
	public Contributor(Player player) {
		this.name = player.getName();
		this.section = userData.getConfigurationSection(name);
		
		if (section == null) {
			userData.createSection(name);
			ConfigurationSection section = userData.getConfigurationSection(name);
			
			section.set("kills", 0);
			section.set("coins", 0);
			section.set("wins", 0);
			section.set("wins", 0);
			section.set("kits", "");
			
			this.section = section;
		}
		
		Main.saveUserData(userData);
	}
	
	public void stopTask() {
		Bukkit.getScheduler().cancelTask(taskID);
		taskRunning = false;
	}
	
	public void trackNearest() {
		
		if (taskRunning)
			return;
		
		taskRunning = true;
		
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {  	
            @Override 
            public void run() {
            	
            	if (getPlayer() == null)
            		return;
            	
            	ItemStack item = getPlayer().getInventory().getItemInMainHand();
            	
            	if (item.getType() != Material.COMPASS) {
            		stopTask();
            		return;
            	}
            		
            	
            	Player tracked = HGListener.getNearest(getPlayer(), (double) 10000, (double) 260);
        		String trackedName;
        		double distance;
        			
        		if (tracked != null) {
        			trackedName = tracked.getName();
        			distance = getPlayer().getLocation().distance(tracked.getLocation());
        			distance = Math.round(distance*10.0)/10.0;
        			getPlayer().setCompassTarget(tracked.getLocation());
        		} else {
        			trackedName = "Nobody";
        			distance = 0;
        		}
        			
        		ItemMeta itemMeta = item.getItemMeta();
        		itemMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD +  trackedName + ChatColor.WHITE + " Distance: " + ChatColor.YELLOW + distance + " blocks");
        		item.setItemMeta(itemMeta);
            }
            
        }, 0L, 10L);
		
		Main.tasks.put("trackerrr", taskID);
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(this.name);
	}
	
	public void setKit(String name) {
		if (Kit.getKitNames().contains(name)) {
			
			if (!Kit.returnKit(name).type.equals("free") && !section.getStringList("kits").contains(name)) {
				getPlayer().sendMessage(ChatColor.RED + "You have not unlocked this kit yet!");
				return;
			}
			
			this.kit = name;
			getPlayer().sendMessage(ChatColor.RED + "You have selected " + name + " as your kit.");
			
		} else {
			getPlayer().sendMessage(ChatColor.RED + "Please select a valid kit!");
		}
	}
	
	public boolean buyKit(String name) {
		if (getCoins() < Kit.returnKit(name).cost) {
			return false;
		} else {
			List<String> list = section.getStringList("kits");
			list.add(name);
			section.set("kits", list);
			
			setCoins(-Kit.returnKit(name).cost); //Saves the UserData file automatically
			return true;
		}
	}
	
	public void giveKit(Kit currentKit) {
		Inventory i = this.getPlayer().getInventory();
		Player p = this.getPlayer();
		
		for (Object data : currentKit.kitData.values()) {
			if (data instanceof String) {
				this.ability = (String) data;
			} else if (data instanceof ItemStack) {
				i.addItem((ItemStack) data);
			} else if (data instanceof PotionEffect) {
				p.addPotionEffect((PotionEffect) data);
			}
			
		}
		p.getInventory().addItem(currentKit.compass);
	}
	
	public void updateKills() {
		this.kills++;
		
		section.set("kills", section.getInt("kills") + 1);
		setCoins(2); //Saves the UserData file automatically
	}
	
	public void updateWins() {
		section.set("wins", section.getInt("wins") + 1);
		setCoins(8); //Saves the UserData file automatically
		
	}
	
	public int getCoins() {
		return section.getInt("coins");
	}
	
	public ArrayList<String> getPremiumKits() {
		ArrayList<String> r = new ArrayList<String>();
		for (String kit : section.getStringList("kits")) {
			r.add(kit);
		}
		return r;
	}
	
	public void setCoins(int amount) {
		section.set("coins", section.getInt("coins") + amount);
		Main.saveUserData(userData);
	}
	
	public int getTotalKills() {
		return Main.returnUserDataConfig().getConfigurationSection(this.name).getInt("kills");
	}
	
	public static Contributor getContributor(Player p) {
		for (Contributor c : Main.players) {
			
			if (p.getName().equals(c.name)) {
				return c;
			}
				
		}
		return null;
	}
	
	public static Contributor getContributor(String name) {
		for (Contributor c : Main.players) {
			
			if (name.equals(c.name)) {
				return c;
			}
				
		}
		return null;
	}
	
	public static ArrayList<String> getContributorNames() {
		
		ArrayList<String> contributorNames = new ArrayList<String>();
		
		for (Contributor contributor : Main.players) {
			contributorNames.add(contributor.name);
		}
		
		return contributorNames;
	}
	
	public void removeContributor() {
		Main.players.remove(this);
	}
	
	public static boolean exists(Player p) {
		for (int i = 0; i < Main.players.size();i++) {
			
			Contributor current = Main.players.get(i);
			
			if (p.getName().equals(current.name)) {
				return true;
			}
				
		}
		return false;
	}
	
}
