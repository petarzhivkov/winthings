package org.openhab.binding.withings.internal.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.binding.withings.internal.helper.StringIoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WithingsPersist {
	
	private static final Logger logger = LoggerFactory.getLogger(WithingsPersist.class);
	
	public static final String SERVICE_NAME = "withings";

    public static final String CONFIG_DIR = "." + File.separator + "configurations";

    public static final String CONTENT_DIR = CONFIG_DIR + File.separator + "services";
	
	public static WithingsPersist getNewWithingsPersist(){
		return new WithingsPersist();
	}
	
    public void persist(String accountId, String userId, String proxyUrl) {
        File file = null;
        String prefix = "";

        try {
            // store properties either to openhab.cfg (legacy) or
            // services/withings.cfg
            if (isLegacyConfiguration()) {
                file = new File(CONFIG_DIR + File.separator + "openhab.cfg");
                // in legacy case each property has to be prefixed with
                prefix = SERVICE_NAME + ":";
            } else {
                file = new File(CONTENT_DIR + File.separator + SERVICE_NAME + ".cfg");
            }

            if (!file.exists()) {
                logger.warn("Configuration file '{}' was not found. Creating it now...", file.getAbsolutePath());
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // if an account different from the default account is used
            // it gets prefixed separated by "."
            if (!WithingsAuthenticator.DEFAULT_ACCOUNT_ID.equals(accountId)) {
                prefix += accountId + ".";
            }

            Map<String, String> config = load(file);

            config.put(prefix + "userid", userId);
            
            config.put(prefix + "proxy", proxyUrl);

            store(config, file);

            logger.warn("Saved Withings account to file '{}'.", file.getAbsolutePath());
        } catch (IOException ioe) {
            logger.warn("Couldn't write Withings account to file '{}'.", file.getAbsolutePath());
        }
    }
	
    /**
     * Checks whether the Binding configuration has been stored to
     * openhab.cfg rather than services/withings.cfg.
     * 
     * @return true, if there is a configuration key like "withings-oauth"
     *         in openhab.cfg and false in all other cases.
     */
	public boolean isLegacyConfiguration() {
        File file = new File( CONFIG_DIR + File.separator + "openhab.cfg");

        try {
            if (file.exists()) {
                Map<String, String> config = load(file);
                for (Object key : config.keySet()) {
                    if (key.toString().startsWith(SERVICE_NAME)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Couldn't open Configuration File '{}'", file.getAbsolutePath());
        }

        return false;
    }

	public Map<String, String> load(File file) throws IOException {
        Map<String, String> config = new LinkedHashMap<String, String>();
        FileInputStream is = new FileInputStream(file);
		List<String> lines = StringIoUtils.readLines(is, Charset.defaultCharset());
        for (String line : lines) {
            String[] parameterPair = line.split("=");
            if (parameterPair.length == 2) {
                config.put(parameterPair[0].trim(), parameterPair[1].trim());
            } else {
                config.put(parameterPair[0], "");
            }
        }
        if(is != null) is.close();
        return config;
    }
    
	private Map<String, String> store(Map<String, String> config, File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file);

        List<String> lines = new ArrayList<String>();
        for (Entry<String, String> line : config.entrySet()) {
            String value = StringIoUtils.isBlank(line.getValue()) ? "" : "=" + line.getValue();
            lines.add(line.getKey() + value);
        }

        StringIoUtils.writeLines(lines, System.getProperty("line.separator"), os, Charset.defaultCharset());
        if(os != null) os.close();
        return config;
    }
}
