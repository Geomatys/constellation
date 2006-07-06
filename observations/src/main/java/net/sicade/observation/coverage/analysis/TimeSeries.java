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
package net.sicade.observation.coverage.analysis;

// J2SE dependencies
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;

// OpenGIS dependencies
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * Repr�sente une s�rie temporelle � une position spatiale fixe. La s�rie sera construite � partir
 * de la base de donn�es d'images.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public final class TimeSeries {
    /**
     * La taille de la {@linkplain #cache}, en nombre d'oct�ts.
     */
    private static final int CACHE_SIZE = 1024;

    /**
     * La taille (en nombre d'octets) d'une valeur de la s�rie temporelle.
     */
    private static final int DATA_SIZE = Double.SIZE / Byte.SIZE;

    /**
     * Ensemble de s�ries temporelles � laquelle cette s�rie appartient.
     */
    private final TimeSeriesTile tile;

    /**
     * Coordonn�es � laquelle sera �valu�e la s�rie temporelle. Cette coordonn�es doit
     * contenir une dimension temporelle dont on fera varier l'ordonn�e. Cette dimension
     * est sp�cifi�e par {@link TimeSeriesTile#varyingDimension}.
     * <p>
     * Ce champ est acc�d� en lecture seule par {@link TimeSeriesTile#writeCoordinates}.
     */
    final DirectPosition position;

    /**
     * Position � partir de laquelle cette s�rie temporelle �crira dans {@link TimeSeries#channel}.
     */
    private final long base;

    /**
     * Position � partir de laquelle �crire la {@linkplain #cache} dans le canal.
     * Cette cache n'est qu'une fen�tre sur l'ensemble de la s�rie temporelle.
     */
    private long cacheBase;

    /**
     * La cache dans laquelle �crire les donn�es avant de les envoyer vers le {@linkplain
     * TimeSeriesTile#channel canal}. Ce buffer ne repr�sente qu'une fen�tre sur l'ensemble
     * de la s�rie temporelle que cet objet enregistrera dans le canal.
     */
    private final ByteBuffer cache;

    /**
     * Une vue de la {@linkplain #cache} sous forme de buffer de {@code double}. C'est dans ce
     * buffer que les valeurs de la s�rie temporelle seront enregistr�es avant d'�tre envoy�es
     * vers le {@linkplain TimeSeriesTile#channel canal}.
     */
    private final DoubleBuffer buffer;

    /**
     * Construit une s�rie temporelle qui sera �valu�e � la position sp�cifi�e.
     *
     * @param tile     La tuile � laquelle appartiendra cette s�rie temporelle.
     * @param position La coordonn�es � laquelle �valu�e la s�rie temporelle.
     * @param counter  Le num�ro de cette s�rie dans {@code tile} (0 pour la premi�re,
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
     * Retourne la taille qu'occupera chaque s�rie temporelle, en nombre d'octets. Cette
     * m�thode est utilis�e par {@code TimeSeries} afin de se positionner dans le fichier.
     */
    private int getSeriesSize() {
        return tile.getSeriesLength() * DATA_SIZE;
    }

    /**
     * Ajoute une valeur � cette s�rie temporelle pour l'instant sp�cifi�.
     * Cette m�thode est appel�e par {@link TimeSeriesTile} pour toute les
     * s�ries temporelles comprises dans une tuile.
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
     * Envoie vers le canal toutes les donn�es qui �taient en attente d'�criture.
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
     * V�rifie que le tableau sp�cifi� � une taille compatible avec la taille du buffer.
     *
     * @param  data Le tableau qui contient ou contiendra les donn�es de la s�ries temporelle.
     * @param  size La taille totale <strong>en octets</strong> de la s�rie temporelle.
     * @throws IllegalArgumentException si le tableau n'a pas la longueur attendue.
     */
    private static void checkDataSize(final double[] data, final int size) throws IllegalArgumentException {
        if (data != null && data.length*DATA_SIZE != size) {
            throw new IllegalArgumentException("La taille du tableau est incompatible avec celle de la s�rie.");
        }
    }

    /**
     * Retourne toutes les donn�es de cette s�ries temporelle. Si l'argument {@code data} est
     * non-nul, alors les donn�es seront �crites dans ce tableau.
     * <p>
     * Chaque valeur {@code data[i]} correspond au temps <var>t</var> retourn� par
     * <code>tile.{@link TimeSeriesTile#getTime getTime}(i)</code>.
     *
     * @param  data Un tableau pr�-allou� dans lequel placer les donn�es, ou {@code null} si aucun.
     * @return Les donn�es de la s�ries temporelle dans {@code data}, ou dans un nouveau tableau si
     *         {@code data} �tait nul.
     * @throws IOException si les donn�es n'ont pas pu �tre r�cup�r�es � partir du disque.
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
     * Modifie toutes les donn�es de cette s�ries temporelle. Cette m�thode est typiquement
     * appell�e apr�s qu'un calcul aie �t� effectu� sur la s�rie temporelle, par exemple un
     * filtre passe-bas.
     *
     * @param  data Les nouvelles donn�es de la s�ries temporelle.
     * @throws IOException si les donn�es n'ont pas pu �tre �crites sur le disque.
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
     * Positionne le buffer sur la premi�re valeur de la s�rie temporelle. Cette m�thode doit
     * �tre appel�e une fois avant de proc�der aux appels successifs de {@link #next}.
     * <p>
     * <strong>NOTE:</strong> Cette m�thode (ainsi que {@link #next}) ne devrait jamais �tre
     * appel�e avant que la construction de la s�rie temporelle n'aie �t� compl�t�e par tous
     * les appels n�cessaires � {@link #evaluate}.
     */
    final void rewind() throws IOException {
        assert Thread.holdsLock(tile);
        cacheBase = base;
        buffer.clear().position(buffer.limit()); // Forcera next() � remplir le buffer � partir du fichier.
    }

    /**
     * Retourne la valeur suivante. Cette m�thode est appel�e afin de construire les images
     * � partir des valeurs de toutes les s�ries temporelles � un temps <var>t</var>.
     */
    final double next() throws IOException {
        assert Thread.holdsLock(tile);
        while (!buffer.hasRemaining()) {
            final int remaning = getSeriesSize() - (int)(cacheBase - base);
            if (remaning == 0) {
                // Cette situation ne devrait pas se produire. Si elle se produit n�anmois,
                // on laissera buffer.get() � la fin de cette m�thode lancer son exception.
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
