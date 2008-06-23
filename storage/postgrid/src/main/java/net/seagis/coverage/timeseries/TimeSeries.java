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
package net.seagis.coverage.timeseries;

// J2SE dependencies
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;

// OpenGIS dependencies
import org.opengis.geometry.DirectPosition;


/**
 * Représente une série temporelle à une position spatiale fixe. La série sera construite à partir
 * de la base de données d'images.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public final class TimeSeries {
    /**
     * La taille de la {@linkplain #cache}, en nombre d'octèts.
     */
    private static final int CACHE_SIZE = 1024;

    /**
     * La taille (en nombre d'octets) d'une valeur de la série temporelle.
     */
    private static final int DATA_SIZE = Double.SIZE / Byte.SIZE;

    /**
     * Ensemble de séries temporelles à laquelle cette série appartient.
     */
    private final TimeSeriesTile tile;

    /**
     * Coordonnées à laquelle sera évaluée la série temporelle. Cette coordonnées doit
     * contenir une dimension temporelle dont on fera varier l'ordonnée. Cette dimension
     * est spécifiée par {@link TimeSeriesTile#varyingDimension}.
     * <p>
     * Ce champ est accédé en lecture seule par {@link TimeSeriesTile#writeCoordinates}.
     */
    final DirectPosition position;

    /**
     * Position à partir de laquelle cette série temporelle écrira dans {@link TimeSeries#channel}.
     */
    private final long base;

    /**
     * Position à partir de laquelle écrire la {@linkplain #cache} dans le canal.
     * Cette cache n'est qu'une fenêtre sur l'ensemble de la série temporelle.
     */
    private long cacheBase;

    /**
     * La cache dans laquelle écrire les données avant de les envoyer vers le {@linkplain
     * TimeSeriesTile#channel canal}. Ce buffer ne représente qu'une fenêtre sur l'ensemble
     * de la série temporelle que cet objet enregistrera dans le canal.
     */
    private final ByteBuffer cache;

    /**
     * Une vue de la {@linkplain #cache} sous forme de buffer de {@code double}. C'est dans ce
     * buffer que les valeurs de la série temporelle seront enregistrées avant d'être envoyées
     * vers le {@linkplain TimeSeriesTile#channel canal}.
     */
    private final DoubleBuffer buffer;

    /**
     * Construit une série temporelle qui sera évaluée à la position spécifiée.
     *
     * @param tile     La tuile à laquelle appartiendra cette série temporelle.
     * @param position La coordonnées à laquelle évaluée la série temporelle.
     * @param counter  Le numéro de cette série dans {@code tile} (0 pour la première,
     *                 1 pour la seconde, <cite>etc.</cite>).
     */
    TimeSeries(final TimeSeriesTile tile, final DirectPosition position, final int counter) {
        this.tile      = tile;
        this.position  = position;
        this.base      = counter * (long) getSeriesSize();
        this.cacheBase = base;
        this.cache     = ByteBuffer.allocateDirect(CACHE_SIZE).order(ByteOrder.nativeOrder());
        this.buffer    = cache.asDoubleBuffer();
    }

    /**
     * Retourne la taille qu'occupera chaque série temporelle, en nombre d'octets. Cette
     * méthode est utilisée par {@code TimeSeries} afin de se positionner dans le fichier.
     */
    private int getSeriesSize() {
        return tile.getSeriesLength() * DATA_SIZE;
    }

    /**
     * Ajoute une valeur à cette série temporelle pour l'instant spécifié.
     * Cette méthode est appelée par {@link TimeSeriesTile} pour toute les
     * séries temporelles comprises dans une tuile.
     */
    final void evaluate(final double t) throws IOException {
        assert Thread.holdsLock(tile);
        position.setOrdinate(tile.varyingDimension, t);
        buffer.put(tile.evaluate(position));
        if (!buffer.hasRemaining()) {
            assert buffer.position() * DATA_SIZE == CACHE_SIZE : buffer;
            flush();
        }
    }

    /**
     * Envoie vers le canal toutes les données qui étaient en attente d'écriture.
     */
    final void flush() throws IOException {
        assert Thread.holdsLock(tile);
        cache.limit(buffer.position() * DATA_SIZE);
        while (cache.hasRemaining()) {
            tile.channel.write(cache, cacheBase);
        }
        cacheBase += cache.position();
        assert cacheBase <= base + getSeriesSize() : cacheBase;
        cache .clear();
        buffer.clear();
    }

    /**
     * Vérifie que le tableau spécifié à une taille compatible avec la taille du buffer.
     *
     * @param  data Le tableau qui contient ou contiendra les données de la séries temporelle.
     * @param  size La taille totale <strong>en octets</strong> de la série temporelle.
     * @throws IllegalArgumentException si le tableau n'a pas la longueur attendue.
     */
    private static void checkDataSize(final double[] data, final int size) throws IllegalArgumentException {
        if (data != null && data.length*DATA_SIZE != size) {
            throw new IllegalArgumentException("La taille du tableau est incompatible avec celle de la série.");
        }
    }

    /**
     * Retourne toutes les données de cette séries temporelle. Si l'argument {@code data} est
     * non-nul, alors les données seront écrites dans ce tableau.
     * <p>
     * Chaque valeur {@code data[i]} correspond au temps <var>t</var> retourné par
     * <code>tile.{@link TimeSeriesTile#getTime getTime}(i)</code>.
     *
     * @param  data Un tableau pré-alloué dans lequel placer les données, ou {@code null} si aucun.
     * @return Les données de la séries temporelle dans {@code data}, ou dans un nouveau tableau si
     *         {@code data} était nul.
     * @throws IOException si les données n'ont pas pu être récupérées à partir du disque.
     */
    public double[] getData(double[] data) throws IOException {
        final int size = getSeriesSize();
        checkDataSize(data, size);
        if (data == null) {
            data = new double[size / DATA_SIZE];
        }
        final DoubleBuffer transfert = DoubleBuffer.wrap(data);
        synchronized (tile) {
            final FileChannel channel = tile.channel.position(base);
            int limit;
            while ((limit = (transfert.remaining() * DATA_SIZE)) != 0) {
                buffer.clear();
                cache .clear();
                if (limit < cache.limit()) {
                    cache.limit(limit);
                }
                if (channel.read(cache) < 0) {
                    throw new EOFException();
                }
                buffer.limit(cache.position() / DATA_SIZE);
                transfert.put(buffer);
            }
            assert tile.channel.position() == base + size;
        }
        return data;
    }

    /**
     * Modifie toutes les données de cette séries temporelle. Cette méthode est typiquement
     * appellée après qu'un calcul aie été effectué sur la série temporelle, par exemple un
     * filtre passe-bas.
     *
     * @param  data Les nouvelles données de la séries temporelle.
     * @throws IOException si les données n'ont pas pu être écrites sur le disque.
     */
    public void setData(final double[] data) throws IOException {
        final int size = getSeriesSize();
        checkDataSize(data, size);
        int offset = 0;
        synchronized (tile) {
            final FileChannel channel = tile.channel.position(base);
            while (offset < data.length) {
                cache.clear();
                buffer.clear();
                buffer.put(data, offset, Math.min(data.length-offset, buffer.capacity()));
                cache.limit(buffer.position() * DATA_SIZE);
                offset += channel.write(cache);
            }
            assert tile.channel.position() == base + size;
        }
    }

    /**
     * Positionne le buffer sur la première valeur de la série temporelle. Cette méthode doit
     * être appelée une fois avant de procéder aux appels successifs de {@link #next}.
     * <p>
     * <strong>NOTE:</strong> Cette méthode (ainsi que {@link #next}) ne devrait jamais être
     * appelée avant que la construction de la série temporelle n'aie été complétée par tous
     * les appels nécessaires à {@link #evaluate}.
     */
    final void rewind() throws IOException {
        assert Thread.holdsLock(tile);
        cacheBase = base;
        buffer.clear().position(buffer.limit()); // Forcera next() à remplir le buffer à partir du fichier.
    }

    /**
     * Retourne la valeur suivante. Cette méthode est appelée afin de construire les images
     * à partir des valeurs de toutes les séries temporelles à un temps <var>t</var>.
     */
    final double next() throws IOException {
        assert Thread.holdsLock(tile);
        while (!buffer.hasRemaining()) {
            final int remaning = getSeriesSize() - (int)(cacheBase - base);
            if (remaning == 0) {
                // Cette situation ne devrait pas se produire. Si elle se produit néanmois,
                // on laissera buffer.get() à la fin de cette méthode lancer son exception.
                break;
            }
            buffer.clear();
            cache.clear();
            if (remaning < cache.limit()) {
                cache.limit(remaning);
            }
            if (tile.channel.read(cache, cacheBase) < 0) {
                throw new EOFException();
            }
            buffer.limit(cache.position() / DATA_SIZE);
            cacheBase += cache.position();
        }
        return buffer.get();
    }
}
