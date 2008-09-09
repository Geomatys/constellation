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
package net.seagis.catalog;


/**
 * An exception that occured on the server side. The {@linkplain #getCause cause} is implementation
 * dependent, but will typically be an instance of {@link java.sql.SQLException} or
 * {@link java.rmi.RemoteException}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ServerException extends CatalogException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -6413382346019648505L;

    /**
     * Creates an exception with the specified cause and no details message.
     *
     * @param cause The cause for this exception.
     */
    public ServerException(final Exception cause) {
        super(cause);
    }
}
