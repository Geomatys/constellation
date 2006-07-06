/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation.sql;

import net.sicade.observation.Procedure;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Procedure proc�dure}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 */
public class ProcedureEntry extends Entry implements Procedure {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -1370011712794916454L;

    /**
     * Construit une nouvelle proc�dure du nom sp�cifi�.
     *
     * @param name Le nom de la proc�dure.
     */
    protected ProcedureEntry(final String name) {
        super(name);
    }

    /** 
     * Construit une nouvelle proc�dure du nom sp�cifi� avec les remarques sp�cifi�es.
     *
     * @param name    Le nom de la proc�dure.
     * @param remarks Remarques s'appliquant � cette proc�dure, ou {@code null}.
     */
    protected ProcedureEntry(final String name, final String remarks) {
        super(name, remarks);
    }
}
