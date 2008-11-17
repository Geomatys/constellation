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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            URI jarUri = URI.create(u.getSchemeSpecificPart());
            String jarFile = jarUri.getPath();
            jarFile = jarFile.substring(0, jarFile.indexOf('!'));
            result.addAll(scanJar(new File(jarFile), filePackageName));
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

}
