/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007 Geomatys
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
 */
package net.seagis.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.geotools.resources.Arguments;


/**
 * Compares a dump of PostGrid schema with the schema on SVN.
 * This is an utility method for making easier to spot changes.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DumpComparator {
    /**
     * All lines, excluding empty and comment lines. Spaces characters are
     * collapsed into a single {@code ' '} character.
     */
    private final List<String> lines;

    /**
     * Creates an initialy empty comparator.
     */
    private DumpComparator() {
        lines = new LinkedList<String>();
    }

    /**
     * Loads all lines from the specified file.
     */
    private void load(final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final StringBuilder  buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("--")) {
                continue;
            }
            final StringTokenizer tokens = new StringTokenizer(line);
            while (tokens.hasMoreTokens()) {
                if (buffer.length() != 0) {
                    buffer.append(' ');
                }
                buffer.append(tokens.nextToken());
            }
            if (line.endsWith(";")) {
                lines.add(buffer.toString());
                buffer.setLength(0);
            }
        }
        reader.close();
        if (buffer.length() != 0) {
            lines.add(buffer.toString());
        }
    }

    /**
     * Remove the {@code postgrid} schema name from the specified line.
     */
    private static String removeSchema(final String line) {
        return line.replace(" postgrid.", " ");
    }

    /**
     * Removes common lines from both {@code DumpComparator}.
     * Returns the number of lines removed.
     */
    private int makeDisjoint(final DumpComparator other, final boolean ignoreSchema) {
        int count = 0;
        for (final Iterator<String> it=lines.iterator(); it.hasNext();) {
            String search = it.next();
            if (ignoreSchema) {
                search = removeSchema(search);
            }
            for (final Iterator<String> scan=other.lines.iterator(); scan.hasNext();) {
                String candidate = scan.next();
                if (ignoreSchema) {
                    candidate = removeSchema(candidate);
                }
                if (search.equals(candidate)) {
                    scan.remove();
                    it.remove();
                    count++;
                    break; // We want to remove only the first occurence.
                }
            }
        }
        return count;
    }

    /**
     * Removes every lines starting with the specified prefix.
     */
    private int removeLineStartingWith(final String prefix) {
        int count = 0;
        for (final Iterator<String> it=lines.iterator(); it.hasNext();) {
            final String candidate = it.next();
            if (candidate.startsWith(prefix)) {
                it.remove();
                count++;
            }
        }
        return count;
    }

    /**
     * Prints unmached lines.
     */
    private void printLines(final String title, final PrintWriter out) {
        out.println("--");
        out.print  ("-- ");
        out.println(title);
        out.println("--");
        out.println();
        for (final String line : lines) {
            out.println(line);
        }
        out.println();
        out.println();
        out.println();
        out.println();
    }

    /**
     * Runs from the command line.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final String oldDir  = arguments.getRequiredString("-old");
        final String newFile = arguments.getRequiredString("-new");
        args = arguments.getRemainingArguments(0);
        final DumpComparator oldSchema = new DumpComparator();
        final DumpComparator newSchema = new DumpComparator();
        try {
            oldSchema.load(new File(oldDir, "postgrid.sql"));
            oldSchema.load(new File(oldDir, "postgrid-model.sql"));
            oldSchema.load(new File(oldDir, "postgrid-comments.sql"));
            newSchema.load(new File(newFile));
        } catch (IOException e) {
            arguments.err.println(e);
            return;
        }
        final PrintWriter out = arguments.out;
        out.print(oldSchema.makeDisjoint(newSchema, false));            out.println(" identical lines");
        out.print(oldSchema.removeLineStartingWith("SET "));            out.println(" ignored SET");
        out.print(newSchema.removeLineStartingWith("REVOKE ALL ON "));  out.println(" ignored REVOKE ALL");
        out.print(oldSchema.makeDisjoint(newSchema, true));             out.println(" identical lines ignoring schema");
        oldSchema.printLines("Unmached lines in old schema", out);
        newSchema.printLines("Unmached lines in new schema", out);
    }
}
