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

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.InvalidObjectException;

import org.opengis.util.InternationalString;


/**
 * Cl� d�signant un aspect d'une configuration d'une base de donn�es. Une cl� peut d�signer
 * par exemple le pilote JDBC � utiliser pour la connexion, ou une des requ�te SQL utilis�e.
 * Un ensemble de cl�s sont pr�d�finies comme variables statiques dans les impl�mentations
 * c�t� serveur des diff�rentes interfaces. Les valeurs correspondantes peuvent �tre modifi�es
 * par {@link net.sicade.gui.swing.ConfigurationEditor}.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public final class ConfigurationKey implements Serializable {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 3068136990916953221L;

    /**
     * Ensemble des cl�s d�j� cr��es dans cette machine virtuelle. Chaque cl� est cr��e une fois
     * pour toute et ajout�e � cet ensemble. La cr�ation des cl�s se fait habituellement lors de
     * l'initialisation des classes qui les contient comme variables statiques.
     */
    private static final Map<String,ConfigurationKey> POOL = new HashMap<String,ConfigurationKey>();

    /**
     * Le nom de la cl� courante. Chaque cl� a un nom unique.
     */
    private final String name;

    /**
     * Valeur par d�faut associ�e � la cl�. Cette valeur est cod�e en dur dans les impl�mentations
     * qui d�finissent des cl�s comme variables statiques. Cette valeur par d�faut ne sera utilis�e
     * que si l'utilisateur n'a pas sp�cifi� explicitement de valeur dans le fichier de configuration.
     */
    private final transient String defaultValue;

    /**
     * Construit une nouvelle cl�.
     *
     * @param name          Le nom de la cl� � cr�er. Ce nom doit �tre unique.
     * @param defaultValue  Valeur par d�faut associ�e � la cl�, utilis�e que si l'utilisateur n'a
     *                      pas sp�cifi� explicitement de valeur.
     *
     * @throws IllegalStateException si une cl� a d�j� �t� cr��e pr�c�demment pour le nom sp�cifi�.
     */
    public ConfigurationKey(final String name, final String defaultValue) throws IllegalStateException {
        this.name         = name.trim();
        this.defaultValue = defaultValue;
        synchronized (POOL) {
            if (POOL.put(name, this) != null) {
                throw new IllegalStateException("Doublon dans les noms de cl�s."); // TODO: localize
            }
        }
    }

    /**
     * Retourne le nom de la cl� courante. Chaque cl� a un nom unique.
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne la valeur par d�faut associ�e � la cl�. Cette valeur est cod�e en dur dans
     * les impl�mentations qui d�finissent des cl�s comme variables statiques. Cette valeur
     * par d�faut ne sera utilis�e que si l'utilisateur n'a pas sp�cifi� explicitement de
     * valeur dans le fichier de configuration.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retourne une instance unique de cette cl� apr�s lecture binaire.
     *
     * @todo L'impl�mentation actuelle �chouera si la classe qui d�clare cette cl� comme variable
     *       statique n'a pas �t� initialis�e avant la d�serialisation de cette cl�e.
     */
    protected Object readResolve() throws ObjectStreamException {
        synchronized (POOL) {
            final Object r = POOL.get(name);
            if (r != null) {
                return r;
            }
        }
        throw new InvalidObjectException("Cl� inconnue: "+name); // TODO: localize
    }

    /**
     * Retourne le nom de cette cl�, pour inclusion dans une interface graphique.
     */
    @Override
    public String toString() {
        return name;
    }
}
