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

// J2SE dependencies
import java.util.Date;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;   // For javadoc

// OpenGIS dependencies
import org.opengis.coverage.SampleDimension;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.image.io.IIOListeners;
import org.geotools.coverage.CoverageStack;
import org.geotools.coverage.GridSampleDimension;  // Pour javadoc
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.util.NumberRange;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Element;
import net.sicade.observation.ConfigurationKey;


/**
 * M�ta-donn�es concernant une image, et �ventuellement une r�f�rence vers l'image elle-m�me.
 * Un objet {@code CoverageReference} permet d'obtenir quelques propri�t�s sur une image telles
 * que sa date et sa couverture g�ographique, sans n�cessiter une connexion � l'image elle-m�me.
 * L'image ne sera t�l�charg�e que la premi�re fois o� elle sera demand�e, lors d'un appel � la
 * m�thode {@link #getCoverage}.
 * <p>
 * Les objets {@code CoverageReference} sont imutables et s�curitaires dans un environnement
 * multi-threads.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface CoverageReference extends Element, CoverageStack.Element {
    /**
     * Cl� pour r�cup�rer le r�pertoire racine des images. La valeur de cette propri�t�
     * peut �tre {@code null} si les fichiers ne sont pas accessibles localement, auquel
     * cas les fichiers devront �tre acc�d�s en utilisant un URL construit � partir de
     * {@link #ROOT_URL}. La valeur par d�faut est {@code null}.
     */
    ConfigurationKey ROOT_DIRECTORY = new ConfigurationKey("RootDirectory", null);

    /**
     * Cl� pour r�cup�rer la racine des images sous forme d'adresse {@code ftp://}.
     * La valeur par d�faut est {@code "ftp://localhost/"}.
     */
    ConfigurationKey ROOT_URL = new ConfigurationKey("RootURL", "ftp://localhost/");

    /**
     * Cl� pour r�cup�rer l'encodage des adresses URL. Des valeurs typiques sont {@code "UTF-8"}
     * et {@code "ISO-8859-1"}. La valeur par d�faut est {@code null}, ce qui signifie qu'aucun
     * encodage ne sera appliqu� sur les adresses URL.
     */
    ConfigurationKey URL_ENCODING = new ConfigurationKey("URL_encoding", null);

    /**
     * Cl� sous laquelle m�moriser l'objet {@code CoverageReference} source dans les propri�t�s de
     * {@link GridCoverage2D}. Cette propri�t� permet de retrouver l'objet {@code CoverageReference}
     * qui a produit un objet {@code GridCoverage2D}. Exemple:
     *
     * <blockquote><pre>
     * CoverageReference reference = ...
     * GridCoverage2D    coverage  = reference.{@linkplain #getCoverage getCoverage}(null);
     * CoverageReference source    = (CoverageReference) coverage.getProperty(CoverageReference.SOURCE_KEY);
     * assert source == reference;
     * </pre></blockquote>
     */
    String SOURCE_KEY = "net.sicade.observation.CoverageReference";

    /**
     * Retourne la s�rie � laquelle appartient cette image.
     */
    Series getSeries();

    /**
     * Retourne le format de cette image.
     */
    Format getFormat();

    /**
     * Retourne le chemin de l'image, ou {@code null} si le fichier n'est pas accessible localement.
     * Dans ce dernier cas, {@link #getURL} devra �tre utilis� � la place.
     */
    File getFile();

    /**
     * Retourne l'URL de l'image, ou {@code null} si le fichier n'est pas accessible ni localement,
     * ni � travers un r�seau.
     */
    URL getURL();

    /**
     * Retourne le syst�me de r�f�rence des coordonn�es de l'image. En g�n�ral, ce syst�me de
     * r�f�rence aura trois dimensions (la derni�re dimension �tant le temps), soit dans l'ordre:
     * <p>
     * <ul>
     *   <li>Les longitudes, en degr�s selon l'ellipso�de WGS 1984.</li>
     *   <li>Les latitudes,  en degr�s selon l'ellipso�de WGS 1984.</li>
     *   <li>Le temps, en jours juliens depuis le 01/01/1950 00:00 UTC.</li>
     * </ul>
     * <p>
     * Bien que toutes les images provenant d'une m�me {@linkplain Series s�rie} ont en
     * g�n�ral le m�me syst�me de r�f�rence des coordonn�es, ce n'est pas toujours le cas.
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Retourne les coordonn�es spatio-temporelles de l'image. Le syst�me de r�f�rence des
     * coordonn�es utilis� est {@linkplain #getCoordinateReferenceSystem celui de l'image}.
     */
    Envelope getEnvelope();

    /**
     * Retourne la plage de temps couverte par l'image, selon les unit�s de l'axe temporel.
     * Cette m�thode est fournit principalement afin de supporter l'interface
     * {@link org.geotools.coverage.CoverageStack.Element}. Pour les autres usage,
     * la m�thode {@link #getTimeRange} peut �tre une alternative plus pratique.
     */
    NumberRange getZRange();

    /**
     * Retourne la plage de temps couverte par l'image. Cette plage sera d�limit�e
     * par des objets {@link Date}. Appeler cette m�thode �quivaut � n'extraire que
     * la partie temporelle de l'{@linkplain #getEnvelope enveloppe} et � transformer
     * les coordonn�es si n�cessaire.
     */
    DateRange getTimeRange();

    /**
     * Retourne les coordonn�es g�ographiques de la r�gion couverte par l'image. Les coordonn�es
     * seront exprim�es en degr�s de longitudes et de latitudes selon l'ellipso�de WGS 1984.
     * Appeler cette m�thode �quivaut parfois � n'extraire que la partie horizontale de
     * l'{@linkplain #getEnvelope enveloppe} et � transformer les coordonn�es si n�cessaire.
     * Toutefois dans certains cas cette m�thode peut retourner une r�gion g�ographique plus
     * grande que l'{@linkplain #getEnvelope enveloppe}, par exemple comme un effet des
     * transformations de coordonn�es ou encore parce que l'image (et par cons�quence son
     * {@linkplain #getEnvelope enveloppe}) sera d�coup�e au moment de la lecture.
     */
    GeographicBoundingBox getGeographicBoundingBox();

    /**
     * Retourne des informations sur la g�om�trie de l'image. Ces informations comprennent notamment
     * la taille de l'image (en pixels) ainsi que la transformation � utiliser pour passer des
     * coordonn�es pixels vers les coordonn�es selon le {@linkplain #getCoordinateReferenceSystem
     * syst�me de r�f�rence de l'image}. Cette transformation sera le plus souvent affine.
     */
    GridGeometry2D getGridGeometry();

    /**
     * Retourne les bandes de l'image. Cette m�thode retourne toujours la version geophysique des
     * bandes (<code>{@linkplain GridSampleDimension#geophysics geophysics}(true)</code>), ce qui
     * est coh�rent avec le type d'image retourn� par {@link #getCoverage getCoverage(...)}.
     *
     * @return La liste des cat�gories g�ophysiques pour chaque bande de l'image.
     *         La longueur de ce tableau sera �gale au nombre de bandes.
     */
    SampleDimension[] getSampleDimensions();

    /**
     * Retourne l'image correspondant � cette entr�e. Cette m�thode retourne toujours la version
     * geophysique de l'image (<code>{@linkplain GridCoverage2D#geophysics geophysics}(true)</code>).
     * <p>
     * Si l'image avait d�j� �t� lue pr�c�demment et qu'elle n'a pas encore �t� r�clam�e par le
     * ramasse-miette, alors l'image existante sera retourn�e sans qu'une nouvelle lecture du
     * fichier ne soit n�cessaire. Si au contraire l'image n'�tait pas d�j� en m�moire, alors
     * un d�codage du fichier sera n�cessaire.
     * <p>
     * Certaines impl�mentations peuvent utiliser en interne les RMI (<cite>Remote Method Invocation</cite>).
     * Dans ce dernier cas, cette m�thode effectuera le d�coupage g�ographique et appliquera d'eventuelles
     * op�rations (par exemple un calcul de gradient) sur le serveur; seul le r�sultat sera envoy� � travers
     * le r�seau vers le client. Il est toutefois possible que la qualit� du r�sultat soit d�grad�e pour une
     * transmission plus compacte sur le r�seau.
     *
     * @param  listeners Liste des objets � informer des progr�s de la lecture ainsi que des
     *         �ventuels avertissements, ou {@code null} s'il n'y en a pas.
     * @return Image lue, ou {@code null} si l'utilisateur a {@linkplain #abort interrompu la lecture}.
     * @throws IOException si le fichier n'a pas �t� trouv� ou si une autre erreur d'entr�s/sorties
     *         est survenue.
     * @throws IIOException s'il n'y a pas de d�codeur appropri� pour l'image, ou si l'image n'est
     *         pas valide.
     * @throws RemoteException si un probl�me est survenu lors de la communication avec le serveur.
     */
    GridCoverage2D getCoverage(IIOListeners listeners) throws IOException;

    /**
     * Annule la lecture de l'image. Cette m�thode peut �tre appel�e � partir de n'importe quel
     * thread.  Si la m�thode {@link #getCoverage getCoverage(...)} �tait en train de lire une
     * image dans un autre thread, elle s'arr�tera et retournera {@code null}.
     */
    void abort();


    /**
     * Une r�f�rence qui d�l�gue son travail � une autre instance de {@link CoverageReference}.
     * L'impl�mentation par d�faut redirige tous les appels des m�thodes vers l'objet {@link
     * CoverageReference} qui a �t� sp�cifi� lors de la construction. Les classes d�riv�es
     * vont typiquement red�finir quelques m�thodes afin d'ajouter ou de modifier certaines
     * fonctionalit�es.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Proxy extends net.sicade.observation.Proxy implements CoverageReference {
        /**
         * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
         */
        private static final long serialVersionUID = 1679051552440633120L;

        /**
         * R�f�rence envelopp�e par ce proxy.
         */
        private final CoverageReference ref;

        /**
         * Construit un proxy qui redirigera tous les appels vers la r�f�rence sp�cifi�e.
         */
        protected Proxy(final CoverageReference ref) {
            this.ref = ref;
            if (ref == null) {
                throw new NullPointerException();
            }
        }

        public CoverageReference         getParent()                                           {return ref;}
        public Series                    getSeries()                    {return ref.getSeries();}
        public Format                    getFormat()                    {return ref.getFormat();}
        public File                      getFile()                      {return ref.getFile();}
        public URL                       getURL()                       {return ref.getURL();}
        public GridGeometry2D            getGridGeometry()              {return ref.getGridGeometry();}
        public CoordinateReferenceSystem getCoordinateReferenceSystem() {return ref.getCoordinateReferenceSystem();}
        public Envelope                  getEnvelope()                  {return ref.getEnvelope();}
        public NumberRange               getZRange()                    {return ref.getZRange();}
        public DateRange                 getTimeRange()                 {return ref.getTimeRange();}
        public GeographicBoundingBox     getGeographicBoundingBox()     {return ref.getGeographicBoundingBox();}
        public SampleDimension[]         getSampleDimensions()          {return ref.getSampleDimensions();}
        public void                      abort()                        {       ref.abort();}
        public GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
            return ref.getCoverage(listeners);
        }
    }
}
