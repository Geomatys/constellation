/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.catalog;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.awt.image.RenderedImage;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;
import static net.seagis.coverage.model.CoverageBuilder.FACTORY;


/**
 * Un décodeur d'image qui sera exécuté sur un serveur distant. Une instance de {@code RemoteLoader} est
 * référencée par chaque {@link GridCoverageEntry} qui a été {@linkplain GridCoverageEntry#export exporté}
 * comme service RMI. Lors de la serialization du {@code GridCoverageEntry} dans le contexte des RMI, la
 * référence vers {@code RemoteLoader} sera automatiquement remplacée par une connexion vers le serveur
 * d'origine.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RemoteLoader extends UnicastRemoteObject implements CoverageLoader {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -2228058795492073485L;

    /**
     * Décodeur d'images sur lequel déléguer le travail.
     */
    private final CoverageLoader loader;

    /**
     * Construit une instance de {@code RemoteLoader} qui délèguera sont travail au décodeur
     * spécifié.
     */
    public RemoteLoader(final CoverageLoader loader) throws RemoteException {
        this.loader = loader;
    }

    /**
     * Procède à la lecture de l'image. Cette méthode ne retourne pas directement l'objet lu,
     * mais l'enveloppe plutôt dans un nouveau {@link GridCoverage2D} qui ne conservera aucune
     * référence vers ses sources. Si son image est le résultat d'une chaîne d'opération, seul
     * le résultat sera conservée plutôt que la chaîne. Enfin, seule la version non-géophysique
     * de l'image sera envoyée sur le réseau pour un transfert plus rapide. Il en résulte une
     * image qui peut être de qualité dégradée par rapport à une image qui aurait été générée
     * localement.
     *
     * @todo Essayer d'éviter d'appeller {@code geophysics(false)} si le format "naturel"
     *       des données est géophysique. Les tests effectuées pour l'instant provoquent
     *       un EOFException lors de la deserialisation.
     */
    public GridCoverage2D getCoverage() throws IOException {
        GridCoverage2D coverage = loader.getCoverage();
        coverage = coverage.geophysics(false);
        RenderedImage image = coverage.getRenderedImage();
        if (image instanceof RenderedOp) {
            image = ((RenderedOp) image).getRendering();
        }
        coverage = FACTORY.create(coverage.getName(), image,
                                  coverage.getCoordinateReferenceSystem(),
                                  coverage.getGridGeometry().getGridToCoordinateSystem(),
                                  coverage.getSampleDimensions(),
                                  null, null);
        return coverage;
    }
}
