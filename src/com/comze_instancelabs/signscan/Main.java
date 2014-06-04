package com.comze_instancelabs.signscan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin {

	JavaPlugin plugin = null;
	
	int currentsecondsgoing = 0;
	int currentaddressesfound = 0;
	
	// hashmap with two arrays in a hashmap, shit just got serious
	//HashMap<Integer, HashMap<String[], int[]>> dude = new HashMap<Integer, HashMap<String[], int[]>>();
	// nvm, let's try a class
	static ArrayList<Address> addresses = new ArrayList<Address>();
	
	public void onEnable(){
		plugin = this;
		getConfig().addDefault("mysql.password", "pw");
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		MySQL = new MySQL("localhost", "3306", "addresses", "root", getConfig().getString("mysql.password"));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("scan")){
			if(!(sender instanceof Player)){
				return true;
			}
			Player p = (Player)sender;
			if(args.length < 2){
				sender.sendMessage(ChatColor.RED + "Usage: /scan endx endz");
				return true;
			}

			int x = Integer.parseInt(args[0]);
			int z = Integer.parseInt(args[1]);

			currentcount = 0;
			currentsecondsgoing = 0;
			currentaddressesfound = 0;
			startScan(sender, p.getWorld(), new int[]{p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()}, new int[]{x, 0, z});
			return true;
		}else if(cmd.getName().equalsIgnoreCase("scantest")){
			if(!(sender instanceof Player)){
				return true;
			}
			Player p = (Player)sender;
			if(args.length < 2){
				sender.sendMessage(ChatColor.RED + "Usage: /scan endx endz");
				return true;
			}

			int x = Integer.parseInt(args[0]);
			int z = Integer.parseInt(args[1]);
			
			currentcount = 0;
			currentsecondsgoing = 0;
			currentaddressesfound = 0;
			startTestScan(sender, p.getWorld(), new int[]{p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()}, new int[]{x, 0, z});
			return true;
		}
		return false;
	}
	
	BukkitTask t;
	
	public void startTestScan(CommandSender sender, final World w, final int[] start, int[] stop){
		int xtemp = 0;
		int ztemp = 0;
		if(stop[0] < start[0]){
			xtemp = start[0] - stop[0];
			int ttemp = start[0];
			start[0] = stop[0];
			stop[0] = ttemp;
		}else{
			xtemp = stop[0] - start[0];
		}
		if(stop[2] < start[2]){
			ztemp = start[2] - stop[2];
			int ttemp = start[2];
			start[2] = stop[2];
			stop[2] = ttemp;
		}else{
			ztemp = stop[2] - start[2];
		}
		final int xlength = xtemp;
		final int zlength = ztemp;

		sender.sendMessage(Integer.toString(xlength) + " " + Integer.toString(zlength));
		
		t = Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			public void run(){
				currentsecondsgoing++;
			}
		}, 0L, 20L);
		
		Thread r = new Thread(new Runnable(){
			@Override
			public void run() {
				for(int x = 0; x < xlength; x++){
					for(int z = 0; z < zlength; z++){
						boolean error = false;
						int[] temp = new int[3];
						for(int y = 20; y < 160; y++){
							Block b = null;
							try{
								b = w.getBlockAt(start[0] + x, y, start[2] + z);
							}catch(Exception e){
								error = true;
								temp = new int[]{start[0] + x, y, start[2] + z};
								continue;
							}
							//System.out.println((start[0] + x) + " " + y + " " + (start[2] + z) + b.getType().toString());
							if(b.getType() == Material.SIGN_POST){
								BlockState bs = b.getState();
								if(bs != null){
									scan((Sign) bs, false, false, start[0], start[2]);
									currentaddressesfound++;
								}
							}
						}
						if(error){
							final int[] temp_ = temp;
							Bukkit.getScheduler().runTask(plugin, new Runnable(){
								public void run(){
									int[] i = temp_;
									Block b = w.getBlockAt(i[0], i[1], i[2]);
									if(b.getType() == Material.SIGN_POST){
										BlockState bs = b.getState();
										if(bs != null){
											scan((Sign) bs, false, false, start[0], start[2]);
											currentaddressesfound++;
										}
									}
								}
							});
						}
					}	
				}
				
				// stop everything after everything's done!
				Bukkit.broadcastMessage(ChatColor.RED + "Seconds: " + Integer.toString(currentsecondsgoing) + " ; Addresses: " + Integer.toString(currentaddressesfound));
				Bukkit.broadcastMessage(ChatColor.RED + Integer.toString(currentaddressesfound / currentsecondsgoing) + " addr/s");

				t.cancel();
			}
		});
		r.start();
		threads.add(r);
	}
	
	
	
	ArrayList<Thread> threads = new ArrayList<Thread>();
	int currentcount = 0;
	boolean cont = true;
	
	public void startScan(CommandSender sender, final World w, final int[] start, int[] stop){
		int xtemp = 0;
		int ztemp = 0;
		if(stop[0] < start[0]){
			xtemp = start[0] - stop[0];
			int ttemp = start[0];
			start[0] = stop[0];
			stop[0] = ttemp;
		}else{
			xtemp = stop[0] - start[0];
		}
		if(stop[2] < start[2]){
			ztemp = start[2] - stop[2];
			int ttemp = start[2];
			start[2] = stop[2];
			stop[2] = ttemp;
		}else{
			ztemp = stop[2] - start[2];
		}
		final int xlength = xtemp;
		final int zlength = ztemp;

		sender.sendMessage(Integer.toString(xlength) + " " + Integer.toString(zlength));
		
		Thread r = new Thread(new Runnable(){
		//Bukkit.getScheduler().runTask(this, new Runnable(){
			@Override
			public void run() {
				for(int x = 0; x < xlength; x++){
					for(int z = 0; z < zlength; z++){
						final int x_ = x;
						final int z_ = z;
						Bukkit.getScheduler().runTask(plugin, new Runnable(){
							public void run(){
								if(w.getBlockAt(start[0] + x_, 0, start[2] + z_).getType() == Material.AIR){
									cont = false;
								}
							}
						});
						boolean error = false;
						int[] temp = new int[3];
						for(int y = 20; y < 160; y++){
							if(!cont){
								break;
							}
							Block b = null;
							try{
								b = w.getBlockAt(start[0] + x, y, start[2] + z);
							}catch(Exception e){
								error = true;
								temp = new int[]{start[0] + x, y, start[2] + z};
								continue;
							}
							//System.out.println((start[0] + x) + " " + y + " " + (start[2] + z) + b.getType().toString());
							if(b.getType() == Material.SIGN_POST){
								BlockState bs = b.getState();
								if(bs != null){
									scan((Sign) bs, true, false, start[0], start[2]);
									currentcount++;
								}
							}
						}
						if(error){
							final int[] temp_ = temp;
							Bukkit.getScheduler().runTask(plugin, new Runnable(){
								public void run(){
									int[] i_ = temp_;
									Block b = w.getBlockAt(i_[0], i_[1], i_[2]);
									if(b.getType() == Material.SIGN_POST){
										BlockState bs = b.getState();
										if(bs != null){
											scan((Sign) bs, true, false, i_[0], i_[2]);
											currentcount++;
										}
									}
								}
							});
						}
					}
				}
				
				// at the end:
				// AW PLEASE DON'T YOU CRASH HERE
				saveSigns();
			}
		});

		r.start();
		threads.add(r);
	}
	
	public void stopScan(){
		
	}
	
	public void scan(Sign s, boolean add, boolean verbose, int x, int z){
		String street = "";
		String number = "";
		for(String s__ : s.getLines()){
			if(s__.matches(".*\\d.*")){
				number = s__;
			}else{
				if(s__.length() > 1){
					street += s__;
				}
			}
		}
		if(verbose){
			Bukkit.broadcastMessage(ChatColor.GREEN + Integer.toString(s.getLocation().getBlockX()) + " " + Integer.toString(s.getLocation().getBlockY()) + " " + Integer.toString(s.getLocation().getBlockZ()) + ". Street: " + street + " " + number);	
		}
		if(add){
			addresses.add(new Address(street, number, x, z));
			//addSign(street, number, x, z);
		}
	}
	
	
	static MySQL MySQL = null;
	static Connection c = null;
	
	public static void addSign(String street, String number, int x, int z){
    	c = MySQL.open();
		
		try {
			c.createStatement().executeUpdate("INSERT INTO address VALUES('0', '" + street + "', '" + number + "', '" + Integer.toString(x) + "', '" + Integer.toString(z) + "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveSigns(){
		for(Address a : addresses){
			addSign(a.getStreet(), a.getNumber(), a.getX(), a.getZ());
		}
	}

}
