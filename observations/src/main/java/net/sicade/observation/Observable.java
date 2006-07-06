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
package net.sicade.observation;


/**
 * Repr�sentation d'un {@linkplain Phenomenon ph�nom�ne} observ� � l'aide d'une certaine
 * {@linkplain Procedure procedure}. Un observable peut �tre un param�tre physique tel que
 * la temp�rature de surface de la mer, un param�tre halieutique tel que la quantit� d'espadons
 * p�ch�s, ou d'autre observables propres � un domaine d'�tude.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Observable extends Element {
    /**
     * Retourne un num�ro unique identifiant cet observable. Ce num�ro est compl�mentaire (et dans
     * une certaine mesure redondant) avec {@linkplain #getName le symbole} de l'observable. Il
     * existe parce que les observables, ainsi que les {@linkplain Station stations}, sont
     * r�f�renc�s dans des millions de lignes dans la table des observations.
     */
    int getNumericIdentifier();

    /**
     * Retourne le ph�nom�ne observ�. Si cet observable repr�sente un param�tre physique tel
     * que la temp�rature de surface de la mer, alors le ph�nom�ne retourn� sera typiquement un
     * {@linkplain net.sicade.observation.coverage.Thematic th�me}. Si cet observable repr�sente
     * une quantit� li�e � une certaine esp�ce de poisson (par exemple la quantit� p�ch�e, mais �a
     * pourrait �tre d'autres quantit�s obtenues selon d'autres {@linkplain Procedure proc�dures}),
     * alors le ph�nom�ne retourn� sera typiquement une
     * {@linkplain net.sicade.observation.fishery.Species esp�ce}.
     */
    Phenomenon getPhenomenon();

    /**
     * Retourne la procedure utilis�e. Si cet observable repr�sente un param�tre physique tel que
     * la temp�rature de surface de la mer, alors la proc�dure retourn�e sera typiquement un
     * {@linkplain net.sicade.observation.coverage.Operation op�rateur d'images}. Si cet observable
     * repr�sente une quantit� de poisson p�ch�e, alors la proc�dure retourn�e sera typiquement un
     * {@linkplain net.sicade.observation.fishery.FisheryType type de p�che}.
     */
    Procedure getProcedure();

    /**
     * Retourne la distribution statistique approximative des valeurs attendues. Cette distribution
     * n'est pas n�cessairement d�termin�e � partir des donn�es, mais peut �tre un <cite>a-priori</cite>.
     * Cette information est utilis�e principalement par les
     * {@linkplain net.sicade.observation.coverage.LinearModel mod�les lin�aires} qui ont besoin d'une
     * <A HREF="http://mathworld.wolfram.com/NormalDistribution.html">distribution normale</A>.
     * Cette m�thode retourne {@code null} si la distribution est inconnue ou n'est pas pertinente
     * pour cet observable.
     */
    Distribution getDistribution();
}
