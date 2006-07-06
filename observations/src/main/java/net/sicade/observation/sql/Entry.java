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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.sql;

// J2SE dependencies
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.InvalidObjectException;

// Sicade dependencies
import net.sicade.observation.Element;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;
import org.geotools.resources.Utilities;


/**
 * Classe de base d'un enregistrement dans une {@linkplain Table table} ou une requête. Chacune de
 * ces entrées représentera un {@linkplain Element element} (phénomène, procédure, série d'images,
 * <cite>etc.</cite>). Les entrées sont habituellement (mais pas obligatoirement) identifiées de
 * manière unique par leurs {@linkplain #getName noms}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Entry implements Element, Serializable {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7119518186999674633L;

    /**
     * Nom de cette entrée. Dans certains cas, un nom peut être créé à la volé
     * la première fois où il sera demandé (voir {@link #createName}).
     */
    private String name;

    /**
     * Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    private final String remarks;

    /**
     * Construit une entrée avec le nom spécifié, mais sans remarques associées.
     *
     * @param name Nom de l'entrée.
     */
    protected Entry(final String name) {
        this(name, null);
    }

    /**
     * Construit une entrée avec le nom et les remarques spécifiés.
     *
     * @param name     Nom de l'entrée.
     * @param remarks  Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    protected Entry(final String name,
                    final String remarks)
    {
        this.name    = (name!=null) ? name.trim() : null;
        this.remarks = remarks;
    }

    /**
     * Construit un nom à la volé. Si cet entrée a été construite avec un nom nul, alors
     * cette méthode sera appelée la première fois où {@link #getName} sera demandée.
     */
    StringBuilder createName() {
        throw new IllegalStateException();
    }

    /**
     * Retourne le nom de cette entrée.
     */
    public final String getName() {
        if (name == null) {
            name = createName().toString();
        }
        return name;
    }

    /**
     * Retourne les remarques associées à cette entrée, ou {@code null} s'il n'y en a pas.
     */
    public final String getRemarks() {
        return remarks;
    }

    /**
     * Retourne un nom à afficher dans une interface utilisateur pour cette entrée. Cette méthode est
     * appelée par différentes composantes <cite>Swing</cite>, par exemple {@link javax.swing.JTree}.
     * L'implémentation par défaut retourne le même nom que {@link #getName}.
     */
    @Override
    public String toString() {
        final String name = getName();
        return (name==null || name.length()==0) ? Resources.format(ResourceKeys.UNNAMED) : name;
    }

    /**
     * Retourne une valeur hashée pour cette entrée. L'implémentation par défaut retourne une
     * valeur basée sur le {@linkplain #getName nom} de cette entrée, sachant que chaque entrée
     * est supposée avoir un nom à peu près unique.
     */
    @Override
    public int hashCode() {
        return getName().hashCode() ^ (int)serialVersionUID;
    }

    /**
     * Vérifie si cette entrée est identique à l'objet spécifié. L'implémentation par défaut compare
     * le {@linkplain #getName nom} de cette entrée ainsi que les {@linkplain #getRemarks remarques},
     * ce qui devrait être suffisant si chaque entrée d'une même classe a effectivement un nom unique,
     * en supposant que toutes les entrées présentes dans cette machine virtuelle proviennent de la
     * même base de données. Certaines classes dérivées feront un examen plus poussé, mais ça sera
     * surtout par précaution. Cette méthode évitera donc de comparer les attributs qui pourraient
     * se traduire par un chargement d'une grande quantité de données.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Entry that = (Entry) object;
            return Utilities.equals(this.getName(),    that.getName()) &&
                   Utilities.equals(this.getRemarks(), that.getRemarks());
        }
        return false;
    }

    /**
     * Appelée automatiquement avant l'enregistrement binaire de cette méthode. Les classes dérivées
     * devraient redéfinir cette méthode si elles ont besoin de compléter les informations contenues
     * dans des champs qui ne devaient êtres renseignés que la première fois où elles étaient
     * nécessaires.
     *
     * @throws Exception si une erreur est survenue lors de la préparation. Sera typiquement
     *         {@link net.sicade.observation.CatalogException}, {@link java.sql.SQLException}
     *         ou {@link java.rmi.RemoteException}.
     */
    protected void preSerialize() throws Exception {
    }

    /**
     * Complète les informations spatio-temporelles avant l'enregistrement binaire.
     */
    private synchronized void writeObject(final ObjectOutputStream out) throws IOException {
        try {
            preSerialize();
        } catch (Exception exception) {
            final InvalidObjectException e = new InvalidObjectException("Can't complete before serialization.");
            e.initCause(exception);
            throw e;
        }
        out.defaultWriteObject();
    }
}
