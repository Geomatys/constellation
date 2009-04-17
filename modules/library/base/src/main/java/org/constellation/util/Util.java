/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.util;

import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Utility methods of general use.
 * <p>
 * TODO: this class needs review.
 *   * methods should be re-ordered for coherence
 *       -- String
 *       -- Reflection
 *       -- ...
 * </p>
 * 
 * @author Mehdi Sidhoum (Geomatys)
 * @author Legal Guilhem (Geomatys)
 * @author Adrian Custer (Geomatys)
 * 
 * @since 0.2
 */
public final class Util {
	
    private static Logger logger = Logger.getLogger("org.constellation.util");
    
    
    /**
     * Returns true if one of the {@code String} elements in a {@code List} 
     * matches the given {@code String}, insensitive to case.
     * 
     * @param list A {@code List<String>} with elements to be tested. 
     * @param str  The {@code String} to evaluate.
     * 
     * @return {@code true}, if at least one element of the list matches the 
     *           parameter, {@code false} otherwise.
     */
    public static boolean matchesStringfromList(final List<String> list,final String str) {
        boolean str_available = false;
        for (String s : list) {
            Pattern pattern = Pattern.compile(str,Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                str_available = true;
            }
        }
        return str_available;
    }
    
    /**
     * Generate a {@code Date} object from a {@code String} which represents an 
     * instant encoded in the ISO-8601 format, e.g. {@code 2009.01.20T17:04Z}.
     * <p>
     * The {@code String} can be defined in the pattern 
     *   yyyy-MM-dd'T'HH:mm:ss.SSSZ 
     * or 
     *   yyyy-MM-dd.
     * </p>
     * @param dateString An instant encoded in the ISO-8601 format.
     * @return A {@code java.util.Date} object representing the same instant.
     * @see TimeParser
     */
    public static Date getDateFromString(String dateString) throws ParseException {
        final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
        final String DATE_FORMAT2 = "yyyy-MM-dd";
        final String DATE_FORMAT3 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        final SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        final SimpleDateFormat sdf2 = new java.text.SimpleDateFormat(DATE_FORMAT2);
        final SimpleDateFormat sdf3 = new java.text.SimpleDateFormat(DATE_FORMAT3);

        if (dateString.contains("T")) {
            String timezoneStr;
            int index = dateString.lastIndexOf("+");
            if (index == -1) {
                index = dateString.lastIndexOf("-");
            }
            if (index > dateString.indexOf("T")) {
                timezoneStr = dateString.substring(index + 1);

                if (timezoneStr.contains(":")) {
                    //e.g : 1985-04-12T10:15:30+04:00
                    timezoneStr = timezoneStr.replace(":", "");
                    dateString = dateString.substring(0, index + 1).concat(timezoneStr);
                } else if (timezoneStr.length() == 2) {
                    //e.g : 1985-04-12T10:15:30-04
                    dateString = dateString.concat("00");
                }
            } else if (dateString.endsWith("Z")) {
                //e.g : 1985-04-12T10:15:30Z
                dateString = dateString.substring(0, dateString.length() - 1).concat("+0000");
            }else {
                //e.g : 1985-04-12T10:15:30
                dateString = dateString + "+0000";
            }
            final String timezone = getTimeZone(dateString);
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));

