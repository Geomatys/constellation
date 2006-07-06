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
import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import static java.lang.Math.min;
import static java.lang.Math.max;

// OpenGIS dependencies
import org.opengis.spatialschema.geometry.Envelope;
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
 * certaines limites spatio-temporelles. En plus des m�thodes abstraites d�finies dans la classe
 * parente, les impl�mentations de cette classe devraient red�finir les m�thodes suivantes:
 * <p>
 * <ul>
 *   <li>{@link #getQuery} (h�rit�e de la classe parente, mais avec de nouvelles conditions)
 *       pour retourner l'instruction SQL � utiliser pour obtenir les donn�es � partir de son
 *       nom ou ID. Utilis�e aussi ainsi pour obtenir la requ�te SQL � utiliser pour obtenir
 *       les coordonn�es spatio-temporelles des enregistrements.</li>
 * </ul>
 * <p>
 * Les limites spatio-temporelles sont d�finies par la propri�t� {@link #getEnvelope Envelope}.
 * Les propri�t�s {@link #getGeographicBoundingBox GeographicBoundingBox} et {@link #getTimeRange
 * TimeRange} peuvent �tre consid�r�es comme les composantes spatiale et temporelle de l'envelope,
 * transform�es selon un syst�me de r�f�rence fixe par commodit�.
 * <p>
 * La m�thode {@link #trimEnvelope} permet de r�duire l'envelope au minimum tout en englobant les
 * m�mes enregistrements.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class BoundedSingletonTable<E extends Element> extends SingletonTable<E> {
    /**
     * Fin des donn�es � explorer, sp�cifi�e comme intervalle de temps par rapport
     * � la date courante.
     */
    private static final long LOOK_AHEAD = 30 * 24 * 60 * 60 * 1000L;

    /**
     * Type de syst�me de r�f�rence des coordonn�es utilis�.
     */
    private final CRS crsType;

    /**
     * Plage de temps des enregistrements � extraire.
     */
    private long tMin, tMax;

    /**
     * Coordonn�es g�ographiques des enregistrements � extraire. La plage de longitudes est plus
     * grande que n�cessaire (�360� au lieu de �180�) cas on ne sait pas � priori si la plage de
     * longitudes utilis�e va de -180 � +180� ou de 0 � 360�.
     */
    private double xMin, xMax, yMin, yMax;

    /**
     * {@code true} si la m�thode {@link #ensureTrimmed} a d�j� r�duit l'{@linkplain #getEnvelope
     * enveloppe spatio-temporelle} de cette table.
     */
    private boolean trimmed;

    /**
     * {@code true} si l'utilisateur a appell� {@link #trimEnvelope}. Dans ce cas, la m�thode
     * {@link #ensureTrimmed} devra r�duire l'{@linkplain #getEnvelope enveloppe spatio-temporelle}
     * la prochaine fois o� elle sera appell�e.
     */
    private boolean trimRequested;

    /**
     * La transformation allant du syst�me de r�f�rence des coordonn�es {@link #crsType} vers
     * le syst�me {@link #getCoordinateReferenceSystem}. Ne sera construit que la premi�re fois
     * o� il sera n�cessaire.
     */
    private transient MathTransform standardToUser;

    /**
     * Construit une table pour la connexion sp�cifi�e.
     *
     * @param  database Connexion vers la base de donn�es d'observations.
     * @param  crsType  Type de syst�me de r�f�rence des coordonn�es utilis�.
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
     * Construit une nouvelle table initialis�e � la m�me couverture spatio-temporelle que la
     * table sp�cifi�e. Ce constructeur suppose que le {@linkplain #getCoordinateReferenceSystem
     * syst�me de r�f�rence des coordonn�es} restera le m�me pour les deux tables.
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
     * Retourne le syst�me de r�f�rence des coordonn�es utilis� pour les coordonn�es spatio-temporelles
     * de {@code [get|set]Envelope}. L'impl�mentation par d�faut retourne le syst�me de r�f�rence
     * correspondant au type sp�cifi� au constructeur. Ce syst�me peut comprendre les dimensions
     * suivantes:
     * <p>
     * <ul>
     *   <li>La longitude en degr�s relatif au m�ridien de Greenwich</li>
     *   <li>La latitude en degr�s</li>
     *   <li>L'altitude en m�tres au dessus de l'ellipso�de WGS 84</li>
     *   <li>Le temps en nombre de jours �coul�s depuis l'epoch.</li>
     * </ul>
     * <p>
     * Ces coordonn�es ne sont pas n�cessairement toutes pr�sentes; cela d�pend de l'�num�ration
     * utilis�e. Par exemple le syst�me d�sign� par {@link CRS#XYT} ne comprend pas l'altitude.
     * Mais les coordonn�es pr�sentes seront toujours dans cet ordre.
     * <p>
     * Les classes d�riv�es peuvent retourner un autre syst�me de r�f�rence, mais ce syst�me doit
     * �tre compatible avec le type sp�cifi� au constructeur (c'est-�-dire qu'une transformation
     * de coordonn�es doit exister entre les deux syst�mes) et ne doit jamais changer pour une
     * instance donn�e de cette classe.
     *
     * @see CRS
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crsType.getCoordinateReferenceSystem();
    }

    /**
     * Retourne la transformation allant du syst�me de r�f�rence des coordonn�es {@link #crsType}
     * vers le syst�me {@link #getCoordinateReferenceSystem}, ou {@code null} si cette
     * transformation est la transformation identit�e.
     *
     * @throws CatalogException si la transformation n'a pas pu �tre construite.
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
     * Retourne les coordonn�es spatio-temporelles de la r�gion d'int�r�t. Le syst�me de r�f�rence
     * des coordonn�es utilis� est celui retourn� par {@link #getCoordinateReferenceSystem}.
     * L'impl�mentation par d�faut construit une envelope � partir des informations retourn�es par
     * {@link #getGeographicBoundingBox} et {@link #getTimeRange}, en transformant les coordonn�es
     * si n�cessaire.
     *
     * @throws CatalogException si une erreur est survenue lors de l'obtention de l'enveloppe ou
     *         de la transformation des coordonnn�es.
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
     * D�finit les coordonn�es spatio-temporelles de la r�gion d'int�r�t. Le syst�me de r�f�rence
     * des coordonn�es utilis� est celui retourn� par {@link #getCoordinateReferenceSystem}.
     * Appeler cette m�thode �quivaut � effectuer les transformations n�cessaires des coordonn�es,
     * puis appeler {@link #setTimeRange setTimeRange(...)} et
     * {@link #setGeographicBoundingBox setGeographicBoundingBox(...)}.
     *
     * @throws CatalogException si une erreur est survenue lors de la transformation des coordonnn�es.
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
     * Retourne les coordonn�es g�ographiques englobeant les enregistrements. Cette r�gion ne sera
     * pas plus grande que la r�gion qui a �t� sp�cifi�e lors du dernier appel de la m�thode
     * {@link #setGeographicBoundingBox setGeographicBoundingBox(...)}. Elle peut toutefois �tre
     * plus petite si la m�thode {@link #trimEnvelope} a �t� appel�e depuis.
     *
     * @return La r�gion g�ographique des enregistrements recherch�s par cette table.
     *
     * @see #getTimeRange
     * @see #getEnvelope
     * @see #trimEnvelope
     *
     * @throws CatalogException si l'enveloppe n'a pas pu �tre obtenue.
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
     * D�finit les coordonn�es g�ographiques de la r�gion dans laquelle on veut rechercher des
     * enregistrements. Les coordonn�es doivent �tre exprim�es en degr�s de longitude et de latitude
     * selon l'ellipso�de WGS&nbsp;1984. Tous les enregistrements qui interceptent cette r�gion
     * seront prises en compte lors du prochain appel de {@link #getEntries}.
     *
     * @param  area Coordonn�es g�ographiques de la r�gion, en degr�s de longitude et de latitude.
     * @return {@code true} si la r�gion d'int�r�t � chang�e, ou {@code false} si les valeurs
     *         sp�cifi�es �taient les m�mes que la derni�re fois.
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
     * que la plage de dates sp�cifi�e lors du dernier appel de la m�thode {@link #setTimeRange
     * setTimeRange(...)}.  Elle peut toutefois �tre plus petite si la m�thode {@link #trimEnvelope}
     * a �t� appel�e depuis.
     *
     * @return La plage de dates des enregistrements. Cette plage sera constitu�e d'objets {@link Date}.
     *
     * @see #getGeographicBoundingBox
     * @see #getEnvelope
     * @see #trimEnvelope
     *
     * @throws CatalogException si l'enveloppe n'a pas pu �tre obtenue.
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
     * D�finit la plage de dates dans laquelle rechercher des enregistrements. Tous les
     * enregistrements qui interceptent cette plage de temps seront pris en compte lors
     * du prochain appel de {@link #getEntries}.
     *
     * @param  timeRange Plage de dates dans laquelle rechercher des enregistrements.
     *         Cette plage doit �tre constitu�e d'objets {@link Date}.
     * @return {@code true} si la plage de temps � chang�e, ou {@code false} si les valeurs
     *         sp�cifi�es �taient les m�mes que la derni�re fois.
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
     * D�finit la plage de dates dans laquelle rechercher des enregistrements. Tous les
     * enregistrements qui interceptent cette plage de temps seront pris en compte lors
     * du prochain appel de {@link #getEntries}.
     *
     * @param  startTime Date de d�but (inclusive) de la p�riode d'int�r�t.
     * @param  endTime   Date de fin   (inclusive) de la p�riode d'int�r�t.
     * @return {@code true} si la plage de temps � chang�e, ou {@code false} si les valeurs
     *         sp�cifi�es �taient les m�mes que la derni�re fois.
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
     * R�duit l'{@linkplain #getEnvelope envelope spatio-temporelle} � la plus petite envelope
     * englobant les enregistrements de cette table.  Cette m�thode ne prend en compte que les
     * enregistrements trouv�s dans l'envelope d�finie lors des appels pr�c�dents aux m�thodes
     * {@code setXXX(...)}. Les valeurs retourn�es par les m�thodes {@code getXXX(...)} seront
     * modifi�es de fa�on � d�finir la plus petite enveloppe contenant les m�mes enregistrements.
     * <p>
     * L'impl�mentation par d�faut utilise la requ�te retourn�e par
     * <code>getQuery({@linkplain QueryType#BOUNDING_BOX BOUNDING_BOX})</code>
     */
    public synchronized void trimEnvelope() {
        trimRequested = true;
    }

    /**
     * Proc�de � la r�duction de l'enveloppe, si elle a �t� demand�e par l'utilisateur
     * (par un appel � {@link #trimEnvelope}) et que cette r�duction n'a pas encore �t�
     * effectu�e. Cette m�thode est appel�e automatiquement par {@link #getGeographicBoundingBox}
     * et {@link #getTimeRange}.
     *
     * @throws SQLException si l'acc�s � la base de donn�es a �chou�.
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
     * Configure la requ�te SQL sp�cifi�e en fonction des limites spatio-temporelles d�finies
     * dans cette table. Cette m�thode est appel�e automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged chang� d'�tat}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: {
                if (false) {
                    // Activer cette ligne si on soup�onne que �a change les r�sultats.
                    // En th�orie, �a ne devrait rien changer.
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
     * Retourne la requ�te SQL � utiliser pour obtenir les donn�es. Les requ�tes retourn�es par
     * cette m�thode doivent r�pondre aux m�mes conditions que celles qui sont stipul�es dans la
     * {@linkplain SingletonTable#getQuery classe parente}, avec l'extension suivante:
     * <p>
     * <ul>
     *   <li><p>Dans le cas particulier ou {@code type} est {@link QueryType#LIST LIST}, la requ�te
     *       doit attendre les arguments suivants:</p>
     *       <ul>
     *         <li>La date de d�part</li>
     *         <li>La date de fin</li>
     *         <li>La longitude minimale (ouest)</li>
     *         <li>La longitude maximale (est)</li>
     *         <li>La latitude minimale (sud)</li>
     *         <li>La latitude maximale (nord)</li>
     *       </ul>
     *   </li>
     *   <li><p>Dans le cas particulier ou {@code type} est {@link QueryType#BOUNDING_BOX BOUNDING_BOX},
     *       les m�mes arguments que la requ�te {@link QueryType#LIST LIST} sont attendues. Les valeurs
     *       retourn�es doivent �tre l'envelope spatio-temporelle d'une r�gion toujours dans le m�me
     *       ordre que pr�c�demment.</p></li>
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
