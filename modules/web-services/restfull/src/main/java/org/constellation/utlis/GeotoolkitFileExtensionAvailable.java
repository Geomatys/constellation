package org.constellation.utlis;

import org.apache.sis.util.Static;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * utility class to know files extensions availables on Geotoolkit.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public final class GeotoolkitFileExtensionAvailable extends Static {


    /**
     * Give a extension list managed by geotoolkit
     * @return an {@link String} {@link List} which contain file extension.
     */
    public static List<String> getAvailableFileExtension() {
        final List<String> extensions = new ArrayList<>(0);

        //get coverage file extension and add on list
        final String[] coverageExtension = ImageIO.getReaderFileSuffixes();
        Collections.addAll(extensions, coverageExtension);

        //access to features file factories
        final Iterator<FileFeatureStoreFactory> ite = FeatureStoreFinder.getAllFactories(FileFeatureStoreFactory.class).iterator();
        while (ite.hasNext()) {

            final FileFeatureStoreFactory factory = ite.next();
            //display general informations about this factory
            String[] tempExtensions = factory.getFileExtensions();
            for (int i = 0; i < tempExtensions.length; i++) {
                String extension = tempExtensions[i];

                //remove point before extension
                extensions.add(extension.replace(".", ""));
            }
        }
        return extensions;
    }

}
