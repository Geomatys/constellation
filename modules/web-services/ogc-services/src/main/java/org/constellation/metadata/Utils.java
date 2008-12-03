/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.metadata;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * A set of utilitie methods.
 * 
 * @author Mehdi Sidhoum, Legal Guilhem.
 */
public class Utils {
    
    /**
     * Returns true if the list contains a string in one of the list elements.
     * 
     * @param list A list of Strings. 
     * @param str  A single string.
     * 
     * @return true if at least an element of the list contains the specifieds string.
     */
    public static boolean matchesStringfromList(List<String> list, String str) {
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
     * Return a string with the first character to upper casse.
     * example : firstToUpper("hello") return "Hello".
     * 
     * @param s the string to modifiy
     * 
     * @return a string with the first character to upper casse.
     */
    public static String firstToUpper(String s) {
        String first = s.substring(0, 1);
        String result = s.substring(1);
        result = first.toUpperCase() + result;
        return result;
    }
    
    /**
     * Returns a Date object from an ISO-8601 representation string. (String defined with pattern yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd).
     * @param dateString
     * @return
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
     * This method extract the timezone from a date string.
     * @param dateString
     * @return
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
     * Return a Date by parsing different kind of date format.
     * 
     * @param date a date representation (example 2002, 02-2007, 2004-03-04, ...)
     * 
     * @return a formated date (example 2002 -> 01-01-2002,  2004-03-04 -> 04-03-2004, ...) 
     */
    public static Date createDate(String date, DateFormat dateFormat) throws ParseException {
        
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
                if (getOccurence(date, "/") == 2) {
                    day = date.substring(0, date.indexOf("/"));
                    date = date.substring(date.indexOf("/") + 1);
                    month = date.substring(0, date.indexOf("/"));
                    year = date.substring(date.indexOf("/") + 1);

                    tmp = getDateFromString(year + "-" + month + "-" + day);
                } else {
                    if (getOccurence(date, "/") == 1) {
                        month = date.substring(0, date.indexOf("/"));
                        year = date.substring(date.indexOf("/") + 1);
                        tmp = getDateFromString(year + "-" + month + "-" + "01");
                    }
                }
            } else if (getOccurence(date, " ") == 2) {
                if (!date.contains("?")) {

                    day = date.substring(0, date.indexOf(" "));
                    date = date.substring(date.indexOf(" ") + 1);
                    month = POOL.get(date.substring(0, date.indexOf(" ")));
                    year = date.substring(date.indexOf(" ") + 1);

                    tmp = getDateFromString(year + "-" + month + "-" + day);
                } else {
                    tmp = getDateFromString("2000" + "-" + "01" + "-" + "01");
                }
            } else if (getOccurence(date, " ") == 1) {
                try {
                    Date d = dateFormat.parse(date);
                    return new Date(d.getTime());
                } catch (ParseException ex) {
                }
                month = POOLcase.get(date.substring(0, date.indexOf(" ")));
                year = date.substring(date.indexOf(" ") + 1);
                tmp = getDateFromString(year + "-" + month + "-" + "01");


            } else if (getOccurence(date, "-") == 1) {

                month = date.substring(0, date.indexOf("-"));
                year = date.substring(date.indexOf("-") + 1);

                tmp = getDateFromString(year + "-" + month + "-" + "01");

            } else if (getOccurence(date, "-") == 2) {

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
     * This method returns a number of occurences occ in the string s.
     */
    public static int getOccurence (String s, String occ){
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
     * Search in the librairies and the classes the child of the specified packages,
     * and return all of them.
     * 
     * @param packages the packages to scan.
     * 
     * @return a list of package names.
     */
    public static List<String> searchSubPackage(String... packages) {
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
                        Logger.getAnonymousLogger().log(Level.FINER, "scanning :" + uri);
                        result.addAll(scan(uri, fileP));
                    } catch (URISyntaxException e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE,"URL, " + url + "cannot be converted to a URI");
                    }
                }
            } catch (IOException ex) {
                Logger.getAnonymousLogger().log(Level.SEVERE,"The resources for the package" + p + ", could not be obtained");
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
    public static List<String> scan(URI u, String filePackageName) throws IOException {
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
                Logger.getAnonymousLogger().warning("unable to scan jar file: " +u.getSchemeSpecificPart());
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
    public static List<String> scanDirectory(File root, String parent) {
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
    public static List<String> scanJar(File file, String parent) throws IOException {
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
    
    /*
     * Encode the specified string with MD5 algorithm.
     * 
     * @param key :  the string to encode.
     * @return the value (string) hexadecimal on 32 bits
     */
    public static String MD5encode(String key) {

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
     * An utilities method which tansform a List of class in a Class[]
     * 
     * @param classes A java.util.List<Class>
     */
    public static Class[] toArray(List<Class> classes) {
        Class[] result = new Class[classes.size()];
        int i = 0;
        for (Class classe : classes) {
            result[i] = classe;
            i++;
        }
        return result;
    }
    
    /**
     * Delete a directory and all its sub-file/directory.
     * 
     * @param directory a file.
     * @return
     */
    public static boolean deleteDirectory(File directory) {
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
    public static List<String> cleanStrings(List<String> list) {
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
    public static String replacePrefix(String s, String localPart, String prefix) {

        return s.replaceAll("[a-zA-Z0-9]*:" + localPart, prefix + ":" + localPart);
    }
    
    /**
     * Return an marshallable Object from an url
     */
    public static Object getUrlContent(String URL, Unmarshaller unmarshaller) throws MalformedURLException, IOException {
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
                    response = ((JAXBElement) response).getValue();
                }
            } catch (JAXBException ex) {
                Logger.getAnonymousLogger().severe("The distant service does not respond correctly: unable to unmarshall response document." + '\n' +
                        "cause: " + ex.getMessage());
            }
        } catch (IOException ex) {
            Logger.getAnonymousLogger().severe("The Distant service have made an error");
            return null;
        }
        return response;
    }
    
    /**
     * Call the empty constructor on the specified class and return the result.
     * 
     * @param classe
     * @return
     */
    public static Object newInstance(Class classe) {
        try {
            if (classe == null)
                return null;
            
            Constructor constructor = classe.getConstructor();
            Logger.getAnonymousLogger().finer("constructor:" + '\n' + constructor.toGenericString());
            
            //we execute the constructor
            Object result = constructor.newInstance();
            return result;
            
        } catch (InstantiationException ex) {
            Logger.getAnonymousLogger().severe("the service can't instanciate the class: " + classe.getName() + "()");
        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe("The service can't access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe("Illegal Argument in empty constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe("invocation target exception in empty constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            Logger.getAnonymousLogger().severe("No such empty constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            Logger.getAnonymousLogger().severe("Security exception while instanciating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Call the constructor(String) on the specified class and return the result.
     * 
     * @param classe
     * @return
     */
    public static Object newInstance(Class classe, String parameter) {
        try {
            if (classe == null)
                return null;
            
            Constructor constructor = classe.getConstructor(String.class);
            Logger.getAnonymousLogger().finer("constructor:" + '\n' + constructor.toGenericString());
            
            //we execute the constructor
            Object result = constructor.newInstance(parameter);
            return result;
            
        } catch (InstantiationException ex) {
            Logger.getAnonymousLogger().severe("the service can't instanciate the class: " + classe.getName() + "(string)");
        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe("The service can't access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe("Illegal Argument in string constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe("invocation target exception in string constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            Logger.getAnonymousLogger().severe("No such string constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            Logger.getAnonymousLogger().severe("Security exception while instanciating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Call the constructor(String) on the specified class and return the result.
     * 
     * @param classe
     * @return
     */
    public static Object newInstance(Class classe, String parameter1, String parameter2) {
        try {
            if (classe == null)
                return null;
            
            Constructor constructor = classe.getConstructor(String.class, String.class);
            Logger.getAnonymousLogger().finer("constructor:" + '\n' + constructor.toGenericString());
            
            //we execute the constructor
            Object result = constructor.newInstance(parameter1, parameter2);
            return result;
            
        } catch (InstantiationException ex) {
            Logger.getAnonymousLogger().severe("the service can't instanciate the class: " + classe.getName() + "(string, string)");
        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe("The service can't access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe("Illegal Argument in double string constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe("invocation target exception in double string constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            Logger.getAnonymousLogger().severe("No such double string constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            Logger.getAnonymousLogger().severe("Security exception while instanciating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Call the constructor(charSequence) on the specified class and return the result.
     * 
     * @param classe
     * @return
     */
    public static Object newInstance(Class classe, CharSequence parameter) {
        try {
            if (classe == null)
                return null;
            
            Constructor constructor = classe.getConstructor(CharSequence.class);
            Logger.getAnonymousLogger().finer("constructor:" + '\n' + constructor.toGenericString());
            
            //we execute the constructor
            Object result = constructor.newInstance(parameter);
            return result;
            
        } catch (InstantiationException ex) {
            Logger.getAnonymousLogger().severe("the service can't instanciate the class: " + classe.getName() + "(CharSequence)");
        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe("The service can't access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe("Illegal Argument in CharSequence constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe("invocation target exception in CharSequence constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            Logger.getAnonymousLogger().severe("No such CharSequence constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            Logger.getAnonymousLogger().severe("Security exception while instanciating class: " + classe.getName());
        }
        return null;
    }
    
    /**
     * Invoke a method with the specified parameter in the specified object.
     * 
     * @param method     The method to invoke
     * @param object     The object on witch the method is invoked.
     * @param parameter  The parameter of the method.
     */
    public static Object invokeMethod(Method method, Object object) {
        Object result = null;
        String baseMessage = "unable to invoke setter: "; 
        try {
            if (method != null) {
                result = method.invoke(object);
            } else {
                Logger.getAnonymousLogger().severe(baseMessage + "The setter is null");
            }

        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe(baseMessage + "The class is not accessible");

        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe(baseMessage + "The argument does not match with the method");

        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe(baseMessage + "Exception throw in the invokated method");
        }
        return result;
    }
    
    /**
     * Invoke a method with the specified parameter in the specified object.
     * 
     * @param method     The method to invoke
     * @param object     The object on witch the method is invoked.
     * @param parameter  The parameter of the method.
     */
    public static Object invokeMethod(Method method, Object object, Object parameter) {
        Object result = null;
        String baseMessage = "unable to invoke setter: "; 
        try {
            if (method != null) {
                result = method.invoke(object, parameter);
            } else {
                Logger.getAnonymousLogger().severe(baseMessage + "The setter is null");
            }

        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe(baseMessage + "The class is not accessible");

        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe(baseMessage + "The argument does not match with the method");

        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe(baseMessage + "Exception throw in the invokated method");
        }
        return result;
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
    public static Method getMethod(String propertyName, Class classe) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName);

        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().info("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            Logger.getAnonymousLogger().info("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            Logger.getAnonymousLogger().info("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
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
    public static Method getMethod(String propertyName, Class classe, Class parameterClass) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName, parameterClass);

        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().info("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            Logger.getAnonymousLogger().info("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            Logger.getAnonymousLogger().info("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
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
    public static Method getGetterFromName(String propertyName, Class rootClass) {
        Logger.getLogger(propertyName).finer("search for a getter in " + rootClass.getName() + " of name :" + propertyName);
        
        //special case and corrections
        if (propertyName.equals("beginPosition")) {
            propertyName = "begining";
        } else if (propertyName.equals("endPosition")) {
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
        }
        
        String methodName = "get" + Utils.firstToUpper(propertyName);
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
                if (getter != null) {
                    Logger.getAnonymousLogger().finer("getter found: " + getter.toGenericString());
                }
                return getter;

            } catch (NoSuchMethodException e) {

                switch (occurenceType) {

                    case 0: {
                        Logger.getAnonymousLogger().finer("The getter " + methodName + "() does not exist");
                        occurenceType = 1;
                        break;
                    }

                    case 1: {
                        Logger.getAnonymousLogger().finer("The getter " + methodName + "s() does not exist");
                        occurenceType = 2;
                        break;
                    }
                    case 2: {
                        Logger.getAnonymousLogger().finer("The getter " + methodName + "es() does not exist");
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        Logger.getAnonymousLogger().finer("The getter " + methodName + "es() does not exist");
                        occurenceType = 4;
                        break;
                    }
                    default:
                        occurenceType = 5;
                }
            }
        }
        Logger.getAnonymousLogger().severe("No getter have been found for attribute " + propertyName + " in the class " + rootClass.getName());
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
    public static Method getSetterFromName(String propertyName, Class classe, Class rootClass) {
        Logger.getAnonymousLogger().finer("search for a setter in " + rootClass.getName() + " of type :" + classe.getName());
        
        //special case
        if (propertyName.equals("beginPosition")) {
            propertyName = "begining";
        } else if (propertyName.equals("endPosition")) {
            propertyName = "ending";
        } 
        
        String methodName = "set" + Utils.firstToUpper(propertyName);
        int occurenceType = 0;
        
        //TODO look all interfaces
        Class interfacee = null;
        if (classe.getInterfaces().length != 0) {
            interfacee = classe.getInterfaces()[0];
        }
        
        Class argumentSuperClass     = classe;
        Class argumentSuperInterface = null;
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
                Logger.getAnonymousLogger().finer("setter found: " + setter.toGenericString());
                return setter;

            } catch (NoSuchMethodException e) {

                /**
                 * This switch is for debugging purpose
                 */
                switch (occurenceType) {

                    case 0: {
                        Logger.getAnonymousLogger().finer("The setter " + methodName + "(" + classe.getName() + ") does not exist");
                        occurenceType = 1;
                        break;
                    }
                    case 1: {
                        Logger.getAnonymousLogger().finer("The setter " + methodName + "(long) does not exist");
                        occurenceType = 2;
                        break;
                    }
                    case 2: {
                        if (interfacee != null) {
                            Logger.getAnonymousLogger().finer("The setter " + methodName + "(" + interfacee.getName() + ") does not exist");
                        }
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        Logger.getAnonymousLogger().finer("The setter " + methodName + "(Collection<" + classe.getName() + ">) does not exist");
                        occurenceType = 4;
                        break;
                    }
                    case 4: {
                        Logger.getAnonymousLogger().finer("The setter " + methodName + "s(Collection<" + classe.getName() + ">) does not exist");
                        occurenceType = 5;
                        break;
                    }
                    case 5: {
                        if (argumentSuperClass != null) {
                            Logger.getAnonymousLogger().finer("The setter " + methodName + "(" + argumentSuperClass.getName() + ") does not exist");
                            argumentSuperClass     = argumentSuperClass.getSuperclass();
                            occurenceType = 5;
                            
                        } else {
                            occurenceType = 6;
                        }
                        break;
                    }
                    case 6: {
                        if (argumentSuperInterface != null) {
                            Logger.getAnonymousLogger().finer("The setter " + methodName + "(" + argumentSuperInterface.getName() + ") does not exist");
                        }
                        occurenceType = 7;
                        break;
                    }
                    default:
                        occurenceType = 7;
                }
            }
        }
        Logger.getAnonymousLogger().severe("No setter have been found for attribute " + propertyName + 
                      " of type " + classe.getName() + " in the class " + rootClass.getName());
        return null;
    }
    
    /**
     * 
     * @param enumeration
     * @return
     */
    public static String getElementNameFromEnum(Object enumeration) {
        String value = "";
        try {
            Method getValue = enumeration.getClass().getDeclaredMethod("value");
            value = (String) getValue.invoke(enumeration);
        } catch (IllegalAccessException ex) {
            Logger.getAnonymousLogger().severe("The class is not accessible");
        } catch (IllegalArgumentException ex) {
            Logger.getAnonymousLogger().severe("IllegalArguement exeption in value()");
        } catch (InvocationTargetException ex) {
            Logger.getAnonymousLogger().severe("Exception throw in the invokated getter value() " + '\n' +
                       "Cause: " + ex.getMessage());
        } catch (NoSuchMethodException ex) {
           Logger.getAnonymousLogger().severe("no such method value() in " + enumeration.getClass().getSimpleName());
        } catch (SecurityException ex) {
           Logger.getAnonymousLogger().severe("security Exception while getting the codelistElement in value() method");
        }
        return value;
    }
    
    /**
     * Obtain the Thread Context ClassLoader.
     */
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
    /**
     * Return an input stream of the specified resource. 
     */
    public static InputStream getResourceAsStream(String url) {
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
    public static Properties getPropertiesFromFile(File f) throws  IOException {
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
    public static void storeProperties(Properties prop, File f) throws IOException {
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
}
