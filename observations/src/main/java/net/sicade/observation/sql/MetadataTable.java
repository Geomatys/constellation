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
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Geotools dependencies
import org.geotools.metadata.sql.MetadataSource;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.quality.DataQualityImpl;


/**
 * Connexion vers la table des m�ta-donn�es. Cette connexion cache les requ�tes pr�c�dentes
 * par des r�f�rences fortes. Nous supposons que les m�ta-donn�es sont assez peu nombreuses
 * et n'ont pas besoin d'un m�canisme utilisant des r�f�rences faibles.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Le m�canisme de cache serait plus efficace � l'int�rieur de {@link MetadataSource}.
 */
public class MetadataTable extends Table implements Shareable {
    /**
     * Connexion vers la base des m�ta-donn�es.
     */
    private MetadataSource source;

    /**
     * Ensemble des m�ta-donn�es qui ont d�j� �t� cr��es.
     */
    private final Map<Class, Map<String,Object>> pool = new HashMap<Class, Map<String,Object>>();

    /**
     * Construit une connexion vers la table des m�ta-donn�es.
     * 
     * @param  database Connexion vers la table des plateformes qui utilisera cette table des stations.
     */
    public MetadataTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la m�ta-donn�es correspondant � l'identifiant sp�cifi�.
     *
     * @param type Le type de m�ta-donn�e (par exemple <code>{@linkplain Citation}.class</code>).
     * @param identifier L'identifiant de la m�ta-donn�e d�sir�e.
     *
     * @throws SQLException si l'acc�s � la base de donn�es a �chou�.
     */
    public synchronized <T> T getEntry(final Class<T> type, final String identifier) throws SQLException {
        Map<String,Object> p = pool.get(type);
        if (p == null) {
            p = new HashMap<String,Object>();
            pool.put(type, p);
        }
        T candidate = type.cast(p.get(identifier));
        if (candidate == null) {
            if (source == null) {
                source = new MetadataSource(database.getConnection());
            }
            candidate = type.cast(source.getEntry(type, identifier));
            /*
             * Extrait imm�diatement les informations les plus utilis�es telles que les titres
             * des citations, afin d'�viter des connexions trop fr�quentes � la base de donn�es
             * (par exemple chaque fois que l'on veut v�rifier le fournisseur pour savoir si une
             * station doit �tre inclue dans une liste).
             */
            if (candidate instanceof DataQuality) {
                // TODO
            }
            if (candidate instanceof Citation) {
                candidate = type.cast(new CitationImpl((Citation) candidate));
            }
            p.put(identifier, candidate);
        }
        return candidate;
    }
}
