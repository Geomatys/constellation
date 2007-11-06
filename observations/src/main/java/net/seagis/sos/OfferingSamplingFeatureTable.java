package net.seagis.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.observation.SamplingFeatureEntry;
import net.seagis.observation.SamplingFeatureTable;
import net.seagis.observation.SamplingPointEntry;
import net.seagis.observation.SamplingPointTable;
import org.geotools.resources.Utilities;

/**
 *
 * @author legal
 */
public class OfferingSamplingFeatureTable extends SingletonTable<OfferingSamplingFeatureEntry> {

    /**
     * identifiant secondaire de la table.
     */
    private String idOffering;
    /**
     * un lien vers la table des sampling feature.
     */
    private SamplingFeatureTable samplingFeatures;
    /**
     * un lien vers la table des sampling point.
     */
    private SamplingPointTable samplingPoints;

    /**
     * Construit une table des phenomene composé.
     *
     * @param  database Connexion vers la base de données.
     */
    public OfferingSamplingFeatureTable(final Database database) {
        this(new OfferingSamplingFeatureQuery(database));
    }

    /**
     * Construit une nouvelle table non partagée
     */
    public OfferingSamplingFeatureTable(final OfferingSamplingFeatureTable table) {
        super(table);
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private OfferingSamplingFeatureTable(final OfferingSamplingFeatureQuery query) {
        super(query);
        setIdentifierParameters(query.bySamplingFeature, null);
    }

    @Override
    protected OfferingSamplingFeatureEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingSamplingFeatureQuery query = (OfferingSamplingFeatureQuery) super.query;
        SamplingFeatureEntry samplingFeature;
        
        if (results.getString(indexOf(query.samplingFeature)) != null) {
            if (samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(SamplingFeatureTable.class);
            }
            samplingFeature = samplingFeatures.getEntry(results.getString(indexOf(query.samplingFeature)));
        } else {
          if (samplingPoints == null) {
                samplingPoints = getDatabase().getTable(SamplingPointTable.class);
            }
            samplingFeature = samplingPoints.getEntry(results.getString(indexOf(query.samplingPoint)));
          
        }

        return new OfferingSamplingFeatureEntry(results.getString(indexOf(query.idOffering)), samplingFeature);
    }

    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        final OfferingSamplingFeatureQuery query = (OfferingSamplingFeatureQuery) super.query;
        if (!type.equals(QueryType.INSERT)) {
            statement.setString(indexOf(query.byOffering), idOffering);
        }
    }

    public String getIdOffering() {
        return idOffering;
    }

    public void setIdOffering(String idOffering) {
        if (!Utilities.equals(this.idOffering, idOffering)) {
            this.idOffering = idOffering;
            fireStateChanged("idOffering");
        }
    }
    
     /**
     * Insere un nouveau capteur a un offering dans la base de donnée.
     *
     */
    public synchronized void getIdentifier(OfferingSamplingFeatureEntry offSamplingFeature) throws SQLException, CatalogException {
        final OfferingSamplingFeatureQuery query  = (OfferingSamplingFeatureQuery) super.query;
        String idSF = "";
        
        PreparedStatement statement = getStatement(QueryType.EXISTS);
        statement.setString(indexOf(query.idOffering), offSamplingFeature.getIdOffering());
        if (offSamplingFeature.getComponent() instanceof SamplingPointEntry) {
             if (samplingPoints == null) {
                samplingPoints = getDatabase().getTable(SamplingPointTable.class);
            }
            idSF = samplingPoints.getIdentifier((SamplingPointEntry)offSamplingFeature.getComponent());
            statement.setString(indexOf(query.samplingPoint), idSF);
            statement.setNull(indexOf(query.samplingFeature), java.sql.Types.VARCHAR);
        } else if (offSamplingFeature.getComponent() instanceof SamplingFeatureEntry) {
            if ( samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(SamplingFeatureTable.class);
            }
            idSF = samplingFeatures.getIdentifier(offSamplingFeature.getComponent());
            statement.setString(indexOf(query.samplingFeature), idSF);
            statement.setNull(indexOf(query.samplingPoint), java.sql.Types.VARCHAR);
            
        } 
        ResultSet result = statement.executeQuery();
        if(result.next()) {
            return;
        }
        PreparedStatement insert    = getStatement(QueryType.INSERT);
        insert.setString(indexOf(query.idOffering), offSamplingFeature.getIdOffering());
        if (offSamplingFeature.getComponent() instanceof SamplingPointEntry) {
             if (samplingPoints == null) {
                samplingPoints = getDatabase().getTable(SamplingPointTable.class);
            }
            idSF = samplingPoints.getIdentifier((SamplingPointEntry)offSamplingFeature.getComponent());
            insert.setString(indexOf(query.samplingPoint), idSF);
            insert.setNull(indexOf(query.samplingFeature), java.sql.Types.VARCHAR);
        } else if (offSamplingFeature.getComponent() instanceof SamplingFeatureEntry) {
            if ( samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(SamplingFeatureTable.class);
            }
            idSF = samplingFeatures.getIdentifier(offSamplingFeature.getComponent());
            insert.setString(indexOf(query.samplingFeature), idSF);
            insert.setNull(indexOf(query.samplingPoint), java.sql.Types.VARCHAR);
        }
        insertSingleton(insert);
    }
}