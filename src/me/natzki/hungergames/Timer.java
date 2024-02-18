package me.natzki.hungergames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Timer {
	
	Task t;

	Main mainInstance;
	BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	int time;
	long ticks;
	int taskID;
	String taskName;
	
	Player player;
	Location last;
	
	public Timer (Main instance, int time, String name, long ticks) {
		mainInstance = instance;
		
		this.time = time;
		this.ticks = ticks;
		taskName = name;

		t = new Task(time, taskName, this.player, this.last);
	}
	
	public Timer (Main instance, int time, String name, long ticks, Player player, Location last) {
		mainInstance = instance;
		
		this.time = time;
		this.ticks = ticks;
		taskName = name;
		this.player = player;
		this.last = last;
		
		t = new Task(time, taskName, player, last);
	}
	
	public void startTimer() {
		
        taskID = scheduler.scheduleSyncRepeatingTask(mainInstance, new Runnable() {  	
        	
            @Override 
            public void run() {
            	t.executeTask(taskName);
            }
            
        }, 0L, ticks);	
        
        t.setTaskID(taskID);
        
        Main.tasks.put(taskName, taskID);
        
	}
}
