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
import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import static java.lang.Math.min;
import static java.lang.Math.max;

// OpenGIS dependencies
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.CRSUtilities;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Element;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;


/**
 * Classe de base des tables dans lesquelles chaque enregistrement est un singleton compris dans
 * certaines limites spatio-temporelles. En plus des méthodes abstraites définies dans la classe
 * parente, les implémentations de cette classe devraient redéfinir les méthodes suivantes:
 * <p>
 * <ul>
 *   <li>{@link #getQuery} (héritée de la classe parente, mais avec de nouvelles conditions)
 *       pour retourner l'instruction SQL à utiliser pour obtenir les données à partir de son
 *       nom ou ID. Utilisée aussi ainsi pour obtenir la requête SQL à utiliser pour obtenir
 *       les coordonnées spatio-temporelles des enregistrements.</li>
 * </ul>
 * <p>
 * Les limites spatio-temporelles sont définies par la propriété {@link #getEnvelope Envelope}.
 * Les propriétés {@link #getGeographicBoundingBox GeographicBoundingBox} et {@link #getTimeRange
 * TimeRange} peuvent être considérées comme les composantes spatiale et temporelle de l'envelope,
 * transformées selon un système de référence fixe par commodité.
 * <p>
 * La méthode {@link #trimEnvelope} permet de réduire l'envelope au minimum tout en englobant les
 * mêmes enregistrements.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class BoundedSingletonTable<E extends Element> extends SingletonTable<E> {
    /**
     * Fin des données à explorer, spécifiée comme intervalle de temps par rapport
     * à la date courante.
     */
    private static final long LOOK_AHEAD = 30 * 24 * 60 * 60 * 1000L;

    /**
     * Type de système de référence des coordonnées utilisé.
     */
    private final CRS crsType;

    /**
     * Plage de temps des enregistrements à extraire.
     */
    private long tMin, tMax;

    /**
     * Coordonnées géographiques des enregistrements à extraire. La plage de longitudes est plus
     * grande que nécessaire (±360° au lieu de ±180°) cas on ne sait pas à priori si la plage de
     * longitudes utilisée va de -180 à +180° ou de 0 à 360°.
     */
    private double xMin, xMax, yMin, yMax;

    /**
     * {@code true} si la méthode {@link #ensureTrimmed} a déjà réduit l'{@linkplain #getEnvelope
     * enveloppe spatio-temporelle} de cette table.
     */
    private boolean trimmed;

    /**
     * {@code true} si l'utilisateur a appellé {@link #trimEnvelope}. Dans ce cas, la méthode
     * {@link #ensureTrimmed} devra réduire l'{@linkplain #getEnvelope enveloppe spatio-temporelle}
     * la prochaine fois où elle sera appellée.
     */
    private boolean trimRequested;

    /**
     * La transformation allant du système de référence des coordonnées {@link #crsType} vers
     * le système {@link #getCoordinateReferenceSystem}. Ne sera construit que la première fois
     * où il sera nécessaire.
     */
    private transient MathTransform standardToUser;

    /**
     * Construit une table pour la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
     * @param  crsType  Type de système de référence des coordonnées utilisé.
     */
    protected BoundedSingletonTable(final Database database, final CRS crsType) {
        super(database);
        this.crsType = crsType;
        tMin =  0;
        tMax =  System.currentTimeMillis() + LOOK_AHEAD;
        xMin = -360;
        xMax = +360;
        yMin =  -90;
        yMax =  +90;
    }

    /**
     * Construit une nouvelle table initialisée à la même couverture spatio-temporelle que la
     * table spécifiée. Ce constructeur suppose que le {@linkplain #getCoordinateReferenceSystem
     * système de référence des coordonnées} restera le même pour les deux tables.
     */
    protected BoundedSingletonTable(final BoundedSingletonTable table) {
        this(table.database, table.crsType);
        tMin           = table.tMin;
        tMax           = table.tMax;
        xMin           = table.xMin;
        xMax           = table.xMax;
        yMin           = table.yMin;
        yMax           = table.yMax;
        trimmed        = table.trimmed;
        trimRequested  = table.trimRequested;
        standardToUser = table.standardToUser;
    }

    /**
     * Retourne le système de référence des coordonnées utilisé pour les coordonnées spatio-temporelles
     * de {@code [get|set]Envelope}. L'implémentation par défaut retourne le système de référence
     * correspondant au type spécifié au constructeur. Ce système peut comprendre les dimensions
     * suivantes:
     * <p>
     * <ul>
     *   <li>La longitude en degrés relatif au méridien de Greenwich</li>
     *   <li>La latitude en degrés</li>
     *   <li>L'altitude en mètres au dessus de l'ellipsoïde WGS 84</li>
     *   <li>Le temps en nombre de jours écoulés depuis l'epoch.</li>
     * </ul>
     * <p>
     * Ces coordonnées ne sont pas nécessairement toutes présentes; cela dépend de l'énumération
     * utilisée. Par exemple le système désigné par {@link CRS#XYT} ne comprend pas l'altitude.
     * Mais les coordonnées présentes seront toujours dans cet ordre.
     * <p>
     * Les classes dérivées peuvent retourner un autre système de référence, mais ce système doit
     * être compatible avec le type spécifié au constructeur (c'est-à-dire qu'une transformation
     * de coordonnées doit exister entre les deux systèmes) et ne doit jamais changer pour une
     * instance donnée de cette classe.
     *
     * @see CRS
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crsType.getCoordinateReferenceSystem();
    }

    /**
     * Retourne la transformation allant du système de référence des coordonnées {@link #crsType}
     * vers le système {@link #getCoordinateReferenceSystem}, ou {@code null} si cette
     * transformation est la transformation identitée.
     *
     * @throws CatalogException si la transformation n'a pas pu être construite.
     */
    private MathTransform getStandardToUser() throws CatalogException {
        assert Thread.holdsLock(this);
        final CoordinateReferenceSystem sourceCRS = crsType.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem targetCRS =    this.getCoordinateReferenceSystem();
        if (!CRSUtilities.equalsIgnoreMetadata(sourceCRS, targetCRS) && standardToUser!=null) try {
            standardToUser = FactoryFinder.getCoordinateOperationFactory(FACTORY_HINTS)
                             .createOperation(sourceCRS, targetCRS).getMathTransform();
        } catch (FactoryException exception) {
            throw new ServerException(exception);
        }
        return standardToUser;
    }

    /**
     * Retourne les coordonnées spatio-temporelles de la région d'intérêt. Le système de référence
     * des coordonnées utilisé est celui retourné par {@link #getCoordinateReferenceSystem}.
     * L'implémentation par défaut construit une envelope à partir des informations retournées par
     * {@link #getGeographicBoundingBox} et {@link #getTimeRange}, en transformant les coordonnées
     * si nécessaire.
     *
     * @throws CatalogException si une erreur est survenue lors de l'obtention de l'enveloppe ou
     *         de la transformation des coordonnnées.
     *
     * @see #getGeographicBoundingBox
     * @see #getTimeRange
     * @see #trimEnvelope
     */
    public synchronized Envelope getEnvelope() throws CatalogException {
        final DateRange            time = getTimeRange();
        final GeographicBoundingBox box = getGeographicBoundingBox();
        GeneralEnvelope envelope = new GeneralEnvelope(crsType.getCoordinateReferenceSystem());
        envelope.setRange(CRS.X_DIMENSION, box.getWestBoundLongitude(), box.getEastBoundLongitude());
        envelope.setRange(CRS.Y_DIMENSION, box.getSouthBoundLatitude(), box.getNorthBoundLatitude());
        if (crsType.T_DIMENSION >= 0) {
            envelope.setRange(crsType.T_DIMENSION, CRS.TEMPORAL.toValue((Date) time.getMinValue()),
                                                   CRS.TEMPORAL.toValue((Date) time.getMaxValue()));
        }
        final MathTransform standardToUser = getStandardToUser();
        if (standardToUser != null) try {
            envelope = CRSUtilities.transform(standardToUser, envelope);
        } catch (TransformException exception) {
            throw new ServerException(exception);
        }
        return envelope;
    }

    /**
     * Définit les coordonnées spatio-temporelles de la région d'intérêt. Le système de référence
     * des coordonnées utilisé est celui retourné par {@link #getCoordinateReferenceSystem}.
     * Appeler cette méthode équivaut à effectuer les transformations nécessaires des coordonnées,
     * puis appeler {@link #setTimeRange setTimeRange(...)} et
     * {@link #setGeographicBoundingBox setGeographicBoundingBox(...)}.
     *
     * @throws CatalogException si une erreur est survenue lors de la transformation des coordonnnées.
     */
    public synchronized void setEnvelope(Envelope envelope) throws CatalogException {
        final MathTransform standardToUser = getStandardToUser();
        if (standardToUser != null) try {
            envelope = CRSUtilities.transform(standardToUser.inverse(), envelope);
        } catch (TransformException exception) {
            throw new ServerException(exception);
        }
        setGeographicBoundingBox(new GeographicBoundingBoxImpl(
                                        envelope.getMinimum(CRS.X_DIMENSION),
                                        envelope.getMaximum(CRS.X_DIMENSION),
                                        envelope.getMinimum(CRS.Y_DIMENSION),
                                        envelope.getMaximum(CRS.Y_DIMENSION)));
        if (crsType.T_DIMENSION >= 0) {
            setTimeRange(CRS.TEMPORAL.toDate(envelope.getMinimum(crsType.T_DIMENSION)),
                         CRS.TEMPORAL.toDate(envelope.getMaximum(crsType.T_DIMENSION)));
        }
    }

    /**
     * Retourne les coordonnées géographiques englobeant les enregistrements. Cette région ne sera
     * pas plus grande que la région qui a été spécifiée lors du dernier appel de la méthode
     * {@link #setGeographicBoundingBox setGeographicBoundingBox(...)}. Elle peut toutefois être
     * plus petite si la méthode {@link #trimEnvelope} a été appelée depuis.
     *
     * @return La région géographique des enregistrements recherchés par cette table.
     *
     * @see #getTimeRange
     * @see #getEnvelope
     * @see #trimEnvelope
     *
     * @throws CatalogException si l'enveloppe n'a pas pu être obtenue.
     */
    public synchronized GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        try {
            ensureTrimmed();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return new GeographicBoundingBoxImpl(xMin, xMax, yMin, yMax);
    }

    /**
     * Définit les coordonnées géographiques de la région dans laquelle on veut rechercher des
     * enregistrements. Les coordonnées doivent être exprimées en degrés de longitude et de latitude
     * selon l'ellipsoïde WGS&nbsp;1984. Tous les enregistrements qui interceptent cette région
     * seront prises en compte lors du prochain appel de {@link #getEntries}.
     *
     * @param  area Coordonnées géographiques de la région, en degrés de longitude et de latitude.
     * @return {@code true} si la région d'intérêt à changée, ou {@code false} si les valeurs
     *         spécifiées étaient les mêmes que la dernière fois.
     */
    public synchronized boolean setGeographicBoundingBox(final GeographicBoundingBox area) {
        boolean change;
        change  = (xMin != (xMin = area.getWestBoundLongitude()));
        change |= (xMax != (xMax = area.getEastBoundLongitude()));
        change |= (yMin != (yMin = area.getSouthBoundLatitude()));
        change |= (yMax != (yMax = area.getNorthBoundLatitude()));
        trimRequested = false;
        if (change) {
            trimmed = false;
            fireStateChanged("GeographicBoundingBox");
        }
        return change;
    }

    /**
     * Retourne la plage de dates des enregistrements. Cette plage de dates ne sera pas plus grande
     * que la plage de dates spécifiée lors du dernier appel de la méthode {@link #setTimeRange
     * setTimeRange(...)}.  Elle peut toutefois être plus petite si la méthode {@link #trimEnvelope}
     * a été appelée depuis.
     *
     * @return La plage de dates des enregistrements. Cette plage sera constituée d'objets {@link Date}.
     *
     * @see #getGeographicBoundingBox
     * @see #getEnvelope
     * @see #trimEnvelope
     *
     * @throws CatalogException si l'enveloppe n'a pas pu être obtenue.
     */
    public synchronized DateRange getTimeRange() throws CatalogException {
        try {
            ensureTrimmed();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return new DateRange(new Date(tMin), new Date(tMax));
    }

    /**
     * Définit la plage de dates dans laquelle rechercher des enregistrements. Tous les
     * enregistrements qui interceptent cette plage de temps seront pris en compte lors
     * du prochain appel de {@link #getEntries}.
     *
     * @param  timeRange Plage de dates dans laquelle rechercher des enregistrements.
     *         Cette plage doit être constituée d'objets {@link Date}.
     * @return {@code true} si la plage de temps à changée, ou {@code false} si les valeurs
     *         spécifiées étaient les mêmes que la dernière fois.
     */
    public final boolean setTimeRange(final DateRange timeRange) {
        Date startTime = timeRange.getMinValue();
        Date   endTime = timeRange.getMaxValue();
        if (!timeRange.isMinIncluded()) {
            startTime = new Date(startTime.getTime()+1);
        }
        if (!timeRange.isMaxIncluded()) {
            endTime = new Date(endTime.getTime()-1);
        }
        return setTimeRange(startTime, endTime);
    }

    /**
     * Définit la plage de dates dans laquelle rechercher des enregistrements. Tous les
     * enregistrements qui interceptent cette plage de temps seront pris en compte lors
     * du prochain appel de {@link #getEntries}.
     *
     * @param  startTime Date de début (inclusive) de la période d'intérêt.
     * @param  endTime   Date de fin   (inclusive) de la période d'intérêt.
     * @return {@code true} si la plage de temps à changée, ou {@code false} si les valeurs
     *         spécifiées étaient les mêmes que la dernière fois.
     */
    public synchronized boolean setTimeRange(final Date startTime, final Date endTime) {
        boolean change;
        change  = (tMin != (tMin = startTime.getTime()));
        change |= (tMax != (tMax =   endTime.getTime()));
        trimRequested = false;
        if (change) {
            trimmed = false;
            fireStateChanged("TimeRange");
        }
        return change;
    }

    /**
     * Réduit l'{@linkplain #getEnvelope envelope spatio-temporelle} à la plus petite envelope
     * englobant les enregistrements de cette table.  Cette méthode ne prend en compte que les
     * enregistrements trouvés dans l'envelope définie lors des appels précédents aux méthodes
     * {@code setXXX(...)}. Les valeurs retournées par les méthodes {@code getXXX(...)} seront
     * modifiées de façon à définir la plus petite enveloppe contenant les mêmes enregistrements.
     * <p>
     * L'implémentation par défaut utilise la requête retournée par
     * <code>getQuery({@linkplain QueryType#BOUNDING_BOX BOUNDING_BOX})</code>
     */
    public synchronized void trimEnvelope() {
        trimRequested = true;
    }

    /**
     * Procède à la réduction de l'enveloppe, si elle a été demandée par l'utilisateur
     * (par un appel à {@link #trimEnvelope}) et que cette réduction n'a pas encore été
     * effectuée. Cette méthode est appelée automatiquement par {@link #getGeographicBoundingBox}
     * et {@link #getTimeRange}.
     *
     * @throws SQLException si l'accès à la base de données a échoué.
     */
    private void ensureTrimmed() throws SQLException {
        assert Thread.holdsLock(this);
        if (trimRequested && !trimmed) {
            final PreparedStatement statement = getStatement(QueryType.BOUNDING_BOX);
            if (statement != null) {
                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    Date time;
                    final Calendar calendar = getCalendar();
                    time = result.getTimestamp(1, calendar);
                    if (time != null) {
                        tMin = max(tMin, time.getTime());
                    }
                    time = result.getTimestamp(2, calendar);
                    if (time != null) {
                        tMax = min(tMax, time.getTime());
                    }
                    double v;
                    v=result.getDouble(3); if (!result.wasNull() && v>xMin) xMin = v;
                    v=result.getDouble(4); if (!result.wasNull() && v<xMax) xMax = v;
                    v=result.getDouble(5); if (!result.wasNull() && v>yMin) yMin = v;
                    v=result.getDouble(6); if (!result.wasNull() && v<yMax) yMax = v;
                }
                result.close();
                fireStateChanged("Envelope");
            }
            trimmed = true;
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction des limites spatio-temporelles définies
     * dans cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: {
                if (false) {
                    // Activer cette ligne si on soupçonne que ça change les résultats.
                    // En théorie, ça ne devrait rien changer.
                    ensureTrimmed();
                }
                // Fall through
            }
            case BOUNDING_BOX: {
                final Calendar calendar = getCalendar();
                statement.setTimestamp(1, new Timestamp(tMin), calendar);
                statement.setTimestamp(2, new Timestamp(tMax), calendar);
                statement.setDouble   (3, xMin);
                statement.setDouble   (4, xMax);
                statement.setDouble   (5, yMin);
                statement.setDouble   (6, yMax);
                break;
            }
        }
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les données. Les requêtes retournées par
     * cette méthode doivent répondre aux mêmes conditions que celles qui sont stipulées dans la
     * {@linkplain SingletonTable#getQuery classe parente}, avec l'extension suivante:
     * <p>
     * <ul>
     *   <li><p>Dans le cas particulier ou {@code type} est {@link QueryType#LIST LIST}, la requête
     *       doit attendre les arguments suivants:</p>
     *       <ul>
     *         <li>La date de départ</li>
     *         <li>La date de fin</li>
     *         <li>La longitude minimale (ouest)</li>
     *         <li>La longitude maximale (est)</li>
     *         <li>La latitude minimale (sud)</li>
     *         <li>La latitude maximale (nord)</li>
     *       </ul>
     *   </li>
     *   <li><p>Dans le cas particulier ou {@code type} est {@link QueryType#BOUNDING_BOX BOUNDING_BOX},
     *       les mêmes arguments que la requête {@link QueryType#LIST LIST} sont attendues. Les valeurs
     *       retournées doivent être l'envelope spatio-temporelle d'une région toujours dans le même
     *       ordre que précédemment.</p></li>
     * </ul>
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case BOUNDING_BOX: return null;
            default: return super.getQuery(type);
        }
    }
}
