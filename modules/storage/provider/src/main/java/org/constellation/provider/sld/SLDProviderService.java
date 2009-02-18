/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider.sld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.provider.StyleProviderService;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDProviderService implements StyleProviderService{

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SLDProviderService.class.getName());
    private static final String NAME = "sld";

    private static final Collection<SLDProvider> PROVIDERS = new ArrayList<SLDProvider>();
    private static final Collection<SLDProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

    @Override
    public Collection<SLDProvider> getProviders() {
        return IMMUTABLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(File file) {
        if(file == null){
            throw new NullPointerException("Configuration file can not be null");
        }

        if(CONFIG_FILE != null){
            throw new IllegalStateException("The SLD provider service has already been initialize");
        }

        SLDProviderService.CONFIG_FILE = file;

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(file));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "[PROVIDER]> SLD config file is unvalid : "+ file.getPath());
        }

        String strFolders = props.getProperty("paths");

        if (strFolders == null) {
            strFolders = "";
        }

        final StringTokenizer token = new StringTokenizer(strFolders,";");
        while(token.hasMoreElements()){
            final String path = token.nextToken();
            final File f = new File(path);
            if(f.exists() && f.isDirectory()){
                PROVIDERS.add(new SLDProvider(f));
                LOGGER.log(Level.INFO, "[PROVIDER]> SLD provider created : " + path);
            }else{
                LOGGER.log(Level.WARNING, "[PROVIDER]> SLD folder provided does not exist : "+ path);
            }
        }

    }

}
