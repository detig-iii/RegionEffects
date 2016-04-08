package ch.swisssmp.regioneffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import ch.swisssmp.regioneffects.Main;

public class RegionEffectPlayerCommand implements CommandExecutor{
    CommandSender _sender;
    boolean consoleMode = true;
    Player p;
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	_sender = sender;
    	if(_sender instanceof Player){
    		consoleMode = false;
    		p = (Player)_sender;
    		if(!p.hasPermission("RegionEffects.use")){
    			p.sendMessage("Du hast keinen Zugriff auf diesen Befehl!");
    			return false;
    		}
    	}
    	if(args.length<1){
    		displayHelp();
    		return false;
    	}
    	else if(args[0].equals("help")){
    		if(args.length>=2){
    			if(args[1].equals("Aktionen")){
    				sendMessage("RegionEffects 'add' Aktionen erklärt");
    				sendMessage("specific: Alle anderen Effekte werden entfernt.");
    				sendMessage("regionOnly: Effekt gilt nur innerhalb der Region und wird beim Verlassen entfernt");
    				return true;
    			}
    		}
    		displayHelp();
    		return true;
    	}
    	if(args.length < 2){
    		displayHelp();
    		return false;
    	}
		boolean regionOnly = false;
		boolean specific = false;
		String region = args[0].toLowerCase();
		String command = args[1].toLowerCase();
		if(command.equals("a")){
			command="add";
		}
		else if((command.equals("addspecific"))||(command.equals("as"))){
			command="add";
			specific = true;
		}
		else if((command.equals("addregiononly"))||(command.equals("ar"))){
			command="add";
			regionOnly = true;
		}
		else if((command.equals("addspecificregiononly"))||(command.equals("addregiononlyspecific"))||(command.equals("ars"))||(command.equals("asr"))){
			command="add";
			specific = true;
			regionOnly = true;
		}
		else if((command.equals("ac"))){
			command = "addcondition";
		}
		else if((command.equals("change"))||(command.equals("e"))||(command.equals("edit"))){
			command = "add";
		}
		else if((command.equals("delete"))||(command.equals("r"))){
			command = "remove";
		}
		else if((command.equals("rc"))){
			command = "removecondition";
		}
		String effect;
		ConfigurationSection regionSection;
		ConfigurationSection effectSection;
		List<String> permissionList;
		switch(command){
		case "add":
	    	if(args.length < 3){
	    		displayHelp();
	    		return false;
	    	}
			effect = args[2].toLowerCase();
			if(PotionEffectType.getByName(effect)==null)
			{
				sendMessage("Fehler: Effekt "+effect+" existiert nicht!");
				return false;
			}
			int time = 10;
			int amplifier = 0;
			boolean ambient = false;
			boolean particles = true;
			String color = "default";
			if(args.length>3){
				try{
					time = Integer.parseInt(args[3]);
				}
				catch(Exception e){
					sendMessage("Warnung: Effektdauer ungültig: "+args[3]);
					sendMessage("Verwende Standardwert: "+time);
				}
			}
			if(args.length>4){
				try{
					amplifier = Integer.parseInt(args[4]);
				}
				catch(Exception e){
					sendMessage("Warnung: Verstärker ungültig: "+args[4]);
					sendMessage("Verwende Standardwert: "+amplifier);
				}
			}
			if(args.length>5){
				if(args[5].toLowerCase()=="true" || args[5].toLowerCase()=="ja")
					ambient = true;
				else
					ambient = false;
			}
			if(args.length>6){
				if(args[6].toLowerCase()=="true" || args[6].toLowerCase()=="ja")
					particles = true;
				else
					particles = false;
			}
			if(args.length>7){
				color = args[7];
			}
			World world = p.getWorld();
			RegionManager manager = Main.container.get(world);
			Map<String, ProtectedRegion> regions = manager.getRegions();
			if(regions.containsKey(region)){
				if(!Main.regions.contains(region)){
					regionSection = Main.regions.createSection(region);
				}
				else regionSection = Main.regions.getConfigurationSection(region);
				if(!regionSection.contains("effects")){
					effectSection = regionSection.createSection("effects");
				}
				else effectSection = regionSection.getConfigurationSection("effects");
				ConfigurationSection potionSection;
				if(!effectSection.contains(effect)){
					potionSection = effectSection.createSection(effect);
				}
				else potionSection = effectSection.getConfigurationSection(effect);
				potionSection.set("type", effect);
				potionSection.set("duration", time);
				potionSection.set("amplifier", amplifier);
				potionSection.set("ambient", ambient);
				potionSection.set("particles", particles);
				potionSection.set("color", color);
				potionSection.set("regionOnly", regionOnly);
				potionSection.set("specific", specific);
				sendMessage("Effekt "+effect+" in der Region "+region+" erstellt!");
			}
			else{
				sendMessage("Region existiert nicht: "+region);
			}
			//Main.container.get(world)
			break;
		case "remove":
	    	if(args.length < 3){
	    		displayHelp();
	    		return false;
	    	}
			effect = args[2].toLowerCase();
			if(Main.regions.contains(region)){
				regionSection = Main.regions.getConfigurationSection(region);
			}
			else{
				sendMessage("Region "+region+" nicht gefunden. (Existiert nicht oder hat keine Effekte)");
				break;
			}
			if(regionSection.contains("effects")){
				effectSection = regionSection.getConfigurationSection("effects");
			}
			else{
				sendMessage("Region "+region+" hat keine Effekte.");
				break;
			}
			if(effectSection.contains(effect)){
				effectSection.set(effect,  null);
				sendMessage("Effekt "+effect+" in der Region "+region+" entfernt!");
			}
			else{
				sendMessage("Effekt "+effect+" nicht gefunden. (Existiert nicht)");
				break;
			}
			break;
		case "clear":
			if(Main.regions.contains(region)){
				Main.regions.set(region, null);
				sendMessage("Alle Effekte von der Region "+region+" entfernt.");
			}
			else sendMessage("Region "+region+" nicht gefunden. (Existiert nicht oder hat keine Effekte)");
			break;
		case "info":
			if(Main.regions.contains(region)){
				regionSection = Main.regions.getConfigurationSection(region);
			}
			else{
				sendMessage("Region "+region+" nicht gefunden. (Existiert nicht oder hat keine Effekte)");
				break;
			}
			if(args.length>2){
				effect = args[2];
				if(effect.equals("permissions")){
					if(regionSection.contains("permissions")){
						permissionList = regionSection.getStringList("permissions");
					}
					else{
						sendMessage("Region "+region+" hat keine Berechtigungs-Einschränkungen.");
						break;
					}
					sendMessage("RegionEffects Region "+region+" Effekt "+effect+" Permissions:");
					sendMessage("------");
					sendMessage("Spieler benötigen:");
					for(String permission : permissionList)
						sendMessage(permission);
					sendMessage("------");
					break;
				}
				if(regionSection.contains("effects")){
					effectSection = regionSection.getConfigurationSection("effects");
				}
				else{
					sendMessage("Region "+region+" hat keine Effekte.");
					break;
				}
				sendMessage("RegionEffects Information:");
				ConfigurationSection potion = effectSection.getConfigurationSection(effect);
				String _type = potion.getString("type");
				int duration = potion.getInt("duration");
				amplifier = potion.getInt("amplifier");
				ambient = potion.getBoolean("ambient");
				particles = potion.getBoolean("particles");
				String _color = potion.getString("color");
				specific = potion.getBoolean("specific");
				regionOnly = potion.getBoolean("regionOnly");
				sendMessage(region+"/"+_type+":");
				sendMessage("Dauer: "+duration);
				sendMessage("Verstärker: "+amplifier);
				sendMessage("Umgebung: "+ambient);
				sendMessage("Partikel: "+particles);
				sendMessage("Farbe: "+_color);
				sendMessage("Spezifisch: "+specific);
				sendMessage("Nur innnerhalb der Region: "+regionOnly);
				sendMessage("Bedingungen anzeigen:");
				sendMessage("/re "+region+" info "+effect+" permissions");
			}
			else{
				sendMessage("RegionEffects Information:");
				String message = region+": ";
				if(regionSection.contains("effects")){
					effectSection = regionSection.getConfigurationSection("effects");
				}
				else{
					sendMessage("Region "+region+" hat keine Effekte.");
					break;
				}
				Set<String> effects = effectSection.getKeys(false);
				for (String effectName : effects){
					message += effectName+", ";
				}
				message.substring(0,  message.length()-2);
				sendMessage(message);
			}
			break;
		case "addcondition":
			String permission;
			//re [region] ap [permission]
	    	if(args.length < 3){
	    		displayHelp();
	    		return false;
	    	}
    		permission = args[2].trim();
			if(Main.regions.contains(region)){
				regionSection = Main.regions.getConfigurationSection(region);
			}
			else{
				sendMessage("Region "+region+" nicht gefunden. (Existiert nicht oder hat keine Effekte)");
				break;
			}
			permissionList = regionSection.getStringList("permissions");
			if(!permissionList.contains(permission))
				permissionList.add(permission);
			regionSection.set("permissions", permissionList);
			sendMessage("Bedingung "+permission+" der Region "+region+" hinzugefügt.");
			break;
		case "removecondition":
			//re [region] rp [permission]
	    	if(args.length < 3){
	    		displayHelp();
	    		return false;
	    	}
    		permission = args[2].trim();
			if(Main.regions.contains(region)){
				regionSection = Main.regions.getConfigurationSection(region);
			}
			else{
				sendMessage("Region "+region+" nicht gefunden. (Existiert nicht oder hat keine Effekte)");
				break;
			}
			if(!regionSection.contains("permissions")){
				permissionList = new ArrayList<String>();
			}
			else permissionList = regionSection.getStringList("permissions");
			if(permissionList.contains(permission))
				permissionList.remove(permission);
			regionSection.set("permissions", permissionList);
			sendMessage("Bedingung "+permission+" von der Region "+region+" entfernt.");
			break;
		default:
    		displayHelp();
		}
    	return true;
    }
    public void displayHelp(){
		sendMessage("RegionEffects Version "+Main.pdfFile.getVersion()+" Befehle:");
		sendMessage("/RegionEffects = /re");
		sendMessage("-----");
		sendMessage("/re help");
		sendMessage("/re [Region] info (Effekt)");
		sendMessage("/re [Region] [Aktion*] [Effekt] [Dauer=10] [Verstärker=0] [Umgebung=Nein] [Partikel=Ja] [Farbe=Standard]");
		sendMessage("/re [Region] remove [Effekt]");
		sendMessage("/re [Region] clear");
		sendMessage("/re [Region] addcondition [permission]");
		sendMessage("/re [Region] removecondition [permission]");
		sendMessage("*Aktionen: add, addspecific, addregionOnly, addspecificregiononly");
		sendMessage("/re help Aktionen");
    }
    private void sendMessage(String message){
		if(consoleMode)
			Main.logger.info(message);
		else
			p.sendMessage(message);
    }
}
