/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
 */
package net.sicade.observation;

import org.opengis.observation.Process;
import net.sicade.catalog.Entry;


/**
 * Implémentation d'une entrée représentant une {@linkplain Procedure procédure}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 */
public class ProcessEntry extends Entry implements Process {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -1370011712794916454L;

    /**
     * Construit une nouvelle procédure du nom spécifié.
     *
     * @param name Le nom de la procédure.
     */
    public ProcessEntry(final String name) {
        super(name);
    }

    /** 
     * Construit une nouvelle procédure du nom spécifié avec les remarques spécifiées.
     *
     * @param name    Le nom de la procédure.
     * @param remarks Remarques s'appliquant à cette procédure, ou {@code null}.
     */
    public ProcessEntry(final String name, final String remarks) {
        super(name, remarks);
    }
}
