package me.natzki.hungergames;

import java.util.ArrayList;
//import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;


public class HGListener implements Listener {
	
	private Main mainInstance;
	private static HGListener instance;
	
	public HGListener(Main plugin) {
		mainInstance = plugin;
		instance = this;
	}
	
	public static HGListener getInstance() {
		return instance;
	}
	
	public static boolean manuallyStarted, 
	gameStarting, 
	gameRunning, 
	deathmatch, 
	gameFinished,
	cooldown,
	PVPcooldownOn,
	firstTipMessage,
	deathMatchAble,
	deathMatchInitiated,
	deathMatchCooldown;
	
	public static ArrayList<String> dmVoters = new ArrayList<String>();
	
	public static String winnerName, secondName, thirdName = "Nobody?";
	
	public static int gameStartingSeconds,
	timeRemaining,
	timeElapsed,
	oneMinuteSecondsRemaining;
	
	public static Location spawn = new Location(Bukkit.getWorld("world_game"), 0, 0, 0);

	public static ArrayList<Biome> getSurroundingArea(Location loc, int area) {
		ArrayList<Biome> r = new ArrayList<Biome>();
		
		int blocks = area;
		
		for (int i = -blocks; i < blocks; i+=16) {
			Location a = new Location(loc.getWorld(), loc.getX() + i, loc.getY(), loc.getZ());
			//a.getChunk().load();
			r.add(a.getBlock().getBiome());
		}
		for (int i = -blocks; i < blocks; i+=16) {
			Location a = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + i);
			//a.getChunk().load();
			r.add(a.getBlock().getBiome());
		}
		for (int i = -blocks; i < blocks; i+=16) {
			Location a = new Location(loc.getWorld(), loc.getX() + i, loc.getY(), loc.getZ());
			//a.getChunk().load();
			r.add(a.getBlock().getBiome());
		}
		for (int i = -blocks; i < blocks; i+=16) {
			Location a = new Location(loc.getWorld(), loc.getX() + -i, loc.getY(), loc.getZ());
			//a.getChunk().load();
			r.add(a.getBlock().getBiome());
		}
		return r;
	}
	
	public static Location checkBiome(Location loc, int offset, int area) {
		ArrayList<Biome> bannedBiomes = new ArrayList<Biome>();
		bannedBiomes.add(Biome.OCEAN);
		bannedBiomes.add(Biome.COLD_OCEAN);
		bannedBiomes.add(Biome.DEEP_COLD_OCEAN);
		bannedBiomes.add(Biome.DEEP_FROZEN_OCEAN);
		bannedBiomes.add(Biome.DEEP_LUKEWARM_OCEAN);
		bannedBiomes.add(Biome.DEEP_OCEAN);
		//bannedBiomes.add(Biome.DEEP_WARM_OCEAN); //no longer valid in 1.18.1?
		bannedBiomes.add(Biome.FROZEN_OCEAN);
		bannedBiomes.add(Biome.LUKEWARM_OCEAN);
		bannedBiomes.add(Biome.WARM_OCEAN);
		bannedBiomes.add(Biome.BEACH);
		bannedBiomes.add(Biome.SNOWY_BEACH);
		bannedBiomes.add(Biome.RIVER);
		bannedBiomes.add(Biome.FROZEN_RIVER);
		
		ArrayList<Biome> surroundingArea = getSurroundingArea(loc, area);
		
		while (true) {
			if (bannedBiomes.containsAll(surroundingArea)) {
				loc.setX(loc.getX()+offset);
				loc.setZ(loc.getZ()+offset);
				loc.getChunk().load();
				surroundingArea = getSurroundingArea(loc, area);
			} else {
				if (loc.getBlock().isLiquid()) {
					loc.setX(loc.getX()+20);
					continue;
				}
				break;
			}
		}
		return loc;
	}
	
	public static Location randomLocation(double place, int offsetX, int offsetZ, boolean initiate) {
		double randomX = Math.round(Math.random() * offsetX);
		double randomZ = Math.round(Math.random() * offsetZ);
		float randomYaw = (float) Math.floor(Math.random() * 360);

		Location loc = new Location(Bukkit.getServer().getWorld("world_game"), place+randomX, 0, place+randomZ, randomYaw, 0);
		
		if (initiate) {
			loc = checkBiome(loc, 2000, 16);
			loc.getChunk().load();
		}
		
		loc.setY(loc.getWorld().getHighestBlockYAt(loc));
		
		while (true) {
			if (loc.getBlock().getType() == Material.LAVA) {
				loc.setX(loc.getX()+20);
			} else {
				break;
			}
		}
		
		return loc;
	}
	
	public static void giveKitAndTeleportEveryone() {
		
		World gameWorld = Bukkit.getServer().getWorld("world_game");
		gameWorld.setTime(0);
		gameWorld.setDifficulty(Difficulty.NORMAL);
		
		
		Location checkArea = randomLocation(10000, 0, 0, true); //define the coordinates of spawn (first argument). 0 will end up in a forest biome, also recommended is 5000 or 10000
		spawn = checkArea;
		double place = checkArea.getX();
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			
			p.teleport(randomLocation(place, 75, 75, false)); //define the offset between players with offsetX and offsetY (75, 75 is ideal)
			p.setGameMode(GameMode.SURVIVAL);
			p.setFireTicks(0);
			p.setAllowFlight(false);
			p.setFlying(false); //to be safe
			p.setFallDistance(0); //so the player doesn't die if he is already falling
			p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 15, 1);
		}
	
		for (Contributor player : Main.players) {
			
			Kit kit = Kit.returnKit(player.kit);
			
			player.getPlayer().getInventory().clear();
			player.getPlayer().updateInventory();
			player.giveKit(kit);
			player.trackNearest();
		}
		
		HGScoreboard.updateGameScoreboard();
	}

	public static boolean checkPlayersInitialCheck;
	
	public void checkPlayerCount(Player currentPlayer) {
		
		if (Main.players.size() <= Main.returnConfig().getInt("deathmatch_players") && timeElapsed >= 10 && gameRunning && !deathmatch && !deathMatchAble) {
			deathMatchAble = true;
			Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "To vote for deathmatch please type /dm");
		}
		
		if (Main.players.size() == 2 && !gameFinished) {
			thirdName = currentPlayer.getName();
		}
		
		if (Main.players.size() == 1 && !gameFinished) {
			secondName = currentPlayer.getName();
			winnerName = Main.players.get(0).name;
			mainInstance.finishGame();
		}	
	}
	
	public void checkDeathMatchAble() {
		
	}
	
	public void playerRespawnToSpectator(Player p, boolean joined) {
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setLevel(0);
		p.setExp(0);
		
		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		
		p.setAllowFlight(true);
		p.setFlying(true);
		
		for (Player players : Bukkit.getOnlinePlayers()) {
			players.hidePlayer(mainInstance, p);
			if (!Contributor.exists(players)) {
				p.hidePlayer(mainInstance, players);
			}
		}
		
		
		/* Voting Service
		ItemStack votePaper = new ItemStack(Material.PAPER, 1);
		ItemMeta paperMeta = votePaper.getItemMeta();
		paperMeta.setDisplayName(ChatColor.BOLD + "Vote for rewards");
		votePaper.setItemMeta(paperMeta);
		*/
		
		ItemStack trackingCompass = new ItemStack(Material.COMPASS, 1);
		ItemMeta trackingCompassMeta = trackingCompass.getItemMeta();
		trackingCompassMeta.setDisplayName(ChatColor.YELLOW + "" +   ChatColor.BOLD +"Teleport");
		trackingCompass.setItemMeta(trackingCompassMeta);
		
		playerGiveDelay(trackingCompass, 4, p, joined); //Can't give compass to player the same moment he respawns, player cant see other people when teleported instantly when he joins
		//playerGiveDelay(votePaper, 8, p, false); //set joined to false since we don't need to teleport twice
	}
	
	public void playerGiveDelay(ItemStack is, int slot, Player p, boolean joined) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(mainInstance, new Runnable() {
		    @Override
			public void run() {
		    	Player player = Bukkit.getServer().getPlayer(p.getName());
		    	
		    	if (player == null)
		    		return;
		    	
		    	if (player.getInventory() != null)
		    		player.getInventory().setItem(slot, is);
				
				if (joined && Main.players.size() > 0) 
					p.teleport((Player) Main.players.get(0).getPlayer());
		    }
		}, 20L);
	}
	
	public void registerPlayer(Player player, String kit, boolean in) {
		
		if (Bukkit.getServer().getOnlinePlayers().size() > Main.returnConfig().getInt("max_players")) {
			player.kickPlayer(Main.returnConfig().getString("full"));
			return;
			
		}
		
		//Reset location, inventory, armor, effects
		PlayerInventory inventory = player.getInventory();
		inventory.clear();
		inventory.setArmorContents(new ItemStack[4]);
		Location loc = new Location(Bukkit.getWorld("world"), -145, 9, 405, -90, 0);
		
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
		player.setFireTicks(0);
		player.setAllowFlight(false);
		player.setFlying(false);
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	
		if (in) {
			Contributor current = new Contributor(player);
			current.setKit(kit);
			Main.players.add(current);
			
			player.setGameMode(GameMode.SURVIVAL);
			player.setFallDistance(0); //so the player doesn't die if he is already falling
			player.teleport(HGListener.randomLocation(HGListener.spawn.getX(), 60, 60, false));
			//player.getPlayer().getInventory().clear();
			//player.getPlayer().updateInventory();
			
			current.giveKit(Kit.returnKit(current.kit));
			
			strikeLightning();
			
			Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " has joined the game!");
			
			HGScoreboard.updateGameScoreboard();
			return;
			
		} else if (gameRunning || gameFinished) {
			HGScoreboard.updateGameScoreboard();
			playerRespawnToSpectator(player, true);
			
			if (Main.returnConfig().getBoolean("notify_ops_ingame_on_playerjoin")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.isOp())
						p.sendMessage(ChatColor.GRAY + "[Private Notification] " + ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " has joined!");
				}
			}
			
			return;
		} else {
			Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " has joined!");
			//Selectkit message (deprecated)
			//player.sendMessage(new Message("selectkit").getConfigMessage(Main.returnConfig()));	
			
			Contributor current = new Contributor(player);
			Main.players.add(current);
			
			player.teleport(loc);
			
			ItemStack kitChest = new ItemStack(Material.CHEST, 1);
			ItemMeta chestMeta = kitChest.getItemMeta();
			chestMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Select kit");
			kitChest.setItemMeta(chestMeta);
			
			/* For voting service
			
			ItemStack votePaper = new ItemStack(Material.PAPER, 1);
			ItemMeta paperMeta = votePaper.getItemMeta();
			paperMeta.setDisplayName(ChatColor.BOLD + "Vote for rewards");
			votePaper.setItemMeta(paperMeta);
			
			*/
			
			playerGiveDelay(kitChest, 4, player, false); //set joined to false since the game is not running
			
			//playerGiveDelay(votePaper, 8, player, false);
			
			HGScoreboard.updateOnlineScoreboard();
			
			if ((Main.players.size() >= Main.returnConfig().getInt("min_players")) && !gameStarting && !cooldown) {
				mainInstance.startGame();
			}
		}
	}
	
	public void deathDropInventory(Player damaged) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(mainInstance, new Runnable() {
		    @Override
			public void run() {
		    	for (ItemStack itemStack : damaged.getInventory().getContents()) {
		    		if (itemStack != null) {
		    			damaged.getWorld().dropItemNaturally(damaged.getLocation(), itemStack);
		    			damaged.getInventory().remove(itemStack);
		    		}
		    	    
		    	}

		    	//No need to loop through the armor contents... no idea why
		    	
		    	damaged.getInventory().setArmorContents(new ItemStack[] { null, null, null, null } );
		    }
		}, 5L);
	}
	
	public static void strikeLightning() {
		for (Contributor c : Main.players) {
			Location loc = c.getPlayer().getLocation().add(0, 255, 0);
			Bukkit.getWorld("world_game").strikeLightning(loc);
		}
	}
	
	public static void checkVotes() {
		if (deathmatch || gameFinished)
			return;
		
		if (dmVoters.size() == Main.players.size() && !deathMatchInitiated) {
			Main.getInstance().startDeathMatch();
		}
	}
	
	public void handlePlayerQuit(Player player, String message, boolean respawn, boolean dropInv) {
		Contributor c = Contributor.getContributor(player);
		
		if (respawn)
			playerRespawnToSpectator(player, false);
		
		if (dropInv)
			deathDropInventory(player);
		
		c.removeContributor();
		
		strikeLightning();
		
		checkPlayerCount(player);
		
		if (dmVoters.contains(player.getName())) {
			dmVoters.remove(player.getName());
		}
		
		if (!deathmatch) {
			checkVotes();
		}

		HGScoreboard.updateGameScoreboard();
		
		Bukkit.broadcastMessage(message);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(""); //nullifying does not work
		Player player = (Player) event.getPlayer();
		registerPlayer(player, "", false);
		
		if (Main.returnConfig().getBoolean("remind_voting")) {
			player.sendMessage(Main.returnConfig().getString("remind_voting_message"));
		};
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		Player player = (Player) event.getPlayer();
		
		if ((gameRunning || gameFinished) && Contributor.exists(player)) {
			String deathMsg = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " left the game!";
			
			handlePlayerQuit(player, deathMsg, false, true);
			return;
		} else if (!gameRunning && !gameFinished) {
			Contributor c = Contributor.getContributor(player);
			c.removeContributor();
			HGScoreboard.updateOnlineScoreboard();
		}
	}

	@EventHandler
	public void onPlayerKill(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		
		Player player = event.getEntity();
		
		if (!(player.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
			String deathcause = player.getLastDamageCause().getCause().name().toLowerCase();
			String deathMessage = new Message("otherdeath_" + deathcause, player).getMessage();

			handlePlayerQuit(player, deathMessage, true, false);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {

		//Check if the damager is a spectator
		if (e.getDamager() instanceof Player) {
			
			if (!Contributor.exists((Player) e.getDamager()) || deathMatchCooldown) {
				e.setCancelled(true);
				return;
			}
		}
		
	    if (e.getEntity() instanceof Player) {

	    	Player damaged = (Player) e.getEntity();
	    	
	    	if (!gameRunning || !Contributor.exists(damaged) || PVPcooldownOn || deathMatchCooldown) {
	    		e.setCancelled(true);
				return;
	    	}

	    		
	    	if (damaged.getHealth() <= e.getDamage()) {
	    		
	    		String deathMessage = "deathMessage has not been defined";
	    		
	    		//Player
	    		if (e.getDamager() instanceof Player) {
	    			
	    			Player damager = (Player) e.getDamager();
	    			
	    			damager.setLevel(damager.getLevel() + 1);
	    			
	    			Contributor killer = Contributor.getContributor((Player) e.getDamager());
					killer.updateKills();
					
					String lastItem = damager.getInventory().getItemInMainHand().getType().name().replace("_", " ").toLowerCase();
					lastItem = lastItem.substring(0, 1).toUpperCase() + lastItem.substring(1);
					if (lastItem.equals("Air"))
						lastItem = "Bare fists";
					
					deathMessage = ChatColor.YELLOW + damaged.getName() + ChatColor.WHITE + " was killed by " + ChatColor.YELLOW + damager.getName() + ChatColor.WHITE + " using " + ChatColor.AQUA + lastItem + ChatColor.WHITE + "!";
	    		//Mob
	    		} else if (e.getDamager() instanceof LivingEntity && !(e.getDamager() instanceof Player)) {
	    			
	    			LivingEntity damager = (LivingEntity) e.getDamager();

		            deathMessage = ChatColor.YELLOW + damaged.getName() + ChatColor.WHITE + " was killed by " + ChatColor.AQUA + damager.getName() + ChatColor.WHITE + "!";

		            if (damager instanceof Mob) {
		            	((Mob) damager).setTarget(null);
		            }
	    		//Explosive
	    		} else if (e.getDamager() instanceof Explosive || e.getDamager() instanceof EnderCrystal || e.getDamager() instanceof ExplosiveMinecart) {
	    			deathMessage = ChatColor.YELLOW + e.getEntity().getName() + ChatColor.WHITE + " exploded into a million pieces!";
	    		//Projectile
	    		} else if (e.getDamager() instanceof Projectile || e.getDamager() instanceof AreaEffectCloud) {
	    			Projectile projectile = (Projectile) e.getDamager();
	    			LivingEntity shooter = (LivingEntity) projectile.getShooter();
	    			
	    			if (e.getDamager() instanceof Arrow) {
	    				if (shooter instanceof Player) {
	    					deathMessage = ChatColor.YELLOW + damaged.getName() + ChatColor.WHITE + " was killed by " + ChatColor.YELLOW + shooter.getName() + ChatColor.WHITE + " using " + ChatColor.AQUA + "Archery" + ChatColor.WHITE + "!";
	    				} else {
	    					deathMessage = ChatColor.YELLOW + damaged.getName() + ChatColor.WHITE + " was killed by " + ChatColor.AQUA + shooter.getName() + ChatColor.WHITE + "!";
	    				}
	    			} else if (e.getDamager() instanceof ThrownPotion || e.getDamager() instanceof AreaEffectCloud) {
	    				if (shooter instanceof Player) {
	    					deathMessage = ChatColor.YELLOW + damaged.getName() + ChatColor.WHITE + " was poisoned fatally by " + ChatColor.YELLOW + shooter.getName() + ChatColor.WHITE + "!";
	    				} else {
	    					deathMessage = ChatColor.YELLOW + damaged.getName() + ChatColor.WHITE + " was poisoned fatally by " + ChatColor.AQUA + shooter.getName() + ChatColor.WHITE + "!";
	    				}
	    			} else if (e.getDamager() instanceof LlamaSpit) {
	    				deathMessage = new Message("otherdeath_llama", damaged).getMessage();
	    			}
	    		//Falling block	
	    		} else if (e.getDamager() instanceof FallingBlock) {
	    			deathMessage = new Message("otherdeath_falling_block", damaged).getMessage();
	    		//Lightning (subinterface of weather, will they add more?)
	    		}  else if (e.getDamager() instanceof LightningStrike) {
	    			deathMessage = new Message("otherdeath_lightning", damaged).getMessage();
	    		} else {
	    			deathMessage = "EntityDamageByEntity damager entity not found.";
	    		}

	    		e.setCancelled(true);
	    		
	    		handlePlayerQuit(damaged, deathMessage, true, true);
	    		
	    	}
	    }  
	}
	
	@EventHandler
    public void onDamageOther(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player){
        	if (!gameRunning || !Contributor.exists((Player) event.getEntity()) || deathMatchCooldown) {
        		event.setCancelled(true);
                return;
        	}
        }
        
    }
    
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer() != null) {
			
			Player player = event.getPlayer();
			
			if ((!gameRunning && !player.isOp() && !gameFinished) || !Contributor.exists(player) || Contributor.getContributor(player).isOpting || deathMatchCooldown) {
				event.setCancelled(true);
				return;
			}
		}
		
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer() != null) {
			
			Player player = event.getPlayer();
			
			if ((!gameRunning && !player.isOp() && !gameFinished) || !Contributor.exists(player) || Contributor.getContributor(player).isOpting || deathMatchCooldown) {
				event.setCancelled(true);
				return;
			}
		}
		
	}
	
	@EventHandler
	public void onMobPlayerTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			if (!Contributor.exists((Player) event.getTarget())) {
				event.setCancelled(true);
				return;
			}		
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if ((!gameRunning && !gameFinished && !event.getPlayer().isOp()) || !Contributor.exists((Player) event.getPlayer()))
			event.setCancelled(true);
		return;
	}
	
	@EventHandler
	public void onItemPickup(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player) {
			if (!Contributor.exists((Player) event.getEntity()))
				event.setCancelled(true);
			return;
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onArrowPickup(PlayerPickupArrowEvent event) {
		if (!Contributor.exists((Player) event.getPlayer()))
			event.setCancelled(true);
		return;
	}
	
	@EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        if (e.getInventory().getHolder() instanceof InventoryHolder && !Contributor.exists((Player) e.getPlayer())){
            e.setCancelled(true);
            return;
        }
    }
	
	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getEntered() instanceof Player && !Contributor.exists((Player) event.getEntered())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String eventMessage = event.getMessage();
		Player eventPlayer = event.getPlayer();
		
		//Complications arise when format symbols are used such as %s or even %
		if (eventMessage.contains("%")) 
			eventMessage = eventMessage.replace("%", "%%");
		 
		String format = "<rank>" + ChatColor.GRAY + "<status>" + ChatColor.GREEN + "<player>" + ChatColor.WHITE + ": <message>";
		//String format = "<group-prefix><player><group-suffix>:<message>";
		
		//replacing your values
		//format.replace("<group-prefix>", MyPermissionManager.getPlayerGroupPrefix(player)); //something like that
		//format.replace("<group-suffix>", MyPermissionManager.getPlayerGroupSuffix(player)); //something like that
		if (eventPlayer.isOp()) {
			format = format.replace("<rank>", ChatColor.RED + "" + ChatColor.BOLD + "[Admin] ");
		} else {
			format = format.replace("<rank>", ChatColor.GRAY + "[User] ");
		}
		if (!Contributor.exists(eventPlayer) && (gameRunning || gameFinished)) {
			format = format.replace("<status>", ChatColor.ITALIC + "[Spectator] ");
		} else {
			format = format.replace("<status>", "");
		}	
		
		format = format.replace("<player>", eventPlayer.getName()); //the player name will be automatically replaced by player.getName() you could write "%s" too but if you do it like that, you can place the message before the player's name
		format = format.replace("<message>", eventMessage);
		
		event.setFormat(format);
		event.setMessage("we are writing something before each message" + eventMessage); //do not change the message with setFormat(), use setMessage() instead!
	}
	
	public static void trackPlayer() {	
		//TODO
		//Tracking for spectators, discouraged but may prove useful...
		//Timer t = new Timer(Main.getInstance(), 100, "tracking_spectator", 10L);
		//t.startTimer();	
	}
	
	 public static Player getNearest(Player p, Double range, Double rangeY) {
		 double distance = Double.POSITIVE_INFINITY; // To make sure the first player checked is closest
		 Player target = null;
		 for (Entity e : p.getNearbyEntities(range, rangeY, range)) {
			 if (!(e instanceof Player))
				 continue;
			 if(e == p) //Added this check so you don't target yourself.
				 continue;
			 if (!(Contributor.exists((Player) e))) //check if the target isn't a spectator
				 continue;
			 double distanceto = p.getLocation().distance(e.getLocation());
			 if (distanceto > distance)
				 continue;
			 distance = distanceto;
			 target = (Player) e;
		 }
		 return target;
	 }
	 
	 @EventHandler
	 public void onIventoryClick(InventoryClickEvent event) {
		 Player player = (Player) event.getWhoClicked();
		 Inventory inventory = event.getInventory();
		 
		 //if (inventory.getName().equals("Kits") && event.getSlotType() != SlotType.OUTSIDE) {
		 if ((inventory.getMaxStackSize() == 10) && event.getSlotType() != SlotType.OUTSIDE) {
			 ItemStack clicked = event.getCurrentItem();
			 
			 if (clicked != null) {
				 
				 Contributor c = Contributor.getContributor(player);
				 String kitName = clicked.getItemMeta().getDisplayName();
				 
				 if (!Kit.returnKit(kitName).type.equals("free") && !Main.returnUserDataConfig().getConfigurationSection(player.getName()).getStringList("kits").contains(kitName)) {
					 if (c.buyKit(kitName)) {
						 player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 15, 0);
					 } else {
						 player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 15, 1);
						 event.setCancelled(true);
						 return;
					 }
				 }
				 
				 c.setKit(kitName);
				 
				 event.setCancelled(true);
				 player.closeInventory();
				 return;
			 }
				 
		 }
		 
		 //if (inventory.getName().equals(ChatColor.BOLD + "Teleport") && event.getSlotType() != SlotType.OUTSIDE) {
		 if ((inventory.getMaxStackSize() == 20) && event.getSlotType() != SlotType.OUTSIDE) {
			 ItemStack clicked = event.getCurrentItem();
			 
			 if (clicked != null) {

				 
				 String playerName = clicked.getItemMeta().getDisplayName().replace(ChatColor.RESET + "Teleport to " + ChatColor.YELLOW + "" + ChatColor.BOLD, "");
				 player.teleport(Bukkit.getPlayer(playerName));
				 
				 event.setCancelled(true);
				 player.closeInventory();
				 return;
			 }
		 }
	 }
	 
	 //Voting service
	 //private HashMap<String, Long> cooldownPaper = new HashMap<String, Long>();

	 @EventHandler
     public void onPlayerThrow(PlayerInteractEvent event) {
         if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null) {
        	 
        	 Player p = event.getPlayer();
        	 
        	 if (event.getItem().getType() == Material.CHEST) {
        		 if (!gameRunning && !gameFinished) {
					 HGGUI GUI = new HGGUI(p);
					 GUI.addInventories();
					 GUI.openKitGUI();
					 GUI = null;
				 }
        		 return;
        	 }
        	 
        	 //Voting service
        	 /*
        	 if (event.getItem().getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Vote for rewards")) {
        		 int cooldownTime = 13; 
        		 
        		 if (cooldownPaper.containsKey(p.getName())) {
        			long secondsLeft = ((cooldownPaper.get(p.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
     	            if (secondsLeft > 0) {
     	                return;
     	            }
        	     }
        		 
        		 cooldownPaper.put(p.getName(), System.currentTimeMillis());
        		 
        		 p.sendMessage(ChatColor.GREEN + "Vote for our server to recieve rewards!");
        		 
        		 for (String site : Main.returnConfig().getStringList("voting_sites")) {
        			 p.sendMessage(site);
        		 }
        		 
        		 cooldownPaper.put(p.getName(), System.currentTimeMillis());
        		 
        		 return;
        	 }
        	 */
        	 
        	 if ((event.getItem().getType() == Material.FIRE_CHARGE) && gameRunning) {
        		 if (deathMatchCooldown) {
        			 event.setCancelled(true);
        			 return;
        		 }
        		 
                 Projectile fireball;
                 int fb_speed = 1;
                 int fb_amount = event.getItem().getAmount();
                 final Vector fb_direction = p.getEyeLocation().getDirection().multiply(fb_speed);
                 p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT, 10, 1);
                 fireball = p.getWorld().spawn(p.getEyeLocation().add(fb_direction.getX(), fb_direction.getY(), fb_direction.getZ()), Fireball.class);
                 fireball.setShooter((ProjectileSource) p);
                 fireball.setVelocity(fb_direction);
                 
                 if (fb_amount > 1) {
                     event.getItem().setAmount(fb_amount - 1);
                     return;
                 } else {
                     p.getInventory().clear(p.getInventory().getHeldItemSlot());
                 }
                 return;
             }
        	 
        	 if (event.getItem().getType() == Material.COMPASS && !Contributor.exists(p) && (gameRunning || gameFinished)) {

        		 Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BOLD + "Teleport");
        		 inv.setMaxStackSize(20); //Identifier for HGListener method since getName() is not supported anymore...
        		 
        		 for (int i = 0; i < Main.players.size(); i++) {
        			 
        			 Contributor c = Main.players.get(i);
        			 
        			 ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
                     SkullMeta meta = (SkullMeta) skull.getItemMeta();
                     meta.setOwningPlayer(c.getPlayer());
                     meta.setDisplayName(ChatColor.RESET + "Teleport to " + ChatColor.YELLOW + "" + ChatColor.BOLD + c.name);
                     skull.setItemMeta(meta);
                     
                     inv.setItem(i, skull);
                     
        		 }
        		 p.openInventory(inv);
        		 return;
        	 }
         }
     }
	 
	 @EventHandler
	 public void onBombExplode(EntityExplodeEvent event){
	     if(event.getEntity() instanceof TNTPrimed && !gameRunning && !gameFinished){
	         event.blockList().clear();
	         event.setCancelled(true);
	     }
	 }
	 
	 @EventHandler
	 public void onWeatherChange(WeatherChangeEvent event) {
		 String worldname = event.getWorld().getName();
		 
		 boolean rain = event.toWeatherState();
		 if(rain && worldname.equals("world"))
		 	event.setCancelled(true);
	 	}
	  
	 @EventHandler
	 public void onThunderChange(ThunderChangeEvent event) {
		 String worldname = event.getWorld().getName();
		 
		 boolean storm = event.toThunderState();
	     if(storm && worldname.equals("world"))
	    	 event.setCancelled(true);
	 }
	 
	 @EventHandler
	    public void countDown(final ServerListPingEvent event) {
		 	String suffix = timeRemaining == 1 ? " minute" : " minutes";
		 
	        if (!gameStarting && !gameRunning && !gameFinished) {
	        	event.setMotd("\u00A7r             \u00A74\u00A7LMinecraft-HG   \u00A7a\u00A7L[Open]\n           \u00A7f\u00A77\u00A7nNew Classical Hunger Games!");
	        	return;
	        }
	        if (gameStarting) {
	        	event.setMotd("\u00A7r        \u00A74\u00A7LMinecraft-HG   \u00A7e\u00A7L[Game Starting...]\n           \u00A7f\u00A77\u00A7nNew Classical Hunger Games!");
	        	return;
	        }
	        if (gameRunning) {
	        	if (deathmatch) {
	        		event.setMotd("\u00A7r         \u00A74\u00A7LMinecraft-HG   \u00A7c\u00A7L[Game Running]\n           \u00A7e\u00A76Time Remaining: \u00A7f0 minutes");
	        		return;
	        	} else {
	        		event.setMotd("\u00A7r         \u00A74\u00A7LMinecraft-HG   \u00A7c\u00A7L[Game Running]\n           \u00A7e\u00A76Time Remaining: \u00A7f" + timeRemaining + suffix);
	        		return;
	        	}
	        }
	        if (gameFinished) {
	        	event.setMotd("\u00A7r      \u00A74\u00A7LMinecraft-HG   \u00A7a\u00A7L[Game Finishing...]\n           \u00A7e\u00A76Time Remaining: \u00A7f" + timeRemaining + suffix);
	        	return;
	        }
	    }
	 

	 @EventHandler
	 public void onPlayerTeleport(PlayerTeleportEvent event) {
		 Player player = event.getPlayer();
		 if (!Contributor.exists(player)) {
			 return;
		 }
		 for (Contributor online : Main.players) {
			 	online.getPlayer().hidePlayer(mainInstance, player);
			 	online.getPlayer().showPlayer(mainInstance, player);
			 	player.hidePlayer(mainInstance, online.getPlayer());
			 	player.showPlayer(mainInstance, online.getPlayer());
		 }
	 }
	 
	 @EventHandler
	 public void onHotbarSwitch(PlayerItemHeldEvent event) {
		 
		 if (!gameRunning && !gameFinished) {
			 return;
		 }
		 
		 Player player = event.getPlayer();
		 ItemStack item = player.getInventory().getItem(event.getNewSlot());
		 
		 if (item != null && item.getType() == Material.COMPASS) {
			 Contributor c;
			 
			 if (Contributor.exists(player)) {
				 c = Contributor.getContributor(player); 
			 }
			 else {
				 return; 
			 }
				 
			 c.trackNearest();
		 }
	 }
	 
	 @EventHandler
	 public void onPlayerMove(PlayerMoveEvent event) {
		 if (deathMatchCooldown && Contributor.exists(event.getPlayer()))
			 event.setCancelled(true);
	 }
}
