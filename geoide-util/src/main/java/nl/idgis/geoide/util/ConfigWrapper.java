package nl.idgis.geoide.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

public class ConfigWrapper {

	private final Config config;
	
	public ConfigWrapper (final Config config) {
		if (config == null) {
			throw new NullPointerException ("config cannot be null");
		}
		
		this.config = config;
	}
	
	public int getInt (final String path, final int defaultValue) {
		try {
			return config.getInt (path);
		} catch (ConfigException.Missing | ConfigException.WrongType e) {
			return defaultValue;
		}
	}
	
	public long getLong (final String path, final long defaultValue) {
		try {
			return config.getLong (path);
		} catch (ConfigException.Missing | ConfigException.WrongType e) {
			return defaultValue;
		}
	}
	
    public boolean getBoolean (final String path, final boolean defaultValue) {
		try {
			return config.getBoolean (path);
		} catch (ConfigException.Missing | ConfigException.WrongType e) {
			return defaultValue;
		}
    }
	
    public double getDouble (final String path, final double defaultValue) {
		try {
			return config.getDouble (path);
		} catch (ConfigException.Missing | ConfigException.WrongType e) {
			return defaultValue;
		}
    }
    
    public String getString (final String path, final String defaultValue) {
		try {
			return config.getString (path);
		} catch (ConfigException.Missing | ConfigException.WrongType e) {
			return defaultValue;
		}
    }
    
    public ConfigWrapper getConfig (final String path) {
    	return new ConfigWrapper (config.getConfig (path));
    }
}
