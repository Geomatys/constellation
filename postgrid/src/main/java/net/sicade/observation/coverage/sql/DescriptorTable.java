/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.sicade.observation.coverage.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.observation.sql.Role;

import org.geotools.resources.CharUtilities;

import net.sicade.observation.Distribution;
import net.sicade.observation.CatalogException;
import net.sicade.observation.NoSuchRecordException;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.Layer;
import net.sicade.observation.coverage.LocationOffset;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.DistributionTable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.QueryType;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion vers la table des {@linkplain Descriptor descripteurs}. Les informations nécessaires à
 * la construction des descripteurs sont puisées principalement dans trois tables: {@link LayerTable},
 * {@link LocationOffsetTable} et {@link OperationTable}. De ces trois tables, la table des couches
 * est particulière du fait qu'elle n'est pas sensée être {@linkplain Shareable partageable}. Cela
 * n'empêche toutefois pas {@code DescriptorTable} de l'être, puisqu'il utilise par défaut une table
 * des couches globales dont il ne modifiera pas la configuration.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@Use({LayerTable.class, OperationTable.class, LocationOffsetTable.class, DistributionTable.class})
@UsedBy({LinearModelTable.class, DescriptorSubstitutionTable.class})
public class DescriptorTable extends SingletonTable<Descriptor> implements Shareable {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column symbol, identifier, phenomenon, procedure, offset, band, distribution;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter bySymbol, byIdentifier;

    /**
     * La table des couches. Elle sera construite la première fois où elle sera nécessaire.
     */
    private LayerTable layer;
    
    /**
     * La table des opérations. Ne sera construite que la première fois où elle sera nécessaire.
     */
    private OperationTable operations;
    
    /**
     * La table des positions relatives.
     * Ne sera construite que la première fois où elle sera nécessaire.
     */
    private LocationOffsetTable offsets;
    
    /**
     * La table des distributions.
     * Ne sera construite que la première fois où elle sera nécessaire.
     */
    private DistributionTable distributions;
    
    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public DescriptorTable(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        symbol       = new Column   (query, "Descriptors", "symbol",       usage);
        identifier   = new Column   (query, "Descriptors", "identifier",   usage);
        phenomenon   = new Column   (query, "Descriptors", "phenomenon",   usage);
        procedure    = new Column   (query, "Descriptors", "procedure",    usage);
        offset       = new Column   (query, "Descriptors", "offset",       usage);
        band         = new Column   (query, "Descriptors", "band",         usage);
        distribution = new Column   (query, "Descriptors", "distribution", usage);
        bySymbol     = new Parameter(query, symbol,     SELECT);
        byIdentifier = new Parameter(query, identifier, SELECT);
        symbol    .setRole(Role.NAME);
        identifier.setRole(Role.IDENTIFIER);
        identifier.setOrdering("ASC");
    }

    /**
     * Définie la table des couches à utiliser. Cette méthode peut être appelée par {@link LayerTable}
     * immédiatement après la construction de {@code DescriptorTable} et avant toute première utilisation.
     * Notez que les instances de {@code DescriptorTable} ainsi créées ne seront pas partagées par
     * {@link Database#getTable}.
     *
     * @param  layer Table des couches à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des couches.
     */
    protected synchronized void setLayerTable(final LayerTable layer)
            throws IllegalStateException
    {
        if (this.layer != layer) {
            if (this.layer != null) {
                throw new IllegalStateException();
            }
            this.layer = layer;
        }
    }

    /**
     * Retourne une entrée pour le nom spécifié. Cette méthode est tolérante au nom: si ce dernier
     * est purement numérique, alors {@link #getEntry(int)} est appelée. Sinon, les chiffres qui
     * apparaissent à la fin du nom peuvent être remplacés par les caractères unicodes représentant
     * ces mêmes chiffres sous forme d'indices.
     */
    public Descriptor getEntryLenient(final String name) throws CatalogException, SQLException {
        try {
            return getEntry(name);
        } catch (final NoSuchRecordException exception) {
            /*
             * Aucune entrée n'a été trouvée pour le nom. Essaie comme identifiant numérique.
             * Si l'identifiant est purement numérique mais la recherche échoue pour ce dernier
             * aussi, on ne fera pas d'autres tentatives.
             */
            int identifier = 0;
            try {
                identifier = Integer.parseInt(name);
            } catch (NumberFormatException dummy) {
                /*
                 * L'identifiant n'est pas numérique. Essaie de remplacer les derniers chiffres
                 * par les caractères unicodes correspondant à ces même chiffres en indices.
                 */
                final StringBuilder builder = new StringBuilder(name);
                for (int i=builder.length(); --i>=0;) {
                    final char c = builder.charAt(i);
                    final char n = CharUtilities.toSubScript(c);
                    if (c == n) {
                        break;
                    }
                    builder.setCharAt(i, n);
                }
                String modified = builder.toString();
                if (!modified.equals(name)) try {
                    return getEntry(modified);
                } catch (NoSuchRecordException ignore) {
                    throw exception;
                }
            }
            return getEntry(identifier);
        }
    }

    /**
     * Construit un descripteur pour l'enregistrement courant.
     */
    protected Descriptor createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String    symbol       = results.getString (indexOf(this.symbol      ));
        final int       identifier   = results.getInt    (indexOf(this.identifier  ));
        final String    phenomenon   = results.getString (indexOf(this.phenomenon  ));
        final String    procedure    = results.getString (indexOf(this.procedure   ));
        final String    position     = results.getString (indexOf(this.offset      ));
        final short     band = (short)(results.getShort  (indexOf(this.band        )) - 1);
        final String    distribution = results.getString (indexOf(this.distribution));
        if (offsets == null) {
            offsets = getDatabase().getTable(LocationOffsetTable.class);
        }
        final LocationOffset offset = offsets.getEntry(position);
        if (layer == null) {
            setLayerTable(getDatabase().getTable(LayerTable.class));
        }
        final Layer layer = this.layer.getEntry(phenomenon);
        if (operations == null) {
            operations = getDatabase().getTable(OperationTable.class);
        }
        final Operation operation = operations.getEntry(procedure);
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        final Distribution distributionEntry = distributions.getEntry(distribution);
        return new DescriptorEntry(identifier, symbol, layer, operation, band, offset, distributionEntry, null);
    }
}
