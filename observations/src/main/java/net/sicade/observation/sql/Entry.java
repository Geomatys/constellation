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
 * Classe de base d'un enregistrement dans une {@linkplain Table table} ou une requ�te. Chacune de
 * ces entr�es repr�sentera un {@linkplain Element element} (ph�nom�ne, proc�dure, s�rie d'images,
 * <cite>etc.</cite>). Les entr�es sont habituellement (mais pas obligatoirement) identifi�es de
 * mani�re unique par leurs {@linkplain #getName noms}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Entry implements Element, Serializable {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -7119518186999674633L;

    /**
     * Nom de cette entr�e. Dans certains cas, un nom peut �tre cr�� � la vol�
     * la premi�re fois o� il sera demand� (voir {@link #createName}).
     */
    private String name;

    /**
     * Remarques s'appliquant � cette entr�e, ou {@code null}.
     */
    private final String remarks;

    /**
     * Construit une entr�e avec le nom sp�cifi�, mais sans remarques associ�es.
     *
     * @param name Nom de l'entr�e.
     */
    protected Entry(final String name) {
        this(name, null);
    }

    /**
     * Construit une entr�e avec le nom et les remarques sp�cifi�s.
     *
     * @param name     Nom de l'entr�e.
     * @param remarks  Remarques s'appliquant � cette entr�e, ou {@code null}.
     */
    protected Entry(final String name,
                    final String remarks)
    {
        this.name    = (name!=null) ? name.trim() : null;
        this.remarks = remarks;
    }

    /**
     * Construit un nom � la vol�. Si cet entr�e a �t� construite avec un nom nul, alors
     * cette m�thode sera appel�e la premi�re fois o� {@link #getName} sera demand�e.
     */
    StringBuilder createName() {
        throw new IllegalStateException();
    }

    /**
     * Retourne le nom de cette entr�e.
     */
    public final String getName() {
        if (name == null) {
            name = createName().toString();
        }
        return name;
    }

    /**
     * Retourne les remarques associ�es � cette entr�e, ou {@code null} s'il n'y en a pas.
     */
    public final String getRemarks() {
        return remarks;
    }

    /**
     * Retourne un nom � afficher dans une interface utilisateur pour cette entr�e. Cette m�thode est
     * appel�e par diff�rentes composantes <cite>Swing</cite>, par exemple {@link javax.swing.JTree}.
     * L'impl�mentation par d�faut retourne le m�me nom que {@link #getName}.
     */
    @Override
    public String toString() {
        final String name = getName();
        return (name==null || name.length()==0) ? Resources.format(ResourceKeys.UNNAMED) : name;
    }

    /**
     * Retourne une valeur hash�e pour cette entr�e. L'impl�mentation par d�faut retourne une
     * valeur bas�e sur le {@linkplain #getName nom} de cette entr�e, sachant que chaque entr�e
     * est suppos�e avoir un nom � peu pr�s unique.
     */
    @Override
    public int hashCode() {
        return getName().hashCode() ^ (int)serialVersionUID;
    }

    /**
     * V�rifie si cette entr�e est identique � l'objet sp�cifi�. L'impl�mentation par d�faut compare
     * le {@linkplain #getName nom} de cette entr�e ainsi que les {@linkplain #getRemarks remarques},
     * ce qui devrait �tre suffisant si chaque entr�e d'une m�me classe a effectivement un nom unique,
     * en supposant que toutes les entr�es pr�sentes dans cette machine virtuelle proviennent de la
     * m�me base de donn�es. Certaines classes d�riv�es feront un examen plus pouss�, mais �a sera
     * surtout par pr�caution. Cette m�thode �vitera donc de comparer les attributs qui pourraient
     * se traduire par un chargement d'une grande quantit� de donn�es.
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
     * Appel�e automatiquement avant l'enregistrement binaire de cette m�thode. Les classes d�riv�es
     * devraient red�finir cette m�thode si elles ont besoin de compl�ter les informations contenues
     * dans des champs qui ne devaient �tres renseign�s que la premi�re fois o� elles �taient
     * n�cessaires.
     *
     * @throws Exception si une erreur est survenue lors de la pr�paration. Sera typiquement
     *         {@link net.sicade.observation.CatalogException}, {@link java.sql.SQLException}
     *         ou {@link java.rmi.RemoteException}.
     */
    protected void preSerialize() throws Exception {
    }

    /**
     * Compl�te les informations spatio-temporelles avant l'enregistrement binaire.
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
