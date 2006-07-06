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
package net.sicade.observation.coverage;

import net.sicade.observation.Element;
import net.sicade.observation.Observation;


/**
 * Position spatio-temporelle relative � une {@linkplain Observation observation}.
 * Il s'agit du d�calage entre une observation (p�che, <cite>etc.</cite>) et un param�tre
 * environnemental (temp�rature, <cite>etc.</cite>) utilis� pour d�crire l'environnement
 * de cette observation. Un d�calage spatio-temporelle est une des trois composantes du
 * {@linkplain Descriptor descripteur du paysage oc�anique}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface LocationOffset extends Element {
    /**
     * Retourne le d�calage temporelle, en nombre de jours. Par exemple la valeur -2 indique que
     * l'on s'int�ressera � la valeur d'un param�tre environnemental (par exemple la temp�rature)
     * deux jours avant l'observation (par exemple une p�che).
     */
    double getDayOffset();

    /**
     * Retourne le d�calage vers l'est, en m�tres. Par exemple la valeur 1000 indique que l'on
     * s'int�resse � la valeur d'un param�tre environnemental 1 kilom�tre � l'est de l'observation.
     */
    double getEasting();

    /**
     * Retourne le d�calage vers le nord, en m�tres. Par exemple la valeur 1000 indique que l'on
     * s'int�resse � la valeur d'un param�tre environnemental 1 kilom�tre au nord de l'observation.
     */
    double getNorthing();

    /**
     * Retourne le d�calage en altitude, en m�tres. Par exemple la valeur 100 indique que l'on
     * s'int�resse � la valeur d'un param�tre environnemental 100 m�tres sous l'observation (par
     * exemple sous la surface de la mer lorsque l'observation est une p�che de surface telle que
     * la p�che � la senne).
     */
    double getAltitudeOffset();
}
