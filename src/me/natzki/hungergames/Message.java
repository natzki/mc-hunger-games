package me.natzki.hungergames;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Message {
	
	String name;
	Player player;
	Entity entity;
	
	public Message(String name, Player player) {
		this.name = name;
		this.player = player;
	}
	
	public String getMessage() {
		
		String prologue = ChatColor.YELLOW + player.getName() + ChatColor.WHITE;
		
		switch (name) {
			case "otherdeath_fall":
				return prologue + " fell to death!";
			case "otherdeath_block_explosion":
				return prologue + " exploded into a million pieces!";
			case "otherdeath_contact":
				return prologue + " got spiked to death!";
			case "otherdeath_cramming":
				return prologue + " was crammed into too tiny spaces!";
			case "otherdeath_dragon_breath":
				return prologue + " got vaporized by the dragon's breath!";
			case "otherdeath_fly_into_wall":
				return prologue + " flew straight into a wall!";
			case "otherdeath_suicide":
				return prologue + " died!";
			case "otherdeath_void":
				return prologue + " fell into the void!";
			case "otherdeath_wither":
				return prologue + " withered away!";
			case "otherdeath_drowning":
				return prologue + " drowned to death!";
			case "otherdeath_fire":
				return prologue + " burned to death!";
			case "otherdeath_fire_tick":
				return prologue + " burned to death!";
			case "otherdeath_lava":
				return prologue + " burned to death!";
			case "otherdeath_hot_floor":
				return prologue + " burned to death!";
			case "otherdeath_lightning":
				return prologue + " was hit fatally by lightning!";
			case "otherdeath_starvation":
				return prologue + " starved to death!";
			case "otherdeath_suffocation":
				return prologue + " suffocated!";
			case "otherdeath_llama":
				return prologue + " was literally spit to death by a llama. Actual cancer.";
			case "otherdeath_falling_block":
				return prologue + " was hit fatally by a falling block!";
			case "thorns":
				return " was torn to death by thorns!";
				
			//Error handling
			case "otherdeath_projectile":
				return "Error: " + name + " deathcause called!";
			case "otherdeath_magic":
				return "Error: " + name + " deathcause called!";
			case "otherdeath_poison":
				return "Error: " + name + " deathcause called!";
			case "otherdeath_entity_attack":
				return "Error: " + name + " deathcause called!";
			case "otherdeath_entity_explosion":
				return "Error: " + name + " deathcause called!";
			case "otherdeath_entity_sweep_attack":
				return "Error: " + name + " deathcause called!";
			default:
				return "No message found! Please check syntax. DamageCause: " + ChatColor.GRAY + name;
		}
	}
}
