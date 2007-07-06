/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.observation.sql;

// J2SE dependencies
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;


/**
 * Ensemble des tables qui utilisent la {@linkplain Table table} annotée.
 * Cette annotation est l'inverse de {@link Use} et n'existe que temporairement.
 * Elle sera supprimée lorsque l'on aura configuré {@code apt} de façon à générer
 * cette information automatiquement.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Documented
@Target(TYPE)
@Retention(SOURCE)
public @interface UsedBy {
    /**
     * Ensemble des tables qui utilisent la {@linkplain Table table} annotée.
     */
    Class<? extends Table>[] value();
}
