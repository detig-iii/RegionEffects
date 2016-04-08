package ch.swisssmp.regioneffects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Color;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import eu.sylian.extraevents.Area;
import eu.sylian.extraevents.PlayerEnterAreaEvent;
import eu.sylian.extraevents.PlayerLeaveAreaEvent;
 
public class Main extends JavaPlugin implements Listener{
	public static Logger logger;
	public static Server server;
	public static PluginDescriptionFile pdfFile;
	public static YamlConfiguration regions;
	public static File regionsFile;
	public static YamlConfiguration config;
	public static File configFile;
	public static WorldGuardPlugin worldguard;
	public static RegionContainer container;
	
	public void onEnable() {
		pdfFile = getDescription();
		logger = Logger.getLogger("Minecraft");
		logger.info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");

		worldguard = getWorldGuard();
		container = Main.worldguard.getRegionContainer();
		
		server = getServer();
		
		server.getPluginManager().registerEvents(this, this);
		
		this.getCommand("RegionEffects").setExecutor(new RegionEffectPlayerCommand());
		
		configFile = new File(getDataFolder(), "config.yml");
		regionsFile = new File(getDataFolder(), "regions.yml");
		try {
	        firstRun();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		config = new YamlConfiguration();
		regions = new YamlConfiguration();
		loadYamls();
	}
	public void onDisable() {
		saveYamls();
		PluginDescriptionFile pdfFile = getDescription();
		logger.info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}

	private WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        logger.info("WorldGuard benötigt!");
	    }
	
	    return (WorldGuardPlugin) plugin;
	}
    @EventHandler
    public void enterEvent(PlayerEnterAreaEvent event){
    	Area area = (Area)event.getArea();
    	Player player = event.getPlayer();
    	if(regions.contains(area.getName())){
    		ConfigurationSection region = regions.getConfigurationSection(area.getName());
    		Set<String> effects = region.getKeys(false);
    		String[] effectArray = new String[effects.size()];
    		effects.toArray(effectArray);
    		for(int i = 0; i < effectArray.length;i++){
    			PotionEffect potionEffect;
    			ConfigurationSection potion = region.getConfigurationSection(effectArray[i]);
    			String _type = potion.getString("type");
    			PotionEffectType type = PotionEffectType.getByName(_type);
    			int duration = potion.getInt("duration");
    			int amplifier = potion.getInt("amplifier");
    			boolean ambient = potion.getBoolean("ambient");
    			boolean particles = potion.getBoolean("particles");
    			String _color = potion.getString("color");
				Color color = parseColor(_color);
    			boolean specific = potion.getBoolean("specific");
    			if(_color=="default" || color==null)
    				potionEffect = new PotionEffect(type, duration*20, amplifier, ambient, particles);
    			else{
    				potionEffect = new PotionEffect(type, duration*20, amplifier, ambient, particles, color);
    			}
    			if (specific){
    				for (PotionEffect effect : player.getActivePotionEffects())
        		        player.removePotionEffect(effect.getType());
        			player.addPotionEffect(potionEffect);
        			return;
    			}
    			player.addPotionEffect(potionEffect);
    		}
    	}
    }
    @EventHandler
    public void leaveEvent(PlayerLeaveAreaEvent event){
    	Area area = (Area)event.getArea();
    	Player player = event.getPlayer();
    	if(regions.contains(area.getName())){
    		ConfigurationSection region = regions.getConfigurationSection(area.getName());
    		Set<String> effects = region.getKeys(false);
    		String[] effectArray = new String[effects.size()];
    		effects.toArray(effectArray);
    		for(int i = 0; i < effectArray.length;i++){
    			ConfigurationSection potion = region.getConfigurationSection(effectArray[i]);
    			if(potion.getBoolean("regionOnly")==true){
        			String _type = potion.getString("type");
        			PotionEffectType type = PotionEffectType.getByName(_type);
        			if(player.hasPotionEffect(type))
        				player.removePotionEffect(type);
    			}
    		}
    	}
    }
    
    private Color parseColor(String colordata){
    	Color result = null;
    	if(colordata.startsWith("#")){
    		
    	}
    	return result;
    }
    
    private void firstRun() throws Exception {
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), configFile);
        }
        if(!regionsFile.exists()){
        	regionsFile.getParentFile().mkdirs();
            copy(getResource("groups.yml"), regionsFile);
        }
    }
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveYamls() {
        try {
            config.save(configFile);
            regions.save(regionsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadYamls() {
        try {
            config.load(configFile);
            regions.load(regionsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


