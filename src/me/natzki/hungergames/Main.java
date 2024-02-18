package me.natzki.hungergames;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileDeleteStrategy;
//import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.util.FileUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

//import com.google.common.base.Joiner;

public class Main extends JavaPlugin {
	
	private static Main mainInstance;
	private static FileConfiguration config;
	private static File userDataFile;
	private static FileConfiguration userDataConfig;
	private static File serverProperties;
	
	public static ArrayList<Contributor> players = new ArrayList<Contributor>();
	
	public static Main getInstance() {
		return mainInstance;
	}
	
	public static FileConfiguration returnConfig() {
		return config;
	}
	
	public static FileConfiguration returnUserDataConfig() {
		return userDataConfig;
	}
	
	public static void saveUserData(FileConfiguration f) {
		try {
			f.save(userDataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static String getPropertyString(String s) {
        Properties pr = new Properties();
        try {
            FileInputStream in = new FileInputStream(serverProperties);
            pr.load(in);
            String string = pr.getProperty(s);
            return string;
        }
        catch (IOException e) {

        }
        return "";
    }
	
	@Override
    public void onEnable() {
		
		String root = new File(".").getAbsolutePath();
        File properties = new File(root+File.separator+"server.properties");
        serverProperties = properties;
		
        /*
		//This is only for my personal server
		if (getPropertyString("level-name").equals("SH")) {
			Bukkit.getLogger().info("[Hunger Games] World SH has been set as the level, disabling server");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		*/
		
        PluginManager pm = getServer().getPluginManager();
        HGListener listener = new HGListener(this);
        pm.registerEvents(listener, this);
        
        mainInstance = this;

        config = getConfig();
        setDefaultConfigValues();
        config.options().header("Here you can set the various options for the Hunger Games plugin\n"
        		+ "Remove to set back to default\n"
        		+ "OPTIONS\n"
        		+"pvp_cooldown_seconds: How long until PvP should be enabled when the game starts\n"
        		+"game_finish_time_seconds: How long until the server restarts when the game is finished\n"
        		+"game_timer_minutes: How long should the game take until deathmatch starts\n"
        		+"game_start_time_seconds: How long until the game starts when the server is full\n"
        		+"deathmatch_players: At how many players should voting for deathmatch be initiated. Set to -1 to disable\n"
        		+"deathmatch_check_minutes: At how many minutes should be checked if /dm should be available from deathmatch_players\n"
        		+"game_deathmatch_time_minutes: How long the deathmatch takes\n"
        		+"supply_drop_minutes: At how many minutes remaining a supply drop will spawn. Set to -1 to disable\n"
        		+"min_players: Minimum amout of players to start the game\n"
        		+"min_players_cap: Change in player count (in minus!!) allowed to avoid possible constant cancellation of the game when players come and go\n"
        		+"max_players: Max players until the game starts\n"
        		+"game_finish_cooldown_minutes: Time in minutes after the game is reset to wait before the next game can start. Set to 0 for no cooldown\n"
        		+"timed_messages: Interval in minutes that random tips are displayed. Set 0 to disable\n"
        		+"tip_messages: Messages that are displayed at timed_messages interval\n"
        		+"selectkit: [NOTE: DEPRECATED!] Message that is displayed when a player joins (use for kit description)\n"
        		+"notify_ops_ingame_on_playerjoin: Wheter to send a message to admins in-game if a player joins\n"
        		+"showram: Show RAM usage in the console (for debugging)\n"
        		+"custom_game_world: Whether or not to use a custom seed for the game world\n"
        		+"custom_game_world_seed: The seed to use for the custom world when custom_game_world is set to true (needs to be an integer whithin scope)\n"
        		+"custom_map: Set to true if you want to use your own map (name must be 'custom')\n"
        		+"remind_voting: Send player a message when he logs in to remind him to vote for the server\n"
        		+"remind_voting_message: Message that is displayed when the player logs in when remind_voting is enabled\n"
        		+"voting_sites: List of voting sites to display"
        		+"");
        saveConfig();
        
        userDataFile = new File(getDataFolder(), "userData.yml");
        userDataConfig = YamlConfiguration.loadConfiguration(userDataFile);
        
        userDataConfig.options().header("This is where user data is stored, such as ranking");
        
        try {
			userDataConfig.save(userDataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        generateGameWorld();
        
        World world = Bukkit.getServer().getWorld("world");
        world.setSpawnLocation(-145, 9, 405);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        
        Kit.addMainKits();
        Kit.setKitData();

        HGScoreboard.createOnlineScoreboard();
        HGScoreboard.createGameScoreboard();
        
        topPlayers();
        
        if (config.getBoolean("showtips", true))
        	timedMessages(config.getInt("timed_messages"));
        
        getLogger().info("HungerGames has been enabled.");
 
    }
	
	@Override
	public void onDisable() {
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.kickPlayer("Reloading Server");
		}
		
		resetGameWorld(true);
		
		getLogger().info("HungerGames is disabled!");
	}
	
	public void copy() {
        String root = new File(".").getAbsolutePath();
        File srcDir = new File(root+File.separator+"custom");
        if (!srcDir.exists()) {
            Bukkit.getLogger().warning("World 'custom' does not exist!");
            return;
        }
        
        File destDir = new File(root+File.separator+"world_game");
        FileUtil.copy(srcDir, destDir); //TODO
        Bukkit.getServer().createWorld(new WorldCreator("world_game"));
    }
	
	public void generateGameWorld() {
		//Ocean Spawn Point
        //16624168
		
		if (config.getBoolean("custom_map")) {
			copy();
			return;
		}
		
		if (config.getBoolean("custom_game_world")) {
			
			int seed = config.getInt("custom_game_world_seed");
			Bukkit.getServer().createWorld(new WorldCreator("world_game").seed((long) seed));
			return;
		}
		
		Bukkit.getServer().createWorld(new WorldCreator("world_game").seed((long) Math.floor(Math.random() * 100000000)));

	}
	
	public void resetGameWorld(boolean onDisable) {
		
		World worldtodelete = Bukkit.getWorld("world_game");
		File folder;
		
		if (worldtodelete != null) {
			folder = worldtodelete.getWorldFolder();
			Bukkit.getServer().unloadWorld((worldtodelete), true); //unload world
			deleteDirectory(folder); //delete world
			
		} else {
			folder = null;
		}
		
		if (onDisable)
			return;

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
		    @Override
			public void run() {
				if (!folder.exists() || folder == null) {
					generateGameWorld();
				} else {
					Bukkit.getLogger().info("Cannot generate new world, since the old one hasn't been deleted yet... Retrying in 10 Seconds...");
					resetGameWorld(false);
				}	
		    }
		}, 10*20L);
	}
	
	public void startDeathMatch() {
		HGListener.deathMatchInitiated = true;
		
		Bukkit.getScheduler().cancelTask(tasks.get("gametimer"));
		
		if (tasks.get("oneMinuteCountdown") != null) {
			Bukkit.getScheduler().cancelTask(tasks.get("oneMinuteCountdown"));
			tasks.remove("oneMinuteCountdown");
		}
		
		HGListener.timeRemaining = 1;
		Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Deathmatch voting is complete! Teleporting all players to spawn in 1 minute");
		HGScoreboard.updateGameScoreboard();
		
		oneMinuteCountdown(60);
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    @Override
			public void run() {
				deathMatchTimer();
		    }
		}, 60*20L);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		String lowerCmd = cmd.getName().toLowerCase();
		int length = args.length;
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Contributor c = Contributor.getContributor(player);
			
			boolean hasPermissionGameCommands = player.hasPermission("hungergames.gamecommands");
			String noPermission = ChatColor.RED + "You don't have permission to use this command!";
			
			switch (lowerCmd) {
				case "startgame":
					if (hasPermissionGameCommands) {
						if (players.size() < 2) {
							player.sendMessage(ChatColor.GRAY + "Need more than one player!");
						} 
						//debugging
						else if (HGListener.gameStarting) {
							HGListener.gameStartingSeconds = 10;
						} 
						//enter startgame twiche to start immediately
						else if (HGListener.gameStarting || HGListener.gameRunning || HGListener.gameFinished){
							player.sendMessage(ChatColor.GRAY + "Game is already running!");
						} else {
							Bukkit.broadcastMessage(ChatColor.GRAY + "The game has been manually started");
							HGListener.manuallyStarted = true;
							HGListener.cooldown = false;
							startGame();
							HGScoreboard.updateOnlineScoreboard();
						}
					} else {
						player.sendMessage(noPermission);
					}
					
					return true;
				case "stopgame":
					if (hasPermissionGameCommands) {
						if (HGListener.gameRunning) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "The game has been manually finished");
							finishGame();
						} else if (HGListener.gameStarting) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "The game has been cancelled");
							HGListener.gameStarting = false;
							HGListener.manuallyStarted = false;
							HGScoreboard.updateOnlineScoreboard();
							Bukkit.getScheduler().cancelTask(tasks.get("startgame"));
							
