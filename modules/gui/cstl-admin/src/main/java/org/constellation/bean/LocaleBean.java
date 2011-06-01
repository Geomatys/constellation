/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.bean;

import java.io.Serializable;
import java.util.Locale;
import javax.faces.context.FacesContext;
import org.geotoolkit.util.ArgumentChecks;

/**
 * Managed bean of locale for the user session.
 * used to switch the viewRoot locale.
 *
 * @author Mehdi Sidhoum (Geomatys).
 */
public final class LocaleBean implements Serializable {

    private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();

    public Locale getLocale() {
        return locale;
    }

    /**
     * Creates a new instance of {@code LocaleBean}.
     */
    public LocaleBean() {
    }

    /**
     * Set viewRoot locale to {@code Locale.ENGLISH}
     * @return
     */
    public String toEnglish() {
        setLocale(Locale.ENGLISH);
        return null;
    }

    /**
     * Set viewRoot locale to {@code Locale.FRENCH}
     * @return
     */
    public String toFrench() {
        setLocale(Locale.FRENCH);
        return null;
    }

    public void setLocale(final Locale locale){
        ArgumentChecks.ensureNonNull("locale", locale);
        this.locale = locale;
        final FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(locale);
    }
}
