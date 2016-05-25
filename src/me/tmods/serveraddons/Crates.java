package me.tmods.serveraddons;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.tmods.api.Sound;
import me.tmods.serverutils.Methods;
public class Crates extends JavaPlugin implements Listener{
	public File maincfgfile = new File("plugins/TModsServerUtils", "data.yml");
	public FileConfiguration maincfg = YamlConfiguration.loadConfiguration(maincfgfile);
	public HashMap<Player,Integer> tasks = new HashMap<Player,Integer>();
	@Override
	public void onEnable() {
		try {
		Bukkit.getPluginManager().registerEvents(this, this);
		if (maincfg.getConfigurationSection("Crates") == null) {
			maincfg.set("Crates.temp", "temporary");
			try {
				maincfg.save(maincfgfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			maincfg.set("Crates.temp", null);
			try {
				maincfg.save(maincfgfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
		try {
		if (cmd.getName().equalsIgnoreCase("crate")) {
			if (args.length != 1) {
				sender.sendMessage(Methods.getLang("wichtypecrate"));
				return false;
			}
			if (!sender.hasPermission("ServerAddons.crate")) {
				sender.sendMessage(Methods.getLang("permdeny"));
				return true;
			}
			if (!maincfg.getConfigurationSection("Crates").contains(args[0])) {
				sender.sendMessage(Methods.getLang("unknowncrate"));
				return true;
			}
			if (sender instanceof Player) {
				((Player) sender).getLocation().getBlock().setType(Material.CHEST);
				ItemStack is = new ItemStack(Material.BEDROCK);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(args[0]);
				is.setItemMeta(im);
				((Chest)((Player) sender).getLocation().getBlock().getState()).getInventory().setItem(0, is);
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("createcrate")) {
			if (args.length != 1) {
				sender.sendMessage(Methods.getLang("wichtypecrate"));
				return false;
			}
			if (!sender.hasPermission("ServerAddons.crate")) {
				sender.sendMessage(Methods.getLang("permdeny"));
				return true;
			}
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.getTargetBlock((Set<Material>)null, 200).getType().equals(Material.CHEST)) {
					Chest chest = (Chest) p.getTargetBlock((Set<Material>)null, 200).getState();
					if (chest.getBlockInventory().getContents().length > 0) {
						for (int i = 0;i < chest.getInventory().getContents().length;i++) {
							maincfg.set("Crates." + args[0] + "." + i, chest.getInventory().getContents()[i]);
						}
						sender.sendMessage(Methods.getLang("crateset"));
						try {
							maincfg.save(maincfgfile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					}
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("delcrate")) {
			if (args.length != 1) {
				sender.sendMessage(Methods.getLang("wichtypecrate"));
				return false;
			}
			if (!sender.hasPermission("ServerAddons.crate")) {
				sender.sendMessage(Methods.getLang("permdeny"));
				return true;
			}
			if (!maincfg.getConfigurationSection("Crates").contains(args[0])) {
				sender.sendMessage(Methods.getLang("unknowncrate"));
				return true;
			}
			if (sender instanceof Player) {
				maincfg.set("Crates." + args[0], null);
				sender.sendMessage(Methods.getLang("cratedel"));
				try {
					maincfg.save(maincfgfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("cratekey")) {
			if (args.length != 1) {
				sender.sendMessage(Methods.getLang("wichtypecrate"));
				return false;
			}
			if (!sender.hasPermission("ServerAddons.crate")) {
				sender.sendMessage(Methods.getLang("permdeny"));
				return true;
			}
			if (!maincfg.getConfigurationSection("Crates").contains(args[0])) {
				sender.sendMessage(Methods.getLang("unknowncrate"));
				return true;
			}
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (Methods.getItemInHand(p) == null) {
					maincfg.set("Keys." + args[0], null);
				} else {
					maincfg.set("Keys." + args[0], Methods.getItemInHand(p));
				}
				try {
					maincfg.save(maincfgfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sender.sendMessage(Methods.getLang("keyset"));
				return true;
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
		return false;
	}
	@EventHandler
	public void inventoryOpen(InventoryOpenEvent event) {
		try {
		if (event.getInventory().getItem(0) != null && event.getInventory().getItem(0).hasItemMeta()) {
			if (event.getInventory().getItem(0).getType() == Material.BEDROCK && maincfg.getConfigurationSection("Crates").getKeys(false).contains(event.getInventory().getItem(0).getItemMeta().getDisplayName())) {
				boolean cancelled = false;
				if (maincfg.getItemStack("Keys." + event.getInventory().getItem(0).getItemMeta().getDisplayName()) != null) {
					if (Methods.getItemInHand((Player) event.getPlayer()) == null) {
						event.getPlayer().closeInventory();
						event.getPlayer().sendMessage(Methods.getLang("nokeyfound"));
						cancelled = true;
					} else {
						if (!Methods.getItemInHand((Player) event.getPlayer()).isSimilar(maincfg.getItemStack("Keys." + event.getInventory().getItem(0).getItemMeta().getDisplayName()))) {
							event.getPlayer().closeInventory();
							event.getPlayer().sendMessage(Methods.getLang("wrongkey"));
							cancelled = true;
						}
					}
					if (!cancelled) {
						if (Methods.getItemInHand((Player) event.getPlayer()).getAmount() > 1) {
							ItemStack i = Methods.getItemInHand((Player) event.getPlayer());
							i.setAmount(i.getAmount() - 1);
							Methods.setItemInHand((Player) event.getPlayer(), i);
						} else {
							Methods.setItemInHand((Player) event.getPlayer(), null);
						}
					}
				}
				if (!cancelled) {
					tasks.put((Player) event.getPlayer(), Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
						Integer timeout = 0;
						@Override
						public void run() {
							try {
							int rand = (int) Math.round(Math.random() * (maincfg.getConfigurationSection("Crates." + event.getInventory().getItem(0).getItemMeta().getDisplayName()).getKeys(false).size()-1));
							ItemStack is = maincfg.getItemStack("Crates." + event.getInventory().getItem(0).getItemMeta().getDisplayName() + "." + maincfg.getConfigurationSection("Crates." + event.getInventory().getItem(0).getItemMeta().getDisplayName()).getKeys(false).toArray()[rand]);
							event.getInventory().setItem(13, is);
							Methods.playSound(Sound.NOTE_PIANO, event.getPlayer().getLocation(), (Player) event.getPlayer());
							if (timeout >= 100) {
								event.getPlayer().getInventory().addItem(event.getInventory().getItem(13));
								if (tasks.containsKey(event.getPlayer())) {
									Bukkit.getScheduler().cancelTask(tasks.get(event.getPlayer()));
									tasks.remove(event.getPlayer());
								}
							}
							timeout += 1;
							} catch (Exception e) {
								Methods.log(e);
							}
						}
					}, 1, 1));
				} else {
					event.setCancelled(true);
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		try {
		if (event.getInventory().getItem(0) != null) {
			if (event.getInventory().getItem(0).hasItemMeta()) {
				if (maincfg.getConfigurationSection("Crates").getKeys(false).contains(event.getInventory().getItem(0).getItemMeta().getDisplayName())) {
					if (tasks.containsKey(event.getPlayer())) {
						Bukkit.getScheduler().cancelTask(tasks.get(event.getPlayer()));
						tasks.remove(event.getPlayer());
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		try {
		if (maincfg.getConfigurationSection("Crates") != null) {
			if (maincfg.getConfigurationSection("Crates").getKeys(false).size() > 0) {
				if (event.getInventory().getItem(0) != null) {
					if (event.getInventory().getItem(0).hasItemMeta()) {
						if (maincfg.getConfigurationSection("Crates").getKeys(false).contains(event.getInventory().getItem(0).getItemMeta().getDisplayName())) {
							event.setCancelled(true);
						}
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		try {
		if (event.getBlock().getType() == Material.CHEST) {
			if (((Chest)event.getBlock().getState()).getBlockInventory().getItem(0) != null) {
				if (((Chest)event.getBlock().getState()).getBlockInventory().getItem(0).hasItemMeta()) {
					if (maincfg.getConfigurationSection("Crates") != null) {
						if (maincfg.getConfigurationSection("Crates").getKeys(false).size() > 0) {
							if (maincfg.getConfigurationSection("Crates").getKeys(false).contains(((Chest)event.getBlock().getState()).getBlockInventory().getItem(0).getItemMeta().getDisplayName())) {
								if (!event.getPlayer().hasPermission("ServerAddons.crate")) {
									event.setCancelled(true);
								} else {
									((Chest)event.getBlock().getState()).getBlockInventory().setItem(0, null);
									((Chest)event.getBlock().getState()).getBlockInventory().setItem(13, null);
								}
							}
						}
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
}
