/*
 * Sicade - Systèmes intégrés de connaissances
 *          pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.resources.seagis;

// J2SE dependencoes
import java.util.Locale;
import java.util.MissingResourceException;

// OpenGIS dependencies
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.ResourceBundle;
import org.geotools.util.ResourceInternationalString;


/**
 * Base class for local-dependent resources. Instances of this class should
 * never been created directly. Use the factory method {@link #getResources}
 * or use static methods instead.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Resources extends ResourceBundle {
    /**
     * Construct a resource bundle using english language.
     * This is the default when no resource are available
     * in user language.
     */
    public Resources() {
        super(Resources_fr.FILEPATH);
        
//      super(// Set 'true' in front of language to use as default.
//            false ? Resources_fr.FILEPATH :
//             true ? Resources_en.FILEPATH :
//             null);
    }

    /**
     * Construct a resource bundle
     * using the specified UTF8 file.
     */
    Resources(final String filepath) {
        super(filepath);
    }

    /**
     * Returns the name of the logger to use.
     */
    protected String getLoggerName() {
        return "net.sicade";
    }

    /**
     * Returns resources in the given locale.
     *
     * @param  locale The locale, or {@code null} for the default locale.
     * @return Resources in the given locale.
     * @throws MissingResourceException if resources can't be found.
     */
    public static Resources getResources(Locale locale) throws MissingResourceException {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return (Resources) getBundle(Resources.class.getName(), locale);
        /*
         * We rely on cache capability of {@link java.util.ResourceBundle}.
         */
    }

    /**
     * Gets an international string for the given key. This method do not check for the key
     * validity. If the key is invalid, then a {@link MissingResourceException} may be thrown
     * when a {@link InternationalString#toString} method is invoked.
     *
     * @param  key The key for the desired string.
     * @return An international string for the given key.
     */
    public static InternationalString formatInternational(final int key) {
        return new ResourceInternationalString(Resources.class.getName(), String.valueOf(key));
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     *
     * @param  key The key for the desired string.
     * @return The string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final int key) throws MissingResourceException {
        return getResources(null).getString(key);
    }

    /**
     * Gets a string for the given key are replace all occurence of "{0}"
     * with values of {@code arg0}.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final int key, final Object arg0) throws MissingResourceException {
        return getResources(null).getString(key, arg0);
    }

    /**
     * Gets a string for the given key are replace all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final int key, final Object arg0, final Object arg1) throws MissingResourceException {
        return getResources(null).getString(key, arg0, arg1);
    }

    /**
     * Gets a string for the given key are replace all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final int key, final Object arg0, final Object arg1, final Object arg2) throws MissingResourceException {
        return getResources(null).getString(key, arg0, arg1, arg2);
    }

    /**
     * Gets a string for the given key are replace all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @param  arg3 Value to substitute to "{3}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final int key, final Object arg0, final Object arg1, final Object arg2, final Object arg3) throws MissingResourceException {
        return getResources(null).getString(key, arg0, arg1, arg2, arg3);
    }

    /**
     * Gets a string for the given key are replace all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @param  arg3 Value to substitute to "{3}".
     * @param  arg4 Value to substitute to "{3}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final int key, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws MissingResourceException {
        return getResources(null).getString(key, arg0, arg1, arg2, arg3, arg4);
    }
}
