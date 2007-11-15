/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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
package net.seagis.coverage.catalog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.*;
import junit.framework.TestCase;
import static org.junit.Assert.*;


/**
 * Tests {@link SeriesEntry}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesEntryTest extends TestCase {
    /**
     * Returns a dummy series for the given root and path.
     */
    private static SeriesEntry series(final String root, final String path) {
        return new SeriesEntry("Some entry", null, root, path, "png", "UTF-8", null, true, null);
    }

    /**
     * Tests a relative file name.
     */
    @Test
    public void testRelativeFile() throws IOException {
        final SeriesEntry entry = series("/SomeRoot/SomeSub/", "SomeSeries/2");
        final String   expected = "/SomeRoot/SomeSub/SomeSeries/2/foo.png";
        final File         file = entry.file("foo");
        final URL           url = entry.url(file);
        assertTrue  (file.isAbsolute());
        assertEquals(new File(expected), file);
        assertEquals(new URL("file://" + expected), url);
    }

    /**
     * Tests an absolute file name.
     */
    @Test
    public void testAbsoluteFile() throws IOException {
        final SeriesEntry entry = series("/SomeRoot/SomeSub/", "/SomeSeries/2");
        final String   expected = "/SomeSeries/2/foo.png";
        final File         file = entry.file("foo");
        final URL           url = entry.url(file);
        assertTrue  (file.isAbsolute());
        assertEquals(new File(expected), file);
        assertEquals(new URL("file://" + expected), url);
    }

    /**
     * Tests a relative URL.
     */
    @Test
    public void testRelativeURL() throws IOException {
        final SeriesEntry entry = series("ftp://localhost/SomeRoot/SomeSub/", "SomeSeries/2");
        final String   expected = "SomeRoot/SomeSub/SomeSeries/2/foo.png";
        final File         file = entry.file("foo");
        final URL           url = entry.url(file);
        assertFalse (file.isAbsolute());
        assertEquals(new File(expected), file);
        assertEquals(new URL("ftp://localhost/" + expected), url);
    }

    /**
     * Tests an absolute URL.
     */
    @Test
    public void testAbsoluteURL() throws IOException {
        final SeriesEntry entry = series("ftp://localhost/SomeRoot/SomeSub/", "ftp://localhost/SomeSeries/2");
        final String   expected = "SomeSeries/2/foo.png";
        final File         file = entry.file("foo");
        final URL           url = entry.url(file);
        assertFalse (file.isAbsolute());
        assertEquals(new File(expected), file);
        assertEquals(new URL("ftp://localhost/" + expected), url);
    }
}
