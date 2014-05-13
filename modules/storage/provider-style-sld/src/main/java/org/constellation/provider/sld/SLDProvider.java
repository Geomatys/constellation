/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.provider.sld;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;

import org.constellation.provider.AbstractStyleProvider;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.sld.MutableLayer;
import org.geotoolkit.sld.MutableLayerStyle;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.MutableUserLayer;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableStyle;
import org.apache.sis.util.collection.Cache;
import org.geotoolkit.style.MutableStyleFactory;
import org.apache.sis.util.logging.Logging;

import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.FactoryException;

import static org.constellation.provider.sld.SLDProviderFactory.*;

/**
 * Style provider. index and cache MutableStyle within the given folder.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDProvider extends AbstractStyleProvider{

    private static final MutableStyleFactory SF = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    private static final Collection<String> MASKS = new ArrayList<>();

    static{
        MASKS.add(".xml");
        MASKS.add(".sld");
    }

    private final StyleXmlIO sldParser = new StyleXmlIO();
    private File folder;
    private final Map<String,File> index = new ConcurrentHashMap<>();
    private final Cache<String,MutableStyle> cache = new Cache<>(20, 20, true);


    protected SLDProvider(String providerId,final SLDProviderFactory service, final ParameterValueGroup source){
        super(providerId,service,source);
        reload();
    }

    File getFolder() {
        return folder;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MutableStyle get(final String key) {

        MutableStyle value = cache.peek(key);
        if (value == null) {
            final Cache.Handler<MutableStyle> handler = cache.lock(key);
            try {
                value = handler.peek();
                if (value == null) {
                    final File f = index.get(key);
                    if(f != null){
                        final String baseErrorMsg = "[PROVIDER]> SLD Style ";
                        //try SLD 1.1
                        try {
                            final MutableStyledLayerDescriptor sld = sldParser.readSLD(f, StyledLayerDescriptor.V_1_1_0);
                            value = getFirstStyle(sld);
                            if(value != null){
                                value.setName(key);
                                LOGGER.log(Level.FINE, "{0}{1} is an SLD 1.1.0", new Object[]{baseErrorMsg, key});
                                return value;
                            }
                        } catch (JAXBException ex) { /* dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try SLD 1.0
                        try {
                            final MutableStyledLayerDescriptor sld = sldParser.readSLD(f, StyledLayerDescriptor.V_1_0_0);
                            value = getFirstStyle(sld);
                            if(value != null){
                                value.setName(key);
                                LOGGER.log(Level.FINE, "{0}{1} is an SLD 1.0.0", new Object[]{baseErrorMsg, key});
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try UserStyle SLD 1.1
                        try {
                            value = sldParser.readStyle(f, SymbologyEncoding.V_1_1_0);
                            if(value != null){
                                value.setName(key);
                                LOGGER.log(Level.FINE, "{0}{1} is a UserStyle SLD 1.1.0", new Object[]{baseErrorMsg, key});
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try UserStyle SLD 1.0
                        try {
                            value = sldParser.readStyle(f, SymbologyEncoding.SLD_1_0_0);
                            if(value != null){
                                value.setName(key);
                                LOGGER.log(Level.FINE, "{0}{1} is a UserStyle SLD 1.0.0", new Object[]{baseErrorMsg, key});
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try FeatureTypeStyle SE 1.1
                        try {
                            final MutableFeatureTypeStyle fts = sldParser.readFeatureTypeStyle(f, SymbologyEncoding.V_1_1_0);
                            value = SF.style();
                            value.featureTypeStyles().add(fts);
                            if(value != null){
                                value.setName(key);
                                LOGGER.log(Level.FINE, "{0}{1} is FeatureTypeStyle SE 1.1", new Object[]{baseErrorMsg, key});
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try FeatureTypeStyle SLD 1.0
                        try {
                            final MutableFeatureTypeStyle fts = sldParser.readFeatureTypeStyle(f, SymbologyEncoding.SLD_1_0_0);
                            value = SF.style();
                            value.featureTypeStyles().add(fts);
                            if(value != null){
                                value.setName(key);
                                LOGGER.log(Level.FINE, "{0}{1} is an FeatureTypeStyle SLD 1.0", new Object[]{baseErrorMsg, key});
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        LOGGER.log(Level.WARNING, "{0}{1} could not be parsed", new Object[]{baseErrorMsg, key});
                    }
                }
            } finally {
                handler.putAndUnlock(value);
            }
        }

        return value;
    }

    @Override
    public synchronized void set(final String key, final MutableStyle style){

        File f = index.get(key);
        if(f == null){
            //file doesnt exist, create it
            f = new File(folder, key+ ".xml");
        }

        final StyleXmlIO util = new StyleXmlIO();
        try {
            util.writeStyle(f, style, StyledLayerDescriptor.V_1_1_0);
            index.put(key, f);
            cache.clear();
            fireUpdateEvent();
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

    }

    @Override
    public synchronized void rename(final String key, final String newName){
        final File f = index.get(key);
        if(index.containsKey(newName)){
            throw new IllegalArgumentException("Style name "+ newName +" already used.");
        }
        if(f == null){
            throw new IllegalArgumentException("Style "+ newName +" do not exist.");
        }

        final File newFile = new File(f.getParentFile(), newName+".xml");
        f.renameTo(newFile);
        reload();
    }

    @Override
    public synchronized void remove(final String key){
        final File f = index.get(key);
        if(f != null){
            f.delete();
            reload();
            fireUpdateEvent();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            index.clear();
            cache.clear();

            final ParameterValueGroup srcConfig = getSource().groups(
                    getFactory().getStoreDescriptor().getName().getCode()).get(0);

            final ParameterValue param = srcConfig.parameter(FOLDER_DESCRIPTOR.getName().getCode());

            if(param == null || param.getValue() == null){
                getLogger().log(Level.WARNING,"Provided File path is not defined.");
                return;
            }

            folder = new File(param.stringValue());

            if(folder == null || !folder.exists() || !folder.isDirectory()){
                getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
                return;
            }

            visit(folder);
        }
        fireUpdateEvent();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
            cache.clear();
        }
    }

    private void visit(final File file) {

        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        }else{
            test(file);
        }
    }

    private void test(final File candidate){
        if(candidate.isFile()){
            final String fullName = candidate.getName();
            final String lowerCase = fullName.toLowerCase();

            if(lowerCase.startsWith(".")) return;

            for(final String mask : MASKS){
                if(lowerCase.endsWith(mask)){
                    final String name = fullName.substring(0, fullName.length()-4);
                    index.put(name, candidate);
                }
            }
        }
    }

    private static MutableStyle getFirstStyle(final MutableStyledLayerDescriptor sld){
        if(sld == null) return null;
        for(final MutableLayer layer : sld.layers()){
            if(layer instanceof MutableNamedLayer){
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;
                for(final MutableLayerStyle stl : mnl.styles()){
                    if(stl instanceof MutableStyle){
                        return (MutableStyle) stl;
                    }
                }
            }else if(layer instanceof MutableUserLayer){
                final MutableUserLayer mnl = (MutableUserLayer) layer;
                for(final MutableStyle stl : mnl.styles()){
                    return stl;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSensorAffectable() {
        return false;
    }
}
