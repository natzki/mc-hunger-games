package me.natzki.hungergames;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class HGScoreboard {

	public static Scoreboard onlineScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	public static Scoreboard gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	
	private static String 
	last1,
	last2,
	last3,
	last4,
	last5,
	last6;
	
	public HGScoreboard() {
		
	}
	
	@SuppressWarnings("deprecation")
	public static void createOnlineScoreboard() {
		Objective obj = onlineScoreboard.registerNewObjective("Contributors", "");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Contributors");
		
		//Space
		Team space = onlineScoreboard.registerNewTeam("space");
		space.setPrefix("");
		space.addEntry("");
		//Online Counter
		Team onlineCounter = onlineScoreboard.registerNewTeam("onlineCounter");
		onlineCounter.addEntry(" ");
		//Space 2
		Team space2 = onlineScoreboard.registerNewTeam("space2");
		space2.setPrefix("");
		space2.addEntry("  ");
		//Enough Players
		Team enoughPlayers = onlineScoreboard.registerNewTeam("enoughPlayers");
		enoughPlayers.addEntry("   ");
		//Space 3
		Team space3 = onlineScoreboard.registerNewTeam("space3");
		space3.setPrefix("");
		space3.addEntry("    ");
		//Countdown
		Team countdown = onlineScoreboard.registerNewTeam("countdown");
		countdown.addEntry("     ");
		//Space 4
		Team space4 = onlineScoreboard.registerNewTeam("space4");
		space4.setPrefix("");
		space4.addEntry("      ");
	}
	
	public static void updateOnlineScoreboard() {
		Objective obj = onlineScoreboard.getObjective("Contributors");
		Team onlineCounter = onlineScoreboard.getTeam("onlineCounter");
		Team enoughPlayers = onlineScoreboard.getTeam("enoughPlayers");
		Team countdown = onlineScoreboard.getTeam("countdown");
		
		onlineCounter.setPrefix(Main.players.size() + "/" + Main.returnConfig().getInt("max_players"));
		if (HGListener.cooldown) {
			enoughPlayers.setPrefix(ChatColor.YELLOW + "" + ChatColor.BOLD + "Cooldown");
		} else {
			enoughPlayers.setPrefix(ChatColor.RED + "" + ChatColor.BOLD + "Not enough players");
		}
		
		if (HGListener.gameStarting) {
			enoughPlayers.setPrefix(ChatColor.GREEN + "" + ChatColor.BOLD + "Game starting");
			
			if (HGListener.gameStartingSeconds < 60) {
				if (HGListener.gameStartingSeconds < 10) {
					countdown.setPrefix("0:0" + HGListener.gameStartingSeconds);
				} else {
				countdown.setPrefix("0:" + HGListener.gameStartingSeconds);
				}
			} else {
				int timeRemaining = (int) Math.floor(HGListener.gameStartingSeconds / 60);
				String suffix = timeRemaining > 1 ? " minutes" : " minute";
				countdown.setPrefix( timeRemaining + suffix);
			}
			
			obj.getScore("").setScore(7);
    		obj.getScore(" ").setScore(6);
    		obj.getScore("  ").setScore(5);
    		obj.getScore("   ").setScore(4);
    		obj.getScore("    ").setScore(3);
    		obj.getScore("     ").setScore(2);
    		obj.getScore("      ").setScore(1);
		} else {
			onlineScoreboard.resetScores("     ");
			onlineScoreboard.resetScores("      ");
			
			obj.getScore("").setScore(5);
    		obj.getScore(" ").setScore(4);
    		obj.getScore("  ").setScore(3);
    		obj.getScore("   ").setScore(2);
    		obj.getScore("    ").setScore(1);
		}

		@SuppressWarnings("unused")
		int updateID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
		    @Override
			public void run() {
		    	for (Player p : Bukkit.getOnlinePlayers()) {
		    		
		    		p.setScoreboard(onlineScoreboard);
		        }
		    }
		}, 5L);
	}
	
	
	@SuppressWarnings("deprecation")
	public static void createGameScoreboard() {
		Objective obj = gameScoreboard.registerNewObjective("playersalive", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Contributors");
		
		Team aliveCounter = gameScoreboard.registerNewTeam("aliveCounter");
		aliveCounter.addEntry("");
		
		Team space = gameScoreboard.registerNewTeam("space");
		space.setPrefix("");
		space.addEntry(ChatColor.BLACK + "");
		Team space2 = gameScoreboard.registerNewTeam("space2");
		space2.setPrefix("");
		space2.addEntry(ChatColor.WHITE + "");
		Team space3 = gameScoreboard.registerNewTeam("space3");
		space3.setPrefix("");
		space3.addEntry(ChatColor.AQUA + "");
		Team space4 = gameScoreboard.registerNewTeam("space4");
		space4.setPrefix("");
		space4.addEntry(ChatColor.BLUE + "");
		
		Team player1 = gameScoreboard.registerNewTeam("player1");
		player1.setPrefix("");
		Team player2 = gameScoreboard.registerNewTeam("player2");
		player2.setPrefix("");
		Team player3 = gameScoreboard.registerNewTeam("player3");
		player3.setPrefix("");
		Team player4 = gameScoreboard.registerNewTeam("player4");
		player4.setPrefix("");
		Team player5 = gameScoreboard.registerNewTeam("player5");
		player5.setPrefix("");
		Team player6 = gameScoreboard.registerNewTeam("player6");
		player6.setPrefix("");
        
		Team timeLeft = gameScoreboard.registerNewTeam("timeLeft");
		timeLeft.setPrefix("");
		
	}
	
	public static void updateGameScoreboard() {
		Objective obj = gameScoreboard.getObjective("playersalive");
		Team aliveCounter = gameScoreboard.getTeam("aliveCounter");
		
		Team player1 = gameScoreboard.getTeam("player1");
		Team player2 = gameScoreboard.getTeam("player2");
		Team player3 = gameScoreboard.getTeam("player3");
		Team player4 = gameScoreboard.getTeam("player4");
		Team player5 = gameScoreboard.getTeam("player5");
		Team player6 = gameScoreboard.getTeam("player6");
		
		Team timeLeft = gameScoreboard.getTeam("timeLeft");

		//Run task 5 ticks later to be sure player count is updated
	    @SuppressWarnings("unused")
		int updateID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
		    @Override
			public void run() {
		    	LinkedHashMap<String, Integer> sortedContributors = new LinkedHashMap<String, Integer>();
				
				for (Contributor c : Main.players) {
					sortedContributors.put(c.name, c.kills);
				}

			    //sorting the map in decreasing order of value
			    sortedContributors = sortedContributors
			        .entrySet()
			        .stream()
			        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
			        .collect(
			            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
			                LinkedHashMap::new));
			    
			    String[] players = sortedContributors.keySet().toArray(new String[sortedContributors.size()]);
			    int length = players.length;
			    String t = ChatColor.BOLD + "";
			    
			    String player1name = length > 0 ? players[0] : " ";
			    String player2name = length > 1 ? players[1] : "  ";
			    String player3name = length > 2 ? players[2] : "   ";
			    String player4name = length > 3 ? players[3] : "    ";
			    String player5name = length > 4 ? players[4] : "     ";
			    String player6name = length > 5 ? players[5] : "      ";
			    boolean player1exists = !player1name.equals(" ");
			    boolean player2exists = !player2name.equals("  ");
			    boolean player3exists = !player3name.equals("   ");
			    boolean player4exists = !player4name.equals("    ");
			    boolean player5exists = !player5name.equals("     ");
			    boolean player6exists = !player6name.equals("      ");
			    
			    gameScoreboard.resetScores(t + last1);
			    gameScoreboard.resetScores(t + last2);
			    gameScoreboard.resetScores(t + last3);
			    gameScoreboard.resetScores(t + last4);
			    gameScoreboard.resetScores(t + last5);
			    gameScoreboard.resetScores(t + last6);
			    
		    	player1.addEntry(t + player1name);
			    player2.addEntry(t + player2name);
			    player3.addEntry(t + player3name);
			    player4.addEntry(t + player4name);
			    player5.addEntry(t + player5name);
			    player6.addEntry(t + player6name);
			    
			    last1 = player1name;
			    last2 = player2name;
			    last3 = player3name;
			    last4 = player4name;
			    last5 = player5name;
			    last6 = player6name;
			    
				int player1kills = sortedContributors.get(player1name) != null ? sortedContributors.get(player1name) : 0;
				int player2kills = sortedContributors.get(player2name) != null ? sortedContributors.get(player2name) : 0;
				int player3kills = sortedContributors.get(player3name) != null ? sortedContributors.get(player3name) : 0;
				int player4kills = sortedContributors.get(player4name) != null ? sortedContributors.get(player4name) : 0;
				int player5kills = sortedContributors.get(player5name) != null ? sortedContributors.get(player5name) : 0;
				int player6kills = sortedContributors.get(player6name) != null ? sortedContributors.get(player6name) : 0;
				
				if (player1exists) {
					player1.setSuffix(ChatColor.AQUA + " " + player1kills + " kills");
					if (Main.players.size() == 1 && HGListener.winnerName != null) {
						player1.setSuffix(ChatColor.AQUA + " (Winner)");
					}
				} else {
					player1.addEntry(player1name);
					player1.setSuffix("");
				}
				if (player2exists) {
					player2.setSuffix(ChatColor.AQUA + " " + player2kills + " kills");
				} else {
					player2.addEntry(player2name);
					player2.setSuffix("");
				}
				if (player3exists) {
					player3.setSuffix(ChatColor.AQUA + " " + player3kills + " kills");
				} else {
					player3.addEntry(player3name);
					player3.setSuffix("");
				}
				if (player4exists) {
					player4.setSuffix(ChatColor.AQUA + " " + player4kills + " kills");
				} else {
					player4.addEntry(player4name);
					player4.setSuffix("");
				}
				if (player5exists) {
					player5.setSuffix(ChatColor.AQUA + " " + player5kills + " kills");
				} else {
					player5.addEntry(player5name);
					player5.setSuffix("");
				}
				if (player6exists) {
					player6.setSuffix(ChatColor.AQUA + " " + player6kills + " kills");
				} else {
					player6.addEntry(player6name);
					player6.setSuffix("");
				}
				
				timeLeft.addEntry(ChatColor.RESET + "");
				
				aliveCounter.setPrefix(Main.players.size() + " Contributors remaining");
	    		
				int timeLeftMinutes = (HGListener.timeRemaining - 1) < 0 ? 0 : HGListener.timeRemaining - 1;
				//TODO
	    		String suffix = HGListener.timeRemaining > 1 ? " minutes" : ":" + HGListener.oneMinuteSecondsRemaining;
	    		if (HGListener.oneMinuteSecondsRemaining < 10)
	    			suffix = ":0" + HGListener.oneMinuteSecondsRemaining;
	    		
	    		//timeRemaining is -1 because the GUI uses time left which is useful when displaying seconds, get it?
	    		if (HGListener.deathmatch) {
	    			timeLeft.setPrefix(ChatColor.RED + "" + ChatColor.BOLD + "Deathmatch " + ChatColor.RESET + timeLeftMinutes + suffix);
	    		} else if (HGListener.gameFinished) {
	    			timeLeft.setPrefix(ChatColor.GREEN + "" + ChatColor.BOLD + "Game Finished");
	    		} else {
	    			timeLeft.setPrefix(ChatColor.GOLD + "" + ChatColor.BOLD + "Time remaining: " + ChatColor.RESET + timeLeftMinutes + suffix);
	    		}
	    		
	    		int player1score = 9;
	    		
	    		if (!player6exists)
	    			player1score--;
	    		if (!player5exists)
	    			player1score--;
	    		if (!player4exists)
	    			player1score--;
	    		if (!player3exists)
	    			player1score--;
	    		if (!player2exists)
	    			player1score--;
	    		if (!player1exists)
	    			player1score--;

	    		obj.getScore(ChatColor.BLACK + "").setScore(player1score+3);
	    		obj.getScore("").setScore(player1score+2);
	    		obj.getScore(ChatColor.WHITE + "").setScore(player1score+1);
	    		
	    		if (player1exists)
	    			obj.getScore(t + player1name).setScore(player1score);
	    		if (player2exists)
	    			obj.getScore(t + player2name).setScore(player1score-1);
	    		if (player3exists)
	    			obj.getScore(t + player3name).setScore(player1score-2);
	    		if (player4exists)
	    			obj.getScore(t + player4name).setScore(player1score-3);
	    		if (player5exists)
	    			obj.getScore(t + player5name).setScore(player1score-4);
	    		if (player6exists)
	    			obj.getScore(t + player6name).setScore(player1score-5);
	    		
	    		obj.getScore(ChatColor.AQUA + "").setScore(3);
	    		obj.getScore(ChatColor.RESET + "").setScore(2);
	    		obj.getScore(ChatColor.BLUE + "").setScore(1);
					
		    	for (Player p : Bukkit.getOnlinePlayers()) {
		    		p.setScoreboard(gameScoreboard);
		        }
		    }
		}, 5L);
	}
}