            if (dateString.contains(".")) {
                return sdf3.parse(dateString);
            }
            return sdf.parse(dateString);
        }
        if (dateString.contains("-")) {
            return sdf2.parse(dateString);
        }
        return null;
    }
    
    /**
     * Extract a description of the offset from GMT for an instant encoded in 
     * ISO-8601 format (e.g. an input of {@code 2009-01-20T12:04:00Z-05} would
     * yield the {@code String} "GMT-05").
     * 
     * @param dateString An instant encoded in ISO-8601 format, must not be 
     *                     {@code null}.
     * @return The offset from GMT for the parameter.
     */
    public static String getTimeZone(final String dateString) {
    	
        if (dateString.endsWith("Z")) {
            return "GMT+" + 0;
        }
        int index = dateString.lastIndexOf("+");
        if (index == -1) {
            index = dateString.lastIndexOf("-");
        }
        if (index > dateString.indexOf("T")) {
            return "GMT" + dateString.substring(index);
        }
        return TimeZone.getDefault().getID();
    }
    
    /**
     * Create a {@link Date} object from a date represented in a {@String} 
     * object and a formatting rule described in a {@link DateFormat} object.
     * <p>
     * The input date parameter can have any of several different formats.
     * </p>
     * <p>
     * TODO: Explain these formats.
     * TODO: This is out of date since we no longer use the {@code DateFormat}
     * objects to format the {@code Date}.
     * </p>
     * 
     * @param date A date representation, e.g. 2002, 02-2007, 2004-03-04, which 
     *               may be null.
     * @return A formated date (example 2002 -> 01-01-2002,  2004-03-04 -> 04-03-2004, ...) 
     */
    public static Date createDate(String date, final DateFormat dateFormat) throws ParseException {
        
        Map<String, String> POOL = new HashMap<String, String>();
        POOL.put("janvier",   "01");
        POOL.put("février",   "02");
        POOL.put("mars",      "03");
        POOL.put("avril",     "04");
        POOL.put("mai",       "05");
        POOL.put("juin",      "06");
        POOL.put("juillet",   "07");
        POOL.put("août",      "08");
        POOL.put("septembre", "09");
        POOL.put("octobre",   "10");
        POOL.put("novembre",  "11");
        POOL.put("décembre",  "12");

        Map<String, String> POOLcase = new HashMap<String, String>();
        POOLcase.put("Janvier",   "01");
        POOLcase.put("Février",   "02");
        POOLcase.put("Mars",      "03");
        POOLcase.put("Avril",     "04");
        POOLcase.put("Mai",       "05");
        POOLcase.put("Juin",      "06");
        POOLcase.put("Juillet",   "07");
        POOLcase.put("Août",      "08");
        POOLcase.put("Septembre", "09");
        POOLcase.put("Octobre",   "10");
        POOLcase.put("Novembre",  "11");
        POOLcase.put("Décembre",  "12");

        String year;
        String month;
        String day;
        Date tmp = getDateFromString("1900" + "-" + "01" + "-" + "01");
        if (date != null) {
            if (date.contains("/")) {
                if (getOccurenceFrequency(date, "/") == 2) {
                    day = date.substring(0, date.indexOf("/"));
                    date = date.substring(date.indexOf("/") + 1);
                    month = date.substring(0, date.indexOf("/"));
                    year = date.substring(date.indexOf("/") + 1);

                    tmp = getDateFromString(year + "-" + month + "-" + day);
                } else {
                    if (getOccurenceFrequency(date, "/") == 1) {
                        month = date.substring(0, date.indexOf("/"));
                        year = date.substring(date.indexOf("/") + 1);
                        tmp = getDateFromString(year + "-" + month + "-" + "01");
                    }
                }
            } else if (getOccurenceFrequency(date, " ") == 2) {
                if (!date.contains("?")) {

                    day = date.substring(0, date.indexOf(" "));
                    date = date.substring(date.indexOf(" ") + 1);
                    month = POOL.get(date.substring(0, date.indexOf(" ")));
                    year = date.substring(date.indexOf(" ") + 1);

                    tmp = getDateFromString(year + "-" + month + "-" + day);
                } else {
                    tmp = getDateFromString("2000" + "-" + "01" + "-" + "01");
                }
            } else if (getOccurenceFrequency(date, " ") == 1) {
                try {
                    Date d;
                    synchronized(dateFormat) {
                        d = dateFormat.parse(date);
                    }
                    return new Date(d.getTime());
                } catch (ParseException ex) {
                }
                month = POOLcase.get(date.substring(0, date.indexOf(" ")));
                year = date.substring(date.indexOf(" ") + 1);
                tmp = getDateFromString(year + "-" + month + "-" + "01");


            } else if (getOccurenceFrequency(date, "-") == 1) {

                month = date.substring(0, date.indexOf("-"));
                year = date.substring(date.indexOf("-") + 1);

                tmp = getDateFromString(year + "-" + month + "-" + "01");

            } else if (getOccurenceFrequency(date, "-") == 2) {

                //if date is in format yyyy-mm-dd
                if (date.substring(0, date.indexOf("-")).length() == 4) {
                    year = date.substring(0, date.indexOf("-"));
                    date = date.substring(date.indexOf("-") + 1); //mm-ddZ
                    month = date.substring(0, date.indexOf("-"));
                    date = date.substring(date.indexOf("-") + 1); // ddZ
                    if (date.contains("Z")) {
                        date = date.substring(0, date.indexOf("Z"));
                    }
                    day = date;

                    tmp = getDateFromString(year + "-" + month + "-" + day);
                } else {
                    day = date.substring(0, date.indexOf("-"));
                    date = date.substring(date.indexOf("-") + 1);
                    month = date.substring(0, date.indexOf("-"));
                    year = date.substring(date.indexOf("-") + 1);

                    tmp = getDateFromString(year + "-" + month + "-" + day);
                }

            } else {
                year = date;
                tmp = getDateFromString(year + "-" + "01" + "-" + "01");
            }
        }
        return tmp;
    }
    
        
    /**
     * Counts the number of occurences of the second parameter within the first.
     * For example, {@code getOccurenceFrequency("Constellation","ll") yields 
     * {@code 1}.
     * 
     * @param s   The {@code String} against which matches should be made, must 
     *              not be null.
     * @param occ The {@code String} whose frequency of occurrence in the first 
     *              parameter should be counted, must not be null.
     * @return The frequency of occurrences of the second parameter characters 
     *           in the character sequence of the first.
     */
    public static int getOccurenceFrequency (String s, final String occ){
        if (! s.contains(occ))
            return 0;
        else {
            int nbocc = 0;
            while(s.indexOf(occ) != -1){
                s = s.substring(s.indexOf(occ)+1);
                nbocc++;
            }
            return nbocc;
        }
    }
    
    /**
     * Searches in the Context ClassLoader for the named packages and returns a 
     * {@code List<String>} with, for each named package, 
     *                                  ...TODO, read .scan(.)
     * <p>
     * TODO: verify this.
     * </p>
     * 
     * @param packages The names of the packages to scan in Java format, i.e. 
     *                   using the "." separator, may be null.
     * 
     * @return A list of package names.
     */
    public static List<String> searchSubPackage(final String... packages) {
        List<String> result = new ArrayList<String>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        for (String p : packages) {
            try {
                String fileP = p.replace('.', '/');
                Enumeration<URL> urls = classloader.getResources(fileP);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try {
                        URI uri = url.toURI();
                        result.addAll(scan(uri, fileP));
                    } catch (URISyntaxException e) {
                        logger.severe("URL, " + url + "cannot be converted to a URI");
                    }
                }
            } catch (IOException ex) {
                logger.severe("The resources for the package" + p + ", could not be obtained");
            }
        }

        return result;
    }

    /**
     * Scan a resource file (a JAR or a directory) to find the sub-package names of
     * the specified "filePackageName"
     * 
     * @param u The URI of the file.
     * @param filePackageName The package to scan.
     * 
     * @return a list of package names.
     * @throws java.io.IOException
     */
    public static List<String> scan(final URI u, final String filePackageName) throws IOException {
        List<String> result = new ArrayList<String>();
        String scheme = u.getScheme();
        if (scheme.equals("file")) {
            File f = new File(u.getPath());
            if (f.isDirectory()) {
                result.addAll(scanDirectory(f, filePackageName));
            }
        } else if (scheme.equals("jar") || scheme.equals("zip")) {
            try {
                URI jarUri = URI.create(u.getSchemeSpecificPart());
                String jarFile = jarUri.getPath();
                jarFile = jarFile.substring(0, jarFile.indexOf('!'));
                result.addAll(scanJar(new File(jarFile), filePackageName));
              
            } catch (IllegalArgumentException ex) {
                logger.warning("unable to scan jar file: " +u.getSchemeSpecificPart());
            }
        }
        return result; 
    }

    /**
     * Scan a directory to find the sub-package names of
     * the specified "parent" package
     * 
     * @param root The root file (directory) of the package to scan.
     * @param parent the package name.
     * 
     * @return a list of package names.
     */
    public static List<String> scanDirectory(final File root, final String parent) {
        List<String> result = new ArrayList<String>();
        for (File child : root.listFiles()) {
            if (child.isDirectory()) {
                result.add(parent.replace('/', '.') + '.' + child.getName());
                result.addAll(scanDirectory(child, parent));
            }
        }
        return result;
    }

    /**
     * Scan a jar to find the sub-package names of
     * the specified "parent" package
     * 
     * @param file the jar file containing the package to scan
     * @param parent the package name.
     * 
     * @return a list of package names.
     * @throws java.io.IOException
     */
    public static List<String> scanJar(final File file, final String parent) throws IOException {
        List<String> result = new ArrayList<String>();
        final JarFile jar = new JarFile(file);
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            if (e.isDirectory() && e.getName().startsWith(parent)) {
                String s = e.getName().replace('/', '.');
                s = s.substring(0, s.length() - 1);
                result.add(s);
            }
        }
        return result;
    }
    
    /**
     * Encode the specified string with MD5 algorithm.
     * 
     * @param key :  the string to encode.
     * @return the value (string) hexadecimal on 32 bits
     */
    public static String MD5encode(final String key) {

        byte[] uniqueKey = key.getBytes();
        byte[] hash = null;
        try {
            // we get an object allowing to crypt the string
            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);

        } catch (NoSuchAlgorithmException e) {
            throw new Error("no MD5 support in this VM");
        }
        StringBuffer hashString = new StringBuffer();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        return hashString.toString();
    }

    /**
     * Transform a {@code List<Class<?>>} to an array of {@code Class<?>}.
     * 
     * <p>
     * TODO: Parameterizing this list needs to fix other code as well.
     * </p>
     * 
     * @param classeList A {@code List<Class<?>>}, must not be null.
     * @return An array of {@code Class<?>}.
     */
    public static Class<?>[] toArray(final List<Class> classeList) {
        Class<?>[] result = new Class<?>[classeList.size()];
        int i = 0;
        for (Class<?> classe : classeList) {
            result[i] = classe;
            i++;
        }
        return result;
    }
    
    /**
     * Delete a directory and all its contents, both files and directories.
     * <p>
     * Note, if this is passed a file rather than a directory, the method will 
     * not delete anything and return {@code false}.
     * </p>
     * 
     * @param directory A {@code File} object, expected to reference a 
     *                    directory.
     * @return {@code true} if, and only if, the directory was successfully 
     *           deleted, and {@code false} otherwise.
     */
    public static boolean deleteDirectory(final File directory) {
        if (directory == null)
            return false;
        if (!directory.exists())
            return false;
        
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    deleteDirectory(directory);
                } else {
                    f.delete();
                }
            }
        } 
        return directory.delete();
        
    }
    
    /**
     * Clean a list of String by removing all the white space, tabulation and carriage in all the strings.
     * 
     * @param list
     * @return
     */
    public static List<String> cleanStrings(final List<String> list) {
        List<String> result = new ArrayList<String>();
        for (String s : list) {
            //we remove the bad character before the real value
           s = s.replace(" ", "");
           s = s.replace("\t", "");
           s = s.replace("\n", "");
           result.add(s);
        }
        return result;
    }
    
    /**
    * Replace all the <ns**:localPart and </ns**:localPart by <prefix:localPart and </prefix:localPart
    * 
    * @param s
    * @param localPart
    * @return
    */ 
    public static String replacePrefix(final String s, final String localPart, final String prefix) {

        return s.replaceAll("[a-zA-Z0-9]*:" + localPart, prefix + ":" + localPart);
    }
    
    /**
     * Return an marshallable Object from an url
     */
    public static Object getUrlContent(final String URL, final Unmarshaller unmarshaller) throws MalformedURLException, IOException {
        URL source         = new URL(URL);
        URLConnection conec = source.openConnection();
        Object response = null;
        
        try {
        
            // we get the response document
            InputStream in = conec.getInputStream();
            StringWriter out = new StringWriter();
            byte[] buffer = new byte[1024];
            int size;

            while ((size = in.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            //we convert the brut String value into UTF-8 encoding
            String brutString = out.toString();

            //we need to replace % character by "percent because they are reserved char for url encoding
            brutString = brutString.replaceAll("%", "percent");
            String decodedString = java.net.URLDecoder.decode(brutString, "UTF-8");

            
            
            try {
                response = unmarshaller.unmarshal(new StringReader(decodedString));
                if (response != null && response instanceof JAXBElement) {
                    response = ((JAXBElement<?>) response).getValue();
                }
            } catch (JAXBException ex) {
                logger.severe("The distant service does not respond correctly: unable to unmarshall response document." + '\n' +
                        "cause: " + ex.getMessage());
            }
        } catch (IOException ex) {
            logger.severe("The Distant service have made an error");
            return null;
        }
        return response;
    }
    
    /**
     * Call the empty constructor on the specified class and return the result, 
     * handling most errors which could be expected by logging the cause and 
     * returning {@code null}.
     * 
     * @param classe An arbitrary class, expected to be instantiable with a 
     *                 {@code null} argument constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe) {
        try {
            if (classe == null)
                return null;
            
            Constructor<?> constructor = classe.getConstructor();

            //we execute the constructor
            Object result = constructor.newInstance();
            return result;
            
        } catch (InstantiationException ex) {
            logger.severe("The service can not instantiate the class: " + classe.getName() + "()");
        } catch (IllegalAccessException ex) {
            logger.severe("The service can not access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {//TODO: this cannot possibly happen.
            logger.severe("Illegal Argument in empty constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            logger.severe("Invocation Target Exception in empty constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            logger.severe("There is no empty constructor for class: " + classe.getName());
        } catch (SecurityException ex) {
            logger.severe("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Call the constructor which uses a {@code String} parameter for the given 
     * class and return the result, handling most errors which could be expected 
     * by logging the cause and returning {@code null}.
     * 
     * @param classe    An arbitrary class, expected to be instantiable with a 
     *                    single {@code String} argument.
     * @param parameter The {@code String} to use as an argument to the 
     *                    constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe, final String parameter) {
        try {
            if (classe == null)
                return null;
            Constructor<?> constructor = classe.getConstructor(String.class);
            
            //we execute the constructor
            Object result = constructor.newInstance(parameter);
            return result;
            
        } catch (InstantiationException ex) {
            logger.severe("The service can not instantiate the class: " + classe.getName() + "(string)");
        } catch (IllegalAccessException ex) {
            logger.severe("The service can not access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            logger.severe("Illegal Argument in string constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            logger.severe("Invocation target exception in string constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            logger.severe("No single string constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            logger.severe("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Call the constructor which uses two {@code String} parameters for the 
     * given class and return the result, handling most errors which could be 
     * expected by logging the cause and returning {@code null}.
     * 
     * @param classe     An arbitrary class, expected to be instantiable with a 
     *                     single {@code String} argument.
     * @param parameter1 The first {@code String} to use as an argument to the 
     *                     constructor.
     * @param parameter2 The second {@code String} to use as an argument to the 
     *                     constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe, final String parameter1, final String parameter2) {
        try {
            if (classe == null)
                return null;
            Constructor<?> constructor = classe.getConstructor(String.class, String.class);
            
            //we execute the constructor
            Object result = constructor.newInstance(parameter1, parameter2);
            return result;
            
        } catch (InstantiationException ex) {
            logger.severe("The service can't instantiate the class: " + classe.getName() + "(string, string)");
        } catch (IllegalAccessException ex) {
            logger.severe("The service can not access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            logger.severe("Illegal Argument in double string constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            logger.severe("Invocation target exception in double string constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            logger.severe("No double string constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            logger.severe("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Call the constructor which uses a character sequence parameter for the 
     * given class and return the result, handling most errors which could be 
     * expected by logging the cause and returning {@code null}.
     * 
     * @param classe    An arbitrary class, expected to be instantiable with a 
     *                    single {@code String} argument.
     * @param parameter The character sequence to use as an argument to the 
     *                    constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe, final CharSequence parameter) {
        try {
            if (classe == null)
                return null;
            Constructor<?> constructor = classe.getConstructor(CharSequence.class);
            
            //we execute the constructor
            Object result = constructor.newInstance(parameter);
            return result;
            
        } catch (InstantiationException ex) {
            logger.severe("The service can't instantiate the class: " + classe.getName() + "(CharSequence)");
        } catch (IllegalAccessException ex) {
            logger.severe("The service can not access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            logger.severe("Illegal Argument in CharSequence constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            logger.severe("Invocation target exception in CharSequence constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            logger.severe("No such CharSequence constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            logger.severe("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Invoke the given method on the specified object, handling most errors 
     * which could be expected by logging the cause and returning {@code null}.
     * 
     * <p>
     * TODO: what happens if the method returns {@code void}?
     * </p>
     * 
     * @param object     The object on which the method will be invoked.
     * @param method     The method to invoke.
     * @return The {@code Object} generated by the method call, or, if the 
     *           method call resulted in a primitive, the auto-boxing 
     *           equivalent, or null.
     */
    public static Object invokeMethod(final Object object, final Method method) {
        Object result = null;
        String baseMessage = "Unable to invoke the method " + method + ": "; 
        try {
            if (method != null) {
                result = method.invoke(object);
            } else {
                logger.severe(baseMessage + "the method reference is null.");
            }

        } catch (IllegalAccessException ex) {
            logger.severe(baseMessage + "the class is not accessible.");

        } catch (IllegalArgumentException ex) {//TODO: this cannot happen
            logger.severe(baseMessage + "the argument does not match with the method.");

        } catch (InvocationTargetException ex) {
            logger.severe(baseMessage + "an Exception was thrown by the invoked method.");
        }
        return result;
    }
    
    /**
     * Invoke a method with the specified parameter on the specified object, 
     * handling most errors which could be expected by logging the cause and 
     * returning {@code null}.
     * 
     * <p>
     * TODO: what happens if the method returns {@code void}?
     * </p>
     * 
     * @param object     The object on which the method will be invoked.
     * @param method     The method to invoke.
     * @param parameter  The parameter of the method.
     * @return The {@code Object} generated by the method call, or, if the 
     *           method call resulted in a primitive, the auto-boxing 
     *           equivalent, or null.
     */
    public static Object invokeMethod(final Method method, final Object object, final Object parameter) {
        Object result = null;
        String baseMessage = "Unable to invoke the method " + method + ": "; 
        try {
            if (method != null) {
                result = method.invoke(object, parameter);
            } else {
                logger.severe(baseMessage + "the method reference is null.");
            }
        } catch (IllegalAccessException ex) {
            logger.severe(baseMessage + "the class is not accessible.");

        } catch (IllegalArgumentException ex) {
            String param = "null";
            if (parameter != null)
                param = parameter.getClass().getSimpleName();
            logger.severe(baseMessage + "the given argument does not match that required by the method.( argument type was " + param + ")");

        } catch (InvocationTargetException ex) {
            String errorMsg = ex.getMessage();
            if (errorMsg == null && ex.getCause() != null) {
                errorMsg = ex.getCause().getMessage();
            }
            if (errorMsg == null && ex.getTargetException() != null) {
                errorMsg = ex.getTargetException().getMessage();
            }
            logger.severe(baseMessage + "an Exception was thrown in the invoked method:" + errorMsg);
        }
        return result;
    }

    /**
     * Invoke a method with the specified parameter on the specified object,
     * the errors are throws (not like the method invokeMethod(...).
     *
     *
     * @param object     The object on which the method will be invoked.
     * @param method     The method to invoke.
     * @param parameter  The parameter of the method.
     * @return The {@code Object} generated by the method call, or, if the
     *           method call resulted in a primitive, the auto-boxing
     *           equivalent, or null.
     */
    public static Object invokeMethodEx(final Method method, final Object object, final Object parameter) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object result = null;
        if (method != null) {
            result = method.invoke(object, parameter);
        } else {
            logger.severe("Unable to invoke the method reference is null.");
        }
        return result;
    }
    
    //TODO: the methods below can be updated for varArgs.
    
    /**
     * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     * 
     * @param propertyName The attribute name.
     * @param classe       The attribute type.  
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName);

        } catch (IllegalArgumentException ex) {
            logger.severe("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            logger.severe("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            logger.severe("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
        }
        return method;
    }
    
    /**
      * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     * 
     * @param propertyName The attribute name.
     * @param classe       The attribute type.  
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe, final Class<?> parameterClass) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName, parameterClass);

        } catch (IllegalArgumentException ex) {
            logger.severe("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            logger.severe("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            logger.severe("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
        }
        return method;
    }

    /**
     * Return a getter Method for the specified attribute (propertyName) 
     * 
     * @param propertyName The attribute name.
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    public static Method getGetterFromName(String propertyName, final Class<?> rootClass) {


        //special case and corrections
        if (propertyName.equals("beginPosition")) {
            if (rootClass.getName().equals("org.geotools.temporal.object.DefaultInstant"))
                return null;
            else
                propertyName = "beginning";
        } else if (propertyName.equals("endPosition")) {
            if (rootClass.getName().equals("org.geotools.temporal.object.DefaultInstant"))
                return null;
            else
                propertyName = "ending";
        } else if (propertyName.equals("onlineResource")) {
            propertyName = "onLineResource";
        } else if (propertyName.equals("dataSetURI")) {
            propertyName = "dataSetUri";
        } else if (propertyName.equals("extentTypeCode")) {
            propertyName = "inclusion";    
        // TODO remove when this issue will be fix in MDWeb    
        } else if (propertyName.indexOf("geographicElement") != -1) {
            propertyName = "geographicElement";
        
        // avoid unnecesary log flood
        } else if ((propertyName.equals("westBoundLongitude") || propertyName.equals("eastBoundLongitude") ||
                   propertyName.equals("northBoundLatitude") || propertyName.equals("southBoundLatitude"))
                   && rootClass.getName().equals("org.geotoolkit.metadata.iso.extent.DefaultGeographicDescription")) {
            return null;
        } else if (propertyName.equals("geographicIdentifier") && rootClass.getName().equals("org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox")) {
            return null;
        } if (propertyName.equals("position") && (rootClass.getName().equals("org.geotools.temporal.object.DefaultPeriod"))) {
            return null;
        }
        
        String methodName = "get" + StringUtilities.firstToUpper(propertyName);
        int occurenceType = 0;
        
        while (occurenceType < 4) {

            try {
                Method getter = null;
                switch (occurenceType) {

                    case 0: {
                        getter = rootClass.getMethod(methodName);
                        break;
                    }
                    case 1: {
                        getter = rootClass.getMethod(methodName + "s");
                        break;
                    }
                    case 2: {
                        getter = rootClass.getMethod(methodName + "es");
                        break;
                    }
                    case 3: {
                        if (methodName.endsWith("y")) {
                            methodName = methodName.substring(0, methodName.length() - 1) + 'i';
                        }
                        getter = rootClass.getMethod(methodName + "es");
                        break;
                    }
                   
                }
                return getter;

            } catch (NoSuchMethodException e) {
                occurenceType++;
            }
        }
        logger.severe("No getter have been found for attribute " + propertyName + " in the class " + rootClass.getName());
        return null;
    }
    
    /**
     * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     * 
     * @param propertyName The attribute name.
     * @param classe       The attribute type.  
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    public static Method getSetterFromName(String propertyName, final Class<?> classe, final Class<?> rootClass) {
        logger.finer("search for a setter in " + rootClass.getName() + " of type :" + classe.getName());
        
        //special case
        if (propertyName.equals("beginPosition")) {
            propertyName = "begining";
        } else if (propertyName.equals("endPosition")) {
            propertyName = "ending";
        } 
        
        String methodName = "set" + StringUtilities.firstToUpper(propertyName);
        int occurenceType = 0;
        
        //TODO look all interfaces
        Class<?> interfacee = null;
        if (classe.getInterfaces().length != 0) {
            interfacee = classe.getInterfaces()[0];
        }
        
        Class<?> argumentSuperClass     = classe;
        Class<?> argumentSuperInterface = null;
        if (argumentSuperClass.getInterfaces().length > 0) {
            argumentSuperInterface = argumentSuperClass.getInterfaces()[0];
        }
        

        while (occurenceType < 7) {
            
            try {
                Method setter = null;
                switch (occurenceType) {

                    case 0: {
                        setter = rootClass.getMethod(methodName, classe);
                        break;
                    }
                    case 1: {
                        if (classe.equals(Integer.class)) {
                            setter = rootClass.getMethod(methodName, long.class);
                            break;
                        } else {
                            occurenceType = 2;
                        }
                    }
                    case 2: {
                        setter = rootClass.getMethod(methodName, interfacee);
                        break;
                    }
                    case 3: {
                        setter = rootClass.getMethod(methodName, Collection.class);
                        break;
                    }
                    case 4: {
                        setter = rootClass.getMethod(methodName + "s", Collection.class);
                        break;
                    }
                    case 5: {
                        setter = rootClass.getMethod(methodName , argumentSuperClass);
                        break;
                    }
                    case 6: {
                        setter = rootClass.getMethod(methodName , argumentSuperInterface);
                        break;
                    }
                }
                return setter;

            } catch (NoSuchMethodException e) {

                /**
                 * This switch is for debugging purpose
                 */
                switch (occurenceType) {

                    case 0: {
                        logger.finer("The setter " + methodName + "(" + classe.getName() + ") does not exist");
                        occurenceType = 1;
                        break;
                    }
                    case 1: {
                        logger.finer("The setter " + methodName + "(long) does not exist");
                        occurenceType = 2;
                        break;
                    }
                    case 2: {
                        if (interfacee != null) {
                            logger.finer("The setter " + methodName + "(" + interfacee.getName() + ") does not exist");
                        }
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        logger.finer("The setter " + methodName + "(Collection<" + classe.getName() + ">) does not exist");
                        occurenceType = 4;
                        break;
                    }
                    case 4: {
                        logger.finer("The setter " + methodName + "s(Collection<" + classe.getName() + ">) does not exist");
                        occurenceType = 5;
                        break;
                    }
                    case 5: {
                        if (argumentSuperClass != null) {
                            logger.finer("The setter " + methodName + "(" + argumentSuperClass.getName() + ") does not exist");
                            argumentSuperClass     = argumentSuperClass.getSuperclass();
                            occurenceType = 5;
                            
                        } else {
                            occurenceType = 6;
                        }
                        break;
                    }
                    case 6: {
                        if (argumentSuperInterface != null) {
                            logger.finer("The setter " + methodName + "(" + argumentSuperInterface.getName() + ") does not exist");
                        }
                        occurenceType = 7;
                        break;
                    }
                    default:
                        occurenceType = 7;
                }
            }
        }
        logger.severe("No setter have been found for attribute " + propertyName +
                      " of type " + classe.getName() + " in the class " + rootClass.getName());
        return null;
    }
    
    /**
     * 
     * @param enumeration
     * @return
     */
    public static String getElementNameFromEnum(final Object enumeration) {
        String value = "";
        try {
            Method getValue = enumeration.getClass().getDeclaredMethod("value");
            value = (String) getValue.invoke(enumeration);
        } catch (IllegalAccessException ex) {
            logger.severe("The class is not accessible");
        } catch (IllegalArgumentException ex) {
            logger.severe("IllegalArguement exeption in value()");
        } catch (InvocationTargetException ex) {
            logger.severe("Exception throw in the invokated getter value() " + '\n' +
                       "Cause: " + ex.getMessage());
        } catch (NoSuchMethodException ex) {
           logger.severe("no such method value() in " + enumeration.getClass().getSimpleName());
        } catch (SecurityException ex) {
           logger.severe("security Exception while getting the codelistElement in value() method");
        }
        return value;
    }
    
    /**
     * Obtain the Thread Context ClassLoader.
     */
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
    /**
     * Return an input stream of the specified resource. 
     */
    public static InputStream getResourceAsStream(final String url) {
        ClassLoader cl = getContextClassLoader();
        return cl.getResourceAsStream(url);
    }

    /**
     * Remove the prefix on propertyName.
     * example : removePrefix(csw:GetRecords) return "GetRecords".
     */
    public static String removePrefix(String s) {
        int i = s.indexOf(':');
        if ( i != -1) {
            s = s.substring(i + 1, s.length());
        }
        return s;
    }
    
    /**
     * Load the properties from a properies file. 
     * 
     * If the file does not exist it will be created and an empty Properties object will be return.
     * 
     * @param f a properties file.
     * 
     * @return a Properties Object.
     */
    public static Properties getPropertiesFromFile(final File f) throws  IOException {
        if (f != null) {
            Properties prop = new Properties();
            if (f.exists()) {
                FileInputStream in = null;
                in = new FileInputStream(f);
                prop.load(in);
                in.close();
            } else {
                f.createNewFile();
            }
            return prop;
        } else {
            throw new IllegalArgumentException(" the properties file can't be null");
        }
    }
    
    /**
     * store an Properties object "prop" into the specified File
     * 
     * @param prop A properties Object.
     * @param f    A file.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    public static void storeProperties(final Properties prop, final File f) throws IOException {
        if (prop == null || f == null) {
            throw new IllegalArgumentException(" the properties or file can't be null");
        } else {
            FileOutputStream out = new FileOutputStream(f);
            prop.store(out, "");
            out.close();
        }
    }
    
    /**
     * Write an {@linkplain RenderedImage image} into an output stream, using the mime
     * type specified.
     *
     * @param image The image to write into an output stream.
     * @param mime Mime-type of the output
     * @param output Output stream containing the image.
     * @throws IOException if a writing error occurs.
     */
    public static synchronized void writeImage(final RenderedImage image,
            final String mime, Object output) throws IOException
    {
        if(image == null) throw new NullPointerException("Image can not be null");
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mime);
        while (writers.hasNext()) {
            final ImageWriter writer = writers.next();
            final ImageWriterSpi spi = writer.getOriginatingProvider();
            if (spi.canEncodeImage(image)) {
                ImageOutputStream stream = null;
                if (!isValidType(spi.getOutputTypes(), output)) {
                    stream = ImageIO.createImageOutputStream(output);
                    output = stream;
                }
                writer.setOutput(output);
                writer.write(image);
                writer.dispose();
                if (stream != null) {
                    stream.close();
                }
                return;
            }
        }
        throw new IOException("Unknowed image type");
    }

    /**
     * Returns the mime type matching the extension of an image file.
     * For example, for a file "my_image.png" it will return "image/png", in most cases.
     *
     * @param extension The extension of an image file.
     * @return The mime type for the extension specified.
     *
     * @throws IIOException if no image reader are able to handle the extension given.
     */
    public static String fileExtensionToMimeType(final String extension) throws IIOException {
        final Iterator<ImageReaderSpi> readers = IIORegistry.lookupProviders(ImageReaderSpi.class);
        while (readers.hasNext()) {
            final ImageReaderSpi reader = readers.next();
            final String[] suffixes = reader.getFileSuffixes();
            for (String suffixe : suffixes) {
                if (extension.equalsIgnoreCase(suffixe)) {
                    final String[] mimeTypes = reader.getMIMETypes();
                    if (mimeTypes != null && mimeTypes.length > 0) {
                        return mimeTypes[0];
                    }
                }
            }
        }
        throw new IIOException("No available image reader able to handle the extension specified: "+ extension);
    }

    /**
     * Returns the mime type matching the format name of an image file.
     * For example, for a format name "png" it will return "image/png", in most cases.
     *
     * @param format name The format name of an image file.
     * @return The mime type for the format name specified.
     *
     * @throws IIOException if no image reader are able to handle the format name given.
     */
    public static String formatNameToMimeType(final String formatName) throws IIOException {
        final Iterator<ImageReaderSpi> readers = IIORegistry.lookupProviders(ImageReaderSpi.class);
        while (readers.hasNext()) {
            final ImageReaderSpi reader = readers.next();
            final String[] formats = reader.getFormatNames();
            for (String format : formats) {
                if (formatName.equalsIgnoreCase(format)) {
                    final String[] mimeTypes = reader.getMIMETypes();
                    if (mimeTypes != null && mimeTypes.length > 0) {
                        return mimeTypes[0];
                    }
                }
            }
        }
        throw new IIOException("No available image reader able to handle the format name specified: "+ formatName);
    }

    /**
     * Returns the format name matching the mime type of an image file.
     * For example, for a mime type "image/png" it will return "png", in most cases.
     *
     * @param mimeType The mime type of an image file.
     * @return The format name for the mime type specified.
     *
     * @throws IIOException if no image reader are able to handle the mime type given.
     */
    public static String mimeTypeToFormatName(final String mimeType) throws IIOException {
        final Iterator<ImageReaderSpi> readers = IIORegistry.lookupProviders(ImageReaderSpi.class);
        while (readers.hasNext()) {
            final ImageReaderSpi reader = readers.next();
            final String[] mimes = reader.getMIMETypes();
            for (String mime : mimes) {
                if (mimeType.equalsIgnoreCase(mime)) {
                    final String[] formats = reader.getFormatNames();
                    if (formats != null && formats.length > 0) {
                        return formats[0];
                    }
                }
            }
        }
        throw new IIOException("No available image reader able to handle the mime type specified: "+ mimeType);
    }

    /**
     * Check if the provided object is an instance of one of the given classes.
     */
    private static synchronized boolean isValidType(final Class<?>[] validTypes,
                                                    final Object type)
    {
        for (final Class<?> t : validTypes) {
            if (t.isInstance(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A utility method whitch replace the special character.
     *
     * @param s the string to clean.
     * @return a String without special character.
     */
    public static String cleanSpecialCharacter(String s) {
        if (s != null) {
            s = s.replace('é', 'e');
            s = s.replace('è', 'e');
            s = s.replace('à', 'a');
            s = s.replace('É', 'E');
        }
        return s;
    }

    /**
     * Transform an exception code into the OWS specification.
     * Example : MISSING_PARAMETER_VALUE become MissingParameterValue.
     *
     * @param code
     * @return
     */
    public static String transformCodeName(String code) {
        String result = "";
        while (code.indexOf('_') != -1) {
            final String tmp = code.substring(0, code.indexOf('_')).toLowerCase();
            result += StringUtilities.firstToUpper(tmp);
            code = code.substring(code.indexOf('_') + 1, code.length());
        }
        code = code.toLowerCase();
        result += StringUtilities.firstToUpper(code);
        return result;
    }
    
    /**
     * Read the contents of a file into string.
     * 
     * @param f the file name
     * @return The file contents as string
     * @throws IOException if the file does not exist or cannot be read.
     */
    public static String stringFromFile(File f) throws IOException {
        
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null){
            sb.append(line).append('\n');
        }
        br.close();
        
        return sb.toString();
    }
    
    /**
     * Read the contents of a file into string.
     * 
     * @param f the file name
     * @return The file contents as string
     * @throws IOException if the file does not exist or cannot be read.
     */
    public static void stringToFile(File f, String s) throws IOException {
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(s);
        bw.close();
    }
}
