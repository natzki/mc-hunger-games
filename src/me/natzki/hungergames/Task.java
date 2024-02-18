package me.natzki.hungergames;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class Task {
	
	Main mainInstance = Main.getInstance();
	int time;
	int taskID;
	String name;
	
	Player player;
	Location last;
	
	public Task (int time, String name, Player player, Location last) {
		this.time = time;
		this.name = name;
		this.player = player;
		this.last = last;
	}
	
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	
	public void stopTimer() {
        Bukkit.getScheduler().cancelTask(taskID);
        Main.tasks.remove(name);
        
    }
	
	public void executeTask(String name) {
		switch (name) {
			case "startgame":
				//When the error comes back...
				//Bukkit.getLogger().info(String.valueOf(HGListener.cooldown));
				//Bukkit.getLogger().info(String.valueOf(HGListener.manuallyStarted));
				//Bukkit.getLogger().info(String.valueOf(HGListener.gameStarting));
				HGListener.gameStartingSeconds = time;
				
				boolean notEnoughPlayers = Main.players.size() < Main.returnConfig().getInt("min_players") - Main.returnConfig().getInt("min_players_cap"); //second number to avoid constant cancellation of the game when players come and go
				
				if (HGListener.manuallyStarted) {
					if (Main.players.size() <= 1) {
						if (Main.tasks.get("oneMinuteCountdown") != null) {
							Bukkit.getScheduler().cancelTask(Main.tasks.get("oneMinuteCountdown"));
							Main.tasks.remove("oneMinuteCountdown");
							HGListener.oneMinuteSecondsRemaining = 0;
						}
						
						Bukkit.broadcastMessage(ChatColor.GRAY + "Game has been cancelled because there are not enough players");
						HGListener.gameStarting = false;
						HGListener.manuallyStarted = false;
						HGScoreboard.updateOnlineScoreboard();
	        			stopTimer();
	        			return;
					}
				} else if (notEnoughPlayers || Main.players.size() <= 1) {
					if (Main.tasks.get("oneMinuteCountdown") != null) {
						Bukkit.getScheduler().cancelTask(Main.tasks.get("oneMinuteCountdown"));
						Main.tasks.remove("oneMinuteCountdown");
						HGListener.oneMinuteSecondsRemaining = 0;
					}
					
					HGListener.gameStarting = false;
					HGListener.manuallyStarted = false;
					HGScoreboard.updateOnlineScoreboard();
	        		stopTimer();
	        		return;
				}
				
				if (time == 0) {
					HGListener.manuallyStarted = false;
					if (Main.players.size() <= 1) {
						stopTimer();
						return;
					}
	                HGListener.gameStarting = false;
	                
	                HGListener.giveKitAndTeleportEveryone();
	                HGListener.trackPlayer();
	                mainInstance.gameTimer();
	                mainInstance.cooldownPvp();
	                stopTimer();
	                return;
	            }
				if (time <= 60 && Main.tasks.get("oneMinuteCountdown") == null) {
					mainInstance.oneMinuteCountdown(time);
				}
				if (time <= 5) {
	        		String suffix = time == 1 ? " second" : " seconds";
	        		
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "Game will start in " + time + suffix);
	        		for (Player p : Bukkit.getOnlinePlayers()) {
	        			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
	        		}
	        		break;
	        	}
				if (time == 10) {
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "Game will start in " + time + " seconds");
	        		break;
	        	}
				if (time % 30 == 0) {
	                Bukkit.broadcastMessage(ChatColor.GOLD + "Game will start in " + time + " seconds");
	                break;
	            }
				break;
			//pvpcooldown
			case "pvpcooldown":
				if (time != 0 && time % 30 == 0) {
	            	Bukkit.broadcastMessage(ChatColor.RED + "PvP will be enabled in " + time + " seconds.");
	            	break;
	            }
	            if (time == 0) {
	                Bukkit.broadcastMessage(ChatColor.RED + "PvP is now enabled!");
	                HGListener.PVPcooldownOn = false;
	                stopTimer();
	                return;
	            }	
				break;
			//out
			case "out":
				if (time == 10*4) {
					last = player.getLocation();
	        		player.sendMessage(ChatColor.RED + "Please stand still for 10 seconds");
	        		break;
				}
				
				boolean changeX = last.getX() != player.getLocation().getX();
				boolean changeY = last.getY() != player.getLocation().getY();
				boolean changeZ = last.getY() != player.getLocation().getY();
				
				if (changeX || changeY || changeZ) {
	        		player.sendMessage(ChatColor.DARK_RED + "Failed! You moved");
	        		Contributor.getContributor(player).isOpting = false;
	        		stopTimer();
	        		return;
	        	}
				
				if (time == 0) {
					Contributor.getContributor(player).isOpting = false;
					
					player.sendMessage(ChatColor.GRAY + "You were opted out of the game!");
					String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " left the game!";
					
	        		HGListener.getInstance().handlePlayerQuit(player, message, true, true);
					stopTimer();
					return;
	        	}
				break;
		
			//gameTimer
			case "gametimer":
				HGListener.timeRemaining = time;
				HGListener.timeElapsed++;
				HGScoreboard.updateGameScoreboard();
				String suffix = time > 1 ? " minutes" : " minute";
				
				if (time == Main.returnConfig().getInt("game_timer_minutes")) {
					Bukkit.broadcastMessage("-----------------------------------------------------");
					Bukkit.broadcastMessage("");
					Bukkit.broadcastMessage(Main.returnConfig().getString("gamestart"));
					Bukkit.broadcastMessage("");
					Bukkit.broadcastMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.WHITE + time + suffix);
					Bukkit.broadcastMessage("");
					Bukkit.broadcastMessage("-----------------------------------------------------");
				}
				
				if (HGListener.timeElapsed == Main.returnConfig().getInt("deathmatch_check_minutes") && Main.players.size() <= Main.returnConfig().getInt("deathmatch_players")) {
					HGListener.deathMatchAble = true;
					Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "To vote for deathmatch please type /dm");
				}
				
				if (HGListener.checkPlayersInitialCheck && (time < 10 || (time == Main.returnConfig().getInt("game_timer_minutes")-10 && !HGListener.deathMatchAble && Main.players.size() <= Main.returnConfig().getInt("deathmatch_players")))) {
					HGListener.getInstance().checkPlayerCount(Main.players.get(0).getPlayer());
					HGListener.checkPlayersInitialCheck = false;
				}
				if (time >= 0 && time == Main.returnConfig().getInt("supply_drop_minutes")) {
					Main.supplyStation();
	        	}
				if (time == 0) {
	        		mainInstance.deathMatchTimer();
	        		stopTimer();
	                return;
	            }
				if (time == 1) {
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "Get ready! " + ChatColor.WHITE + "Teleporting all players to spawn in 1 minute");
	        		mainInstance.oneMinuteCountdown(60);
	        		break;
	        	}
				if (time == 10) {
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.WHITE + time + suffix);
	        		break;
	        	}
	        	if (time % 30 == 0) {
	                Bukkit.broadcastMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.WHITE + time + suffix);
	                break;
	            }
				break;
			//oneMinuteCountdown	
			case "oneMinuteCountdown":
				HGListener.oneMinuteSecondsRemaining--;

				if (HGListener.gameRunning) {
					HGScoreboard.updateGameScoreboard();
				} else if (HGListener.gameStarting) {
					HGScoreboard.updateOnlineScoreboard();
				} else {
					Bukkit.broadcastMessage("Error: oneMinuteCountdown cannot be assigned to correct Scoreboard");
				}

				if (time == 0) {
					stopTimer();
					return;
				}
				
				break;
			//deathmatch
			case "deathmatch":
				HGListener.timeRemaining = time;
				HGListener.timeElapsed++;
				
				HGScoreboard.updateGameScoreboard();
				
				if (time == 0) {
					mainInstance.finishGame();
	        		stopTimer();
	                return;
	            }
				if (time == 1) {
	        		mainInstance.oneMinuteCountdown(60);
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Game will end in 1 minute");
	        		break;
	        	}
				if (time == 5) {
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Time remaining until game end: " + ChatColor.WHITE + time + " minutes");
	        		break;
	        	}
	        	if (time % 30 == 0) {
	                Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Time remaining until game end: " + ChatColor.WHITE + time + " minutes");
	                break;
	        	}
				break;
		
			//finishgame
			case "finishgame":
				if (time == 0) {
	                mainInstance.stopGame();
	                stopTimer();
	                return;
	            }
	        	if (time == Main.returnConfig().getInt("game_finish_time_seconds")) {
	        		
	        		if (Main.players.size() == 1) {
	        			Contributor winner = Main.players.get(0);
	        			
	        			Bukkit.broadcastMessage("");
	    				Bukkit.broadcastMessage("-----------------------------------------------------");
	    				Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + winner.name + ChatColor.RESET + " won the game! Kills: " + ChatColor.AQUA + winner.kills);
	    				Bukkit.broadcastMessage("");
	    				Bukkit.broadcastMessage("1. " + ChatColor.GREEN + HGListener.winnerName);
	    				Bukkit.broadcastMessage("2. " + ChatColor.GREEN + HGListener.secondName);
	    				Bukkit.broadcastMessage("3. " + ChatColor.GREEN + HGListener.thirdName);
	    				Bukkit.broadcastMessage("-----------------------------------------------------");
	    				Bukkit.broadcastMessage("");
	    				
	    				for (Player p : Bukkit.getOnlinePlayers()) {
	            			p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 15, 1);
	            		}
	    				
	    				Main.players.get(0).updateWins();
	        		} else {
	        			Bukkit.broadcastMessage("");
	    				Bukkit.broadcastMessage("-----------------------------------------------------");
	    				Bukkit.broadcastMessage("");
	        			Bukkit.broadcastMessage(ChatColor.GOLD + "Looks like nobody won..");
	        			Bukkit.broadcastMessage("");
	        			Bukkit.broadcastMessage("-----------------------------------------------------");
	    				Bukkit.broadcastMessage("");
	    				
	    				for (Player p : Bukkit.getOnlinePlayers()) {
	            			p.playSound(p.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 15, -1);
	            		}
	        		}
	        		break;
	        	}
	        	if (time == 5) {
	        		Bukkit.broadcastMessage(ChatColor.GOLD + "The game will restart in 5 seconds. Server will lag a bit");
	        		break;
	        	}
				break;
		
			//game cooldown
			case "gamecooldown":
				if (time == 0) {
					HGListener.cooldown = false;
					if (Bukkit.getServer().getOnlinePlayers().size() >= Main.returnConfig().getInt("min_players")) {
						mainInstance.startGame();
					}
					HGScoreboard.updateOnlineScoreboard();
					stopTimer();
					return;
				}
				break;
			//fireworks
			case "fireworks":
				if (time == 1 || Main.players.size() > 1) {
					stopTimer();
					return;
				}
				
				if (time % 4 == 0) {
					break;
				}
				
				Contributor c = Main.players.size() == 1 ? Main.players.get(0) : null;
				
				if (c != null) {
					Player p = c.getPlayer();
					
					p.setAllowFlight(true);
					p.setFlying(true);
					
					Location loc = p.getLocation();
					loc.setY(loc.getY() + 5);
					
					Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			        FireworkMeta fwm = fw.getFireworkMeta();
			        
			        fwm.setPower(0);
			        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).withFade(Color.AQUA).build());
			        
			        fw.setFireworkMeta(fwm);
			        fw.detonate();
			        
			        //Creates a new TNTPrimed entity
			        Entity tnt = p.getWorld().spawn(loc, TNTPrimed.class);
			         
			        //Sets it to detonate after x ticks
			        ((TNTPrimed) tnt).setFuseTicks(60);
			        
			        Vector direction = new Vector();
	                direction.setX(0.0D + Math.random() - Math.random());
	                direction.setY(0.65D);
	                direction.setZ(0.0D + Math.random() - Math.random());
			         
			        //scales up its velocity so it will move x times faster
			        tnt.setVelocity(direction.multiply(2.5));
			        
			        for (int i = 0 ; i < 1; i++){
			            Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			            fw2.setFireworkMeta(fwm);
			        }
			        break;
				}
				break;
			//showram
			case "showramusage":
				Runtime r = Runtime.getRuntime();
				long memUsed = (r.totalMemory() - r.freeMemory()) / 1048576; //Converting
				
				Bukkit.getLogger().info("Current memory allocation: " + String.valueOf(memUsed) + " megabytes");
				
				r = null;
				break;
			//timed messages
			case "timed_messages":
				
				if (HGListener.firstTipMessage) {
					HGListener.firstTipMessage = false;
					break;
				}
				
				List<String> messages = Main.returnConfig().getStringList("tip_messages");
				int messagesSize = messages.size();
				int random = (int) Math.floor(Math.random() * messagesSize);

				if (Main.lastint == random) {
					random++;
					if (random > (messagesSize - 1)) {
						random -= 2;
						if (random < 0) {
							random = 0; //Preventing underflow
						}
						Bukkit.broadcastMessage(ChatColor.YELLOW + messages.get(random));
						
					} else {
						Bukkit.broadcastMessage(ChatColor.YELLOW + messages.get(random));
					}
				} else {
					Bukkit.broadcastMessage(ChatColor.YELLOW + messages.get(random));
				}
				Main.lastint = random;
				time = 1;
				break;
			/*	
			//tracking
			//TODO
			//HGListener.trackPlayer()
			//Discouraged since they can teleport
			case "tracking_spectator":
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
	        		Inventory inv = p.getInventory();
	        		for (ItemStack i : inv) {
	        			if(i != null && i.getType().equals(Material.COMPASS)) {
	        				if (i.getType() == Material.COMPASS) {
	        						
	        					Player tracked = HGListener.getNearest(p, (double) 10000, (double) 260);
	        					String trackedName;
	        					double distance;
	        						
	        					if (tracked != null) {
	        						trackedName = tracked.getName();
	        						distance = p.getLocation().distance(tracked.getLocation());
	        						distance=Math.round(distance*10.0)/10.0;
	        						p.setCompassTarget(tracked.getLocation());
	        					} else {
	        						trackedName = "Nobody";
	        						distance = 0;
	        					}
	        						
	        					ItemMeta itemMeta = i.getItemMeta();
	        					itemMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD +  trackedName + ChatColor.WHITE + " Distance: " + ChatColor.YELLOW + distance + " blocks");
	        					i.setItemMeta(itemMeta);
	        				}
	        			}
	        		}
	        	}
				time = 1;
				break;
				*/
			default:
				Bukkit.broadcastMessage("Error: Taskname not found, please check syntax");
				stopTimer();
				return;
		}
		time--;
	}
}