							if (tasks.get("oneMinuteCountdown") != null) {
								Bukkit.getScheduler().cancelTask(tasks.get("oneMinuteCountdown"));
								tasks.remove("oneMinuteCountdown");
								HGListener.oneMinuteSecondsRemaining = 0;
							}
						} else {
							player.sendMessage(ChatColor.GRAY + "Cannot stop the game, game must be running");
						}
					} else {
						player.sendMessage(noPermission);
					}
					return true;
				case "deathmatch":
					if (hasPermissionGameCommands) {
						if (HGListener.gameRunning && !HGListener.deathmatch) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "A deathmatch has been manually initiated");
							
							startDeathMatch();
						} else {
							player.sendMessage(ChatColor.GRAY + "Cannot initiate deathmatch right now");

						}
					} else {
						player.sendMessage(noPermission);
					}
					return true;
				case "dm":
					if (HGListener.deathMatchAble && !HGListener.deathMatchInitiated && Contributor.exists(player)) {
						if (HGListener.dmVoters.contains(player.getName())) {
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You already voted for deathmatch");
						} else {
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You voted for deathmatch");
							HGListener.dmVoters.add(player.getName());
							
							HGListener.checkVotes();
						}
					} else {
						player.sendMessage(ChatColor.GRAY + "Cannot use this command right now");
					}
					return true;
				case "kit":
					if (HGListener.gameRunning || HGListener.gameFinished) {
						player.sendMessage(ChatColor.RED + "The game has already started!");
						return true;
					}
					switch (length) {
						case 0:
							HGGUI GUI = new HGGUI(player);
							GUI.addInventories();
							GUI.openKitGUI();
							GUI = null;
							return true;
						case 1:
							String arg0 = args[0].toLowerCase();
							arg0 = arg0.substring(0, 1).toUpperCase() + arg0.substring(1);
							
							c.setKit(arg0);
							return true;
						default:
							player.sendMessage(ChatColor.RED + "Please select a valid kit!");
							return true;
					}
				case "kits":
					ArrayList<String> kits = Kit.getKitNames();
					
					player.sendMessage(kits.toString());
					return true;
				case "in":
					if (hasPermissionGameCommands) {
						if (!HGListener.gameRunning) {
							player.sendMessage(ChatColor.GRAY + "Game must be running!");
							return true;
						}
						
						switch(length) {
							case 0:
								player.sendMessage(ChatColor.GRAY + "Please specify a player");
								return true;
							case 1:
							case 2:
								String kit = "Rogue";
								if (args.length >= 2)
									kit = args[1];
								
								Player toAdd = Bukkit.getPlayer(args[0]);
								if (toAdd == null) {
									player.sendMessage(ChatColor.GRAY + "Player not found");
									return true;
								}
								
								if (Contributor.exists(toAdd)) {
									player.sendMessage(ChatColor.GRAY + "Player is already in game");
									return true;
								}
								
								HGListener.getInstance().registerPlayer(toAdd, kit, true);
								return true;
							default:
								player.sendMessage(ChatColor.GRAY + "Please type a valid name");
								return true;
 						}
						
					} else {
						player.sendMessage(noPermission);
						return true;
					}
					
				case "out":
					if (!HGListener.gameRunning || HGListener.gameFinished) {
						player.sendMessage(ChatColor.GRAY + "Can't opt out of the game: it must be running!");
						return true;
					} else if (!Contributor.exists(player)) {
						player.sendMessage(ChatColor.GRAY + "Can't opt out of the game: you are not contributing!");
						return true;
					}
					
					Contributor current = Contributor.getContributor(player);
					
					if (current.isOpting)
						return true;
					
					current.isOpting = true;
					
					Timer t = new Timer(this, 10*4, "out", 5L, player, player.getLocation());
					t.startTimer();
					
					return true;
				case "showkit":
					player.sendMessage(Contributor.getContributor(player).kit);
					return true;
				case "discord":
					player.sendMessage(ChatColor.DARK_PURPLE + "Click to join our Discord server => " + ChatColor.RESET + "natzki.me/discord");
					return true;
				case "skypehg":
					player.sendMessage(ChatColor.AQUA + "Click to join our Skype group => " + ChatColor.RESET + "natzki.me/skypeHG");
					return true;
				case "coins":
					
				switch (length) {
						case 0:
							player.sendMessage(ChatColor.DARK_GREEN + "Your balance is " + ChatColor.YELLOW + c.getCoins() + ChatColor.DARK_GREEN + " coins.");
							return true;
						default:
							if (args[0].equals("give")) {
								
								if (length == 1) {
									player.sendMessage(ChatColor.DARK_GREEN + "Please specify a player!");
									return true;
								}
								
								if (args[1].equals(player.getName())) {
									player.sendMessage(ChatColor.DARK_GREEN + "Not to yourself, dummy!");
									return true;
								}

								ConfigurationSection section = Main.returnUserDataConfig().getConfigurationSection(args[1]);
								int amount;
								
								if (section != null) {
									
									if (length == 2) {
										player.sendMessage(ChatColor.DARK_GREEN + "Please specify an amount!");
										return true;
									}
									
									try {
										amount = Integer.parseInt(args[2]);
									} catch (NumberFormatException e) {
										player.sendMessage(ChatColor.DARK_GREEN + "Please specify a valid amount!");
										return true;
									}
									
									if (amount > c.getCoins()) {
										player.sendMessage(ChatColor.DARK_GREEN + "Not enough balance!");
										return true;
									} else if (amount < 1) {
										player.sendMessage(ChatColor.DARK_GREEN + "Please specify a valid amount!");
										return true;
									} else {
										String suffix = amount == 1 ? " coin" : " coins";
										
										section.set("coins", section.getInt("coins") + amount);
										c.setCoins(-amount);
										
										player.sendMessage(ChatColor.DARK_GREEN + "You have given " + args[1] + " " + ChatColor.YELLOW + amount + ChatColor.DARK_GREEN + suffix);
										
										if (Bukkit.getPlayer(args[1]) != null)
											Bukkit.getPlayer(args[1]).sendMessage(ChatColor.DARK_GREEN + "You have recieved " + ChatColor.YELLOW + amount + ChatColor.DARK_GREEN + suffix + " from " + player.getName() + "!");
										
										return true;
									}
								} else {
									player.sendMessage(ChatColor.DARK_GREEN + "Player not found!");
									return true;
								}
							} else {
								String suffix = c.getCoins() == 1 ? " coin" : " coins";
								player.sendMessage(ChatColor.DARK_GREEN + "Your balance is " + ChatColor.YELLOW + c.getCoins() + ChatColor.DARK_GREEN + suffix);
								return true;
							}
					}
				case "showplayers":
					String list = String.join(", ", Contributor.getContributorNames()); 
					player.sendMessage(list);
					//player.sendMessage(Joiner.on(", ").join(Contributor.getContributorNames())); //TODO
					return true;
				case "supply":
					if (hasPermissionGameCommands) {
						if (!HGListener.gameRunning) {
							player.sendMessage(ChatColor.GRAY + "Game must be running");
						} else {
							supplyStation();
						}
					} else {
						player.sendMessage(noPermission);
					}
					return true;
				case "showram":
					if (hasPermissionGameCommands) {
						if (isLogging) {
							player.sendMessage("Already logging RAM!");
							return true;
						}
						
						player.sendMessage("Ram is being monitored to the console");
						isLogging = true;
						getRam();
						return true;
					} else {
						player.sendMessage(noPermission);
						return true;
					}
				case "stopram":
					if (hasPermissionGameCommands) {
						if (!isLogging) {
							player.sendMessage("RAM is not being logged!");
							return true;
						}
						
						player.sendMessage("Ram logging has been stopped");
						Bukkit.getScheduler().cancelTask(tasks.get("showramusage"));
						isLogging = false;
						return true;
					} else {
						player.sendMessage(noPermission);
						return true;
					}
				default:
					player.sendMessage(ChatColor.RED + "Cannot execute this command!");
					return true;
			}
			
		} else if (sender instanceof ConsoleCommandSender) {
			
			sender = (ConsoleCommandSender) sender;
			
			switch (lowerCmd) {
				case "reward":
					if (length == 2) {
						String playerName = args[0];
						@SuppressWarnings("unused")
						String service = args[1];
						
						Player rewarded = Bukkit.getPlayer(playerName);
						
						if (rewarded != null) {
							rewarded.sendMessage(ChatColor.GREEN + "Thank you for voting. You have received" + ChatColor.YELLOW + " 5 " + ChatColor.GREEN + "coins.");
						}
						
						if (userDataConfig.getConfigurationSection(playerName) != null) {
							ConfigurationSection section = userDataConfig.getConfigurationSection(playerName);
							section.set("coins", section.getInt("coins") + 5);
							
							saveUserData(userDataConfig);
						}
						
						return true;
					} else {
						Bukkit.getLogger().warning("Arguments length does not match for command 'reward'!");
						return true;
					}
				case "startgame":
					if (players.size() < 2) {
						sender.sendMessage(ChatColor.GRAY + "Need more than one player!");
					} else if (HGListener.gameStarting || HGListener.gameRunning || HGListener.gameFinished){
						sender.sendMessage(ChatColor.GRAY + "Game is already running!");
					} else {
						Bukkit.broadcastMessage(ChatColor.GRAY + "The game has been manually started");
						HGListener.manuallyStarted = true;
						HGListener.cooldown = false;
						startGame();
						HGScoreboard.updateOnlineScoreboard();
					}
					return true;
				case "stopgame":
					if (HGListener.gameRunning) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "The game has been manually finished");
						finishGame();
					} else if (HGListener.gameStarting) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "The game has been cancelled");
						HGListener.gameStarting = false;
						HGListener.manuallyStarted = false;
						HGScoreboard.updateOnlineScoreboard();
						Bukkit.getScheduler().cancelTask(tasks.get("startgame"));
						
						if (tasks.get("oneMinuteCountdown") != null) {
							Bukkit.getScheduler().cancelTask(tasks.get("oneMinuteCountdown"));
							tasks.remove("oneMinuteCountdown");
							HGListener.oneMinuteSecondsRemaining = 0;
						}
					} else {
						sender.sendMessage(ChatColor.GRAY + "Cannot stop the game, game must be running");
					}
					return true;
					
					//FOR DEBUGGING WITH THE CONSOLE
					
					/*
					 * public class Main
{
	public static void main(String[] args) {
	    
	    int length = args.length;
	    
	    switch (length) {
						case 2:			
							String method = args[0];
							
							double a;
							double total;
							double totalex;
							double minefee = 4;
							
							try {
								a = Integer.parseInt(args[1]);
							} catch (NumberFormatException e) {
								System.out.println("Please specify a valid amount!");
								return;
							}
							
							switch (method) {
								case "cb":
									double spread = a * 0.005 * 2;
									double cbfee = a * 0.0149;
									total = a + spread + cbfee;
									break;
								case "sbb":
									double sbbfee = a * 0.091;
									total = a + sbbfee;
									break;
								case "k-frik":
									double kfee = a * 0.0055;
									total = a + kfee;
									break;
								//case "k-DE":
								default:
									System.out.println("Please use syntax correctly. /calc <method> <amount>");
									return;
							}
							
							double bfee = total * 0.015;
							totalex = total + bfee + minefee;
							
							System.out.println("" + total);
							if (!method.equals("sbb")) {
								System.out.println("" + totalex);
							}
							return;
						default:
							System.out.println("Please use syntax correctly. /calc <method> <amount>");
							return;
	    }
	    
	}
}
					 * 
					 * */
					
				case "calc":
					switch (length) {
						case 2:			
							String method = args[0];
							
							double a;
							double total;
							double totalex;
							double minefee = 4;
							
							try {
								a = Integer.parseInt(args[1]);
							} catch (NumberFormatException e) {
								sender.sendMessage("Please specify a valid amount!");
								return true;
							}
							
							switch (method) {
								case "cb":
									double spread = a * 0.005 * 2;
									double cbfee = a * 0.0149;
									total = a + spread + cbfee;
									break;
								case "sbb":
									double sbbfee = a * 0.091;
									total = a + sbbfee;
									break;
								case "k-frik":
									double kfee = a * 0.0055;
									total = a + kfee;
									break;
								//case "k-DE":
								default:
									sender.sendMessage("Please use syntax correctly. /calc <method> <amount>");
									return true;
							}
							
							double bfee = total * 0.015;
							totalex = total + bfee + minefee;
							
							sender.sendMessage("" + total);
							if (!method.equals("sbb")) {
								sender.sendMessage("" + totalex);
							}
							return true;
						default:
							sender.sendMessage("Please use syntax correctly. /calc <method> <amount>");
							return true;
					}
				default:
					sender.sendMessage("Unknown command for console");
					return true;
			}
		} else {
			sender.sendMessage("Can't execute command or is unknown! Or have you sent from a remote console?");
			Bukkit.getLogger().info("Can't execute command or is unknown! Or have you sent from a remote console?");
			return true;
		}
	}
	
	public static HashMap<String, Integer> tasks = new HashMap<String, Integer>();
	
	public void startGame() {
		if (tasks.get("timed_messages") != null)
			Bukkit.getScheduler().cancelTask(tasks.get("timed_messages"));
		if (tasks.get("gamecooldown") != null)
			Bukkit.getScheduler().cancelTask(tasks.get("gamecooldown"));
		
		HGListener.gameStarting = true;
		HGListener.cooldown = false;
		
		int time = config.getInt("game_start_time_seconds");
		HGListener.gameStartingSeconds = time;
		Timer t = new Timer(mainInstance, time, "startgame", 20L);
		t.startTimer();
	}
	
	public void cooldownPvp() {
		HGListener.PVPcooldownOn = true;
		
		int time = config.getInt("pvp_cooldown_seconds");
		Timer t = new Timer(mainInstance, time, "pvpcooldown", 20L);
		t.startTimer();
	}
	
	public void gameTimer() {
		HGListener.gameRunning = true;
		HGListener.timeElapsed = -1;
		HGListener.oneMinuteSecondsRemaining = 59;
        
		int time = config.getInt("game_timer_minutes");
		Timer t = new Timer(mainInstance, time, "gametimer", 1200L);
		t.startTimer();	
	}
	
	public void oneMinuteCountdown(int time) {
		HGListener.oneMinuteSecondsRemaining = time;
		
		Timer t = new Timer(mainInstance, time, "oneMinuteCountdown", 20L);
		t.startTimer();
	}
	
	public static void supplyStation() {
		int x = (int) Math.floor(Math.random() * 500);
		int z = (int) Math.floor(Math.random() * 500);
		
		//World world = Bukkit.getWorld("world"); //testing
		World world = Bukkit.getWorld("world_game");
		
		Location ench = HGListener.spawn.clone();
		ench.setX((ench.getX()-250) + x);
		ench.setZ((ench.getZ()-250) + z);
		ench = HGListener.checkBiome(ench, 50, 1);
		ench.setY(world.getHighestBlockYAt(ench));
		
		world.getBlockAt(ench).setType(Material.ENCHANTING_TABLE);
		ench.setY(ench.getY()-1);
		world.getBlockAt(ench).setType(Material.BEDROCK);
		ench.setY(ench.getY()+1);
		
		Location chest = ench.clone();
		chest.setX(chest.getX() - 3); 
		chest.setY(world.getHighestBlockYAt(chest));
		
		world.getBlockAt(chest).setType(Material.CHEST);
		chest.setY(chest.getY()-1);
		world.getBlockAt(chest).setType(Material.BEDROCK);
		chest.setY(chest.getY()+1);
		
		Location pillar = chest.clone();
		pillar.setX(pillar.getX()+1);
		pillar.setY(pillar.getY()+5);
		
		/*
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setX(pillar.getX()+1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setZ(pillar.getZ()+1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setX(pillar.getX()-1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setX(pillar.getX()-1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setZ(pillar.getZ()-1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setZ(pillar.getZ()-1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setX(pillar.getX()+1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setX(pillar.getX()+1);
		world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
		pillar.setX(pillar.getX()-1);
		pillar.setZ(pillar.getZ()+1);
		pillar.setY(pillar.getY()+1);
		
		*/
		
		for (int i = 0; i < 32; i++) {
			if (pillar.getY() == 256 || i == 31) {
				world.getBlockAt(pillar).setType(Material.BLACK_GLAZED_TERRACOTTA);
				break;
			}
			world.getBlockAt(pillar).setType(Material.MAGENTA_GLAZED_TERRACOTTA);
			pillar.setY(pillar.getY()+1);
		}
		
		//Lighting issues
		/*
		chest.setX(chest.getX()+1);
		world.getBlockAt(chest).setType(Material.TORCH);
		chest.setX(chest.getX()-2);
		world.getBlockAt(chest).setType(Material.TORCH);
		chest.setX(chest.getX()+1);
		chest.setZ(chest.getZ()+1);
		world.getBlockAt(chest).setType(Material.TORCH);
		chest.setZ(chest.getZ()-2);
		world.getBlockAt(chest).setType(Material.TORCH);
		chest.setZ(chest.getZ()+1);
		
		
		net.minecraft.server.v1_13_R2.Chunk c = ((CraftChunk) world.getBlockAt(chest).getChunk()).getHandle();
		c.initLighting();
		
		*/
		
		Location spawn1 = chest.clone();
		spawn1.setX(spawn1.getX() + 3);
		spawn1.setZ(spawn1.getZ() + 3);
		spawn1.setY(world.getHighestBlockYAt(spawn1));
		
		Location spawn2 = chest.clone();
		spawn2.setX(spawn2.getX() + 3);
		spawn2.setZ(spawn2.getZ() - 3);
		spawn2.setY(world.getHighestBlockYAt(spawn2));
		
		Chest chestBlock = (Chest) chest.getBlock().getState();
		Inventory inventory = chestBlock.getBlockInventory();
        Material[] randomItens = {
        		Material.AIR, 
        		Material.TNT,
        		Material.EMERALD,
        		Material.ENCHANTED_BOOK,
        		Material.POTION,
        		Material.FEATHER,
        		Material.FISHING_ROD,
        		Material.BREAD, 
        		Material.DIAMOND_PICKAXE, 
        		Material.ARROW, 
        		Material.BOW,
        		Material.EXPERIENCE_BOTTLE
        		};
        
        ArrayList<Enchantment> enchantments = new ArrayList<Enchantment>();
        enchantments.add(Enchantment.ARROW_KNOCKBACK);
        enchantments.add(Enchantment.DURABILITY);
        enchantments.add(Enchantment.LOOT_BONUS_MOBS);
        enchantments.add(Enchantment.PROTECTION_PROJECTILE);
        enchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
        enchantments.add(Enchantment.DAMAGE_UNDEAD);
        enchantments.add(Enchantment.DAMAGE_ARTHROPODS);
        
        ArrayList<Enchantment> fishingEnchantments = new ArrayList<Enchantment>();
        fishingEnchantments.add(Enchantment.LUCK);
        fishingEnchantments.add(Enchantment.LURE);
        
        ArrayList<PotionType> potionEffects = new ArrayList<PotionType>();
        potionEffects.add(PotionType.SPEED);
        potionEffects.add(PotionType.INSTANT_HEAL);
        potionEffects.add(PotionType.NIGHT_VISION);

        int max = 7; //item count
        
        Material lastItem = Material.KNOWLEDGE_BOOK;
        
        for (int i = 0; i < max; i++) {
        	Random rand = new Random();
            int intRandom1 = rand.nextInt(27);
            
            Material currentItem = randomItens[rand.nextInt(randomItens.length)];
            
            if (lastItem == currentItem) {
            	i--;
            	continue;
            }
            
            lastItem = currentItem;
            
            ItemStack is = new ItemStack(currentItem, 1);
            
            if (currentItem == Material.POTION) {
            	int r = rand.nextInt(potionEffects.size());
            	PotionType current = potionEffects.get(r);
             	
            	is = Kit.getPotionItemStack(false, current, currentItem, 100, 1, null);
            }
            
            if (currentItem == Material.ENCHANTED_BOOK) {
            	int r = rand.nextInt(enchantments.size());
            	Enchantment current = enchantments.get(r);
            	
            	HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
            	enchants.put(current, 1);
            	
            	is = Kit.enchantItem(currentItem, enchants);
            }
            
            if (currentItem == Material.FISHING_ROD) {
            	int r = rand.nextInt(fishingEnchantments.size());
            	Enchantment current = fishingEnchantments.get(r);
            	
            	HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
            	enchants.put(current, 2);
            	
            	is = Kit.enchantItem(currentItem, enchants);
            }
            
            if (currentItem.getMaxStackSize() > 2) {
            	is.setAmount(rand.nextInt(5));
            }
           
            inventory.setItem(intRandom1, is);
            
            if (i == max-1) {
            	int j = 0;
            	for (ItemStack js : inventory) {
            		if (js == null) {
            			continue;
            		}
            		j++;
            	}
            	if (j < 5) {
            		i--;
            	}
            }
        }

		world.spawnEntity(spawn1, EntityType.WITHER_SKELETON);
		world.spawnEntity(spawn2, EntityType.WITHER_SKELETON);
		
		Bukkit.broadcastMessage(ChatColor.RED + "A supply station has spawned at " + ChatColor.RESET + (int)chest.getX()+", "+(int)chest.getY()+", "+(int)chest.getZ());
		HGListener.strikeLightning();
	}
	
	public void deathMatchTimer() {
		
		if (tasks.get("pvpcooldown") != null) 
			Bukkit.getScheduler().cancelTask(tasks.get("pvpcooldown"));
		
		if (tasks.get("gametimer") != null)
			Bukkit.getScheduler().cancelTask(tasks.get("gametimer"));
		
		if (tasks.get("oneMinuteCountdown") != null) {
			Bukkit.getScheduler().cancelTask(tasks.get("oneMinuteCountdown"));
			tasks.remove("oneMinuteCountdown");
		}
			
		
		HGListener.deathmatch = true;
		HGListener.deathMatchAble = false;
		HGListener.deathMatchCooldown = true;
		
		HGListener.oneMinuteSecondsRemaining = 59;
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(HGListener.randomLocation(HGListener.spawn.getX(), 20, 20, false));
		}
		
		int time = config.getInt("game_deathmatch_time_minutes");
		Timer t = new Timer(mainInstance, time, "deathmatch", 1200L);
		t.startTimer();
		
		HGScoreboard.updateGameScoreboard();
		
		@SuppressWarnings("unused")
		int updateID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
		    @Override
			public void run() {
		    	HGListener.deathMatchCooldown = false;
		    }
		}, 100L);
	}
	
	public void finishGame() {
		
		//Remove all necessary timers that may currently be running
		if (tasks.get("pvpcooldown") != null) 
			Bukkit.getScheduler().cancelTask(tasks.get("pvpcooldown"));
		
		if (tasks.get("gametimer") != null)
			Bukkit.getScheduler().cancelTask(tasks.get("gametimer"));
		
		if (tasks.get("deathmatch") != null)
			Bukkit.getScheduler().cancelTask(tasks.get("deathmatch"));
		
		if (tasks.get("out") != null)
			Bukkit.getScheduler().cancelTask(tasks.get("out"));
		
		if (tasks.get("oneMinuteCountdown") != null) {
			Bukkit.getScheduler().cancelTask(tasks.get("oneMinuteCountdown"));
			tasks.remove("oneMinuteCountdown");
		}
		
		
		HGListener.PVPcooldownOn = false;
		HGListener.gameRunning = false;
		HGListener.gameFinished = true;
		HGListener.cooldown = true;
		HGListener.checkPlayersInitialCheck = false;
		
		HGScoreboard.updateGameScoreboard();
		
		int time = config.getInt("game_finish_time_seconds");
		Timer t = new Timer(this, time*4, "fireworks", 5L);
		t.startTimer();
				
		Timer t1 = new Timer(this, time, "finishgame", 20L);
		t1.startTimer();
	}
	
	public void stopGame() {
		
		topPlayers();
		
		for (int task : tasks.values()) {
			Bukkit.getScheduler().cancelTask(task);
		}
		tasks.clear();
		
		timedMessages(config.getInt("timed_messages"));

		//I take no chances!
		HGListener.gameStarting = false;
		HGListener.gameRunning = false;
		HGListener.deathmatch = false;
		HGListener.gameFinished = false;
		HGListener.manuallyStarted = false;
		HGListener.deathMatchAble = false;
		HGListener.dmVoters.clear();
		HGListener.deathMatchInitiated = false;
		HGListener.winnerName = "?";
		HGListener.secondName = "?";
		HGListener.thirdName = "?";
		
		isLogging = false; //The task gets cancelled anyway
		
		Main.players.clear();
		
		World world = Bukkit.getWorld("world");
		world.setDifficulty(Difficulty.PEACEFUL);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		
		for (Player p : Bukkit.getOnlinePlayers()) {		
			HGListener.getInstance().registerPlayer(p, "", false);
		}
		
		int time = config.getInt("game_finish_cooldown_minutes");
		if (time > 0) {
			String suffix = time > 1 ? " minutes" : " minute";
			
			Timer t = new Timer(this, time, "gamecooldown", 1200L);
			t.startTimer();
			Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Game will be able to start in " + time + suffix);
		}
		
		resetGameWorld(false);
	}
	
	public static boolean isLogging;
	
	public void getRam() {
		Timer t = new Timer(mainInstance, -1, "showramusage", 20L);
		t.startTimer();
	}
	
	public static int lastint = 999999; //to check if the previous timed message was already displayed
	
	public static void timedMessages(int interval) {
		if (interval <= 0) {
			return;
		} else {
			HGListener.firstTipMessage = true;
			long i = (long) interval * 1200;
			Timer t = new Timer(mainInstance, -1, "timed_messages", i);
			t.startTimer();
		}
	}
	
	public static boolean deleteDirectory(File path) {
        if(path.exists()) {
            File files[] = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                	deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                    /* TODO
                    try {
                    	FileDeleteStrategy.FORCE.delete(files[i]);
					} catch (IOException e) {
						e.printStackTrace();
					}
					*/
                }
            }
        }
        return(path.delete());
	}
	
	@SuppressWarnings("deprecation")
	public static void topPlayers() {
		
		LinkedHashMap<String, Integer> players = new LinkedHashMap<String, Integer>();

		for (String name : userDataConfig.getKeys(false)) {
			ConfigurationSection currentSection = userDataConfig.getConfigurationSection(name);
			players.put(name, currentSection.getInt("wins"));
		}
		
		players = players
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
		
		World world = Bukkit.getWorld("world");
		
		String[] playersNames = players.keySet().toArray(new String[players.size()]);
	    int length = playersNames.length;
		
		String name1 = length > 0 ? players.keySet().toArray()[0].toString() : "None";
		String name2 = length > 1 ? players.keySet().toArray()[1].toString() : "None";
		String name3 = length > 2 ? players.keySet().toArray()[2].toString() : "None";
		
		int kills1 = name1 != "None" ? userDataConfig.getConfigurationSection(name1).getInt("kills") : 0;
		int kills2 = name2 != "None" ? userDataConfig.getConfigurationSection(name2).getInt("kills") : 0;
		int kills3 = name3 != "None" ? userDataConfig.getConfigurationSection(name3).getInt("kills") : 0;
		
		kills1 = String.valueOf(kills1).equals("null") ? 0 : kills1;
		kills2 = String.valueOf(kills2).equals("null") ? 0 : kills2;
		kills3 = String.valueOf(kills3).equals("null") ? 0 : kills3;
		
		BlockState player1 = world.getBlockAt(new Location(world, -141, 11, 410)).getState();
		BlockState player2 = world.getBlockAt(new Location(world, -142, 11, 410)).getState();
		BlockState player3 = world.getBlockAt(new Location(world, -143, 11, 410)).getState();
		
		Block sign1 = world.getBlockAt(new Location(world, -141, 10, 410));
		Block sign2 = world.getBlockAt(new Location(world, -142, 10, 410));
		Block sign3 = world.getBlockAt(new Location(world, -143, 10, 410));
		
		boolean runAgain = false;
		 
		//Player heads
		if (player1 instanceof Skull) {
		    Skull skull = (Skull) player1;
		    skull.setOwningPlayer(Bukkit.getOfflinePlayer(name1));
		    skull.update();
		} else {
			world.getBlockAt(new Location(world, -141, 11, 410)).setType(Material.PLAYER_WALL_HEAD);
			runAgain = true;
		}
		if (player2 instanceof Skull) {
		    Skull skull = (Skull) player2;
		    skull.setOwningPlayer(Bukkit.getOfflinePlayer(name2));
		    skull.update();
		} else {
			world.getBlockAt(new Location(world, -142, 11, 410)).setType(Material.PLAYER_WALL_HEAD);
			runAgain = true;
		}
		if (player3 instanceof Skull) {
		    Skull skull = (Skull) player3;
		    skull.setOwningPlayer(Bukkit.getOfflinePlayer(name3));
		    skull.update();
		} else {
			world.getBlockAt(new Location(world, -143, 11, 410)).setType(Material.PLAYER_WALL_HEAD);
			runAgain = true;
		}
		
		String bold = ChatColor.BOLD + "";
		
		//Signs
		if(sign1.getType().equals(Material.OAK_WALL_SIGN)) {
	            // We cast the block to a sign
	            Sign sign = (Sign) sign1.getState();
	            
	            sign.setLine(0, bold + name1);
	            sign.setLine(1, String.valueOf(players.get(name1) + " wins"));
	            sign.setLine(2, String.valueOf(kills1) + " kills");
	            sign.update();
	     } else {
	    	 sign1.setType(Material.OAK_WALL_SIGN);
	    	 runAgain = true;
	     }
		
		if(sign2.getType().equals(Material.OAK_WALL_SIGN)) {
            // We cast the block to a sign
            Sign sign = (Sign) sign2.getState();
            
            sign.setLine(0, bold + name2);
            sign.setLine(1, String.valueOf(players.get(name2) + " wins"));
            sign.setLine(2, String.valueOf(kills2) + " kills");
            sign.update();
		} else {
			sign2.setType(Material.OAK_WALL_SIGN);
			runAgain = true;
		}
		
		if(sign3.getType().equals(Material.OAK_WALL_SIGN)) {
            // We cast the block to a sign
            Sign sign = (Sign) sign3.getState();
            
            sign.setLine(0, bold + name3);
            sign.setLine(1, String.valueOf(players.get(name3) + " wins"));
            sign.setLine(2, String.valueOf(kills3) + " kills");
            sign.update();
		} else {
			sign3.setType(Material.OAK_WALL_SIGN);
			runAgain = true;
		}
		
		if (runAgain)
			topPlayers();
		
	}
	
	public void setDefaultConfigValues() {
		
		HashMap<String, Integer> defaultNumbers = new HashMap<String, Integer>();
		defaultNumbers.put("game_start_time_seconds", 60);
		defaultNumbers.put("game_finish_time_seconds", 20);
		defaultNumbers.put("pvp_cooldown_seconds", 120);
		defaultNumbers.put("game_timer_minutes", 60);
		defaultNumbers.put("supply_drop_minutes", 30);
		defaultNumbers.put("deathmatch_players", 4);
		defaultNumbers.put("deathmatch_check_minutes", 10);
		defaultNumbers.put("game_deathmatch_time_minutes", 15);
		defaultNumbers.put("min_players", 4);
		defaultNumbers.put("min_players_cap", 5);
		defaultNumbers.put("max_players", 100);
		defaultNumbers.put("timed_messages", 3);
		defaultNumbers.put("game_finish_cooldown_minutes", 5);
		defaultNumbers.put("custom_game_world_seed", 123456789);
		
		HashMap<String, Boolean> defaultBooleans = new HashMap<String, Boolean>();
		defaultBooleans.put("showtips", true);
		defaultBooleans.put("custom_game_world", false);
		defaultBooleans.put("custom_map", false);
		defaultBooleans.put("notify_ops_ingame_on_playerjoin", false);
		defaultBooleans.put("remind_voting", false);
		
		HashMap<String, String> configMessagesDefault = new HashMap<String, String>();
		configMessagesDefault.put("selectkit", ChatColor.RED + "Welcome! Please select your kit with /kit <name>. To view available kits simply type /kit.");
		configMessagesDefault.put("gamefull", "The server is full!");
		configMessagesDefault.put("gamestart", ChatColor.GOLD + "" + ChatColor.BOLD + "Let the games begin!");
		configMessagesDefault.put("remind_voting_message", ChatColor.GREEN + "Don't forget to vote!");
		
		String[] defaultTipMessages = {
				"Use your compass to track players."
			};
		
		String[] votingSites = {
				"natzki.me/vote1",
				"natzki.me/vote2"
			};
		
		HashMap<String, String[]> configMessagesArrayDefault = new HashMap<String, String[]>();
		configMessagesArrayDefault.put("tip_messages", defaultTipMessages);
		configMessagesArrayDefault.put("voting_sites", votingSites);
		
		for (String name : defaultNumbers.keySet()) {
			if (!config.isSet(name)) {
				config.set(name, defaultNumbers.get(name));
				saveConfig();
			}
		}
		
		for (String name : configMessagesDefault.keySet()) {
			if (!config.isSet(name)) {
				config.set(name, configMessagesDefault.get(name));
				saveConfig();
			}
		}
		
		for (String name : defaultBooleans.keySet()) {
			if (!config.isSet(name)) {
				config.set(name, defaultBooleans.get(name));
				saveConfig();
			}
		}
		
		for (String name : configMessagesArrayDefault.keySet()) {
			if (!config.isSet(name)) {
				config.set(name, Arrays.asList(configMessagesArrayDefault.get(name)));
				saveConfig();
			}
		}
	}
}