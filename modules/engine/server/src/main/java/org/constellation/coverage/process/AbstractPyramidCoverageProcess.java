package org.constellation.coverage.process;

import org.apache.sis.measure.NumberRange;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactoryType;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.coverage.AbstractCoverageStoreFactory;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidSet;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.filestore.FileCoverageStoreFactory;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.xmlstore.XMLCoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStore;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.internal.coverage.CoverageUtilities;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.OutOfDomainOfValidityException;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.DOMAIN;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.IN_COVERAGE_REF;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.ORIGINAL_DATA;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PROVIDER_OUT_ID;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_CRS;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_DATASET;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_FOLDER;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_NAME;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AbstractPyramidCoverageProcess extends AbstractCstlProcess {
    
    @Autowired
    private DomainRepository domainRepository;
    
    @Autowired
    protected IDataBusiness dataBusiness;

    @Autowired
    protected IProviderBusiness providerBusiness;

    protected static final String PNG_FORMAT = "PNG";
    protected static final String TIFF_FORMAT = "TIFF";
    protected static final int TILE_SIZE = 256;

    public AbstractPyramidCoverageProcess(ProcessDescriptor desc, ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    protected static void fillParameters(CoverageReference inCoverageRef, Data orinigalData, String pyramidName,
                                         String providerID, File pyramidFolder, Domain domain, Dataset dataset,
                                         CoordinateReferenceSystem[] pyramidCRS, ParameterValueGroup params) {
        getOrCreate(IN_COVERAGE_REF, params).setValue(inCoverageRef);
        getOrCreate(ORIGINAL_DATA, params).setValue(orinigalData);
        getOrCreate(PYRAMID_NAME, params).setValue(pyramidName);
        getOrCreate(PROVIDER_OUT_ID, params).setValue(providerID);
        getOrCreate(PYRAMID_FOLDER, params).setValue(pyramidFolder);
        getOrCreate(DOMAIN, params).setValue(domain);
        getOrCreate(PYRAMID_DATASET, params).setValue(dataset);
        getOrCreate(PYRAMID_CRS, params).setValue(pyramidCRS);
    }


    protected CoverageStore getOrCreateXMLCoverageStore(final File pyramidFolder)
            throws DataStoreException, ProcessException, MalformedURLException {

        final ParameterValueGroup storeParams = XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
        getOrCreate(XMLCoverageStoreFactory.NAMESPACE, storeParams).setValue("no namespace");
        getOrCreate(XMLCoverageStoreFactory.PATH, storeParams).setValue(pyramidFolder.toURI().toURL());
        getOrCreate(XMLCoverageStoreFactory.CACHE_TILE_STATE, storeParams).setValue(true);

        final XMLCoverageStore store = (XMLCoverageStore) CoverageStoreFinder.open(storeParams);
        if (store == null) {
            throw new ProcessException("Can't initialize XMLCoverageStore.", this, null);
        }

        return store;
    }

    protected CoverageReference getOrCreateCRef(XMLCoverageStore coverageStore, Name coverageName, String tileFormat, ViewType type)
            throws DataStoreException {
        CoverageReference cv = null;
        for (Name n : coverageStore.getNames()) {
            if (n.getLocalPart().equals(coverageName.getLocalPart())) {
                cv = coverageStore.getCoverageReference(n);
            }
        }
        if (cv == null) {
            cv = coverageStore.create(coverageName, type, tileFormat);
        }
        return cv;
    }

    /**
     * Compute bounds of pyramid CRS
     * @return
     */
    protected Envelope getPyramidWorldEnvelope(CoordinateReferenceSystem crs){
        return CRS.getEnvelope(crs);
    }

    /**
     * Re-use previous pyramid scales or compute new scales from input coverage.
     * @param inputCoverage
     * @param pyramidRef
     * @return scales array
     * @throws DataStoreException
     * @throws FactoryException
     */
    protected double[] getPyramidScales(GridCoverage2D inputCoverage, PyramidalCoverageReference pyramidRef,
                                        CoordinateReferenceSystem pyramidCRS)
            throws DataStoreException, FactoryException, TransformException, OutOfDomainOfValidityException {

        final PyramidSet pyramidSet = pyramidRef.getPyramidSet();
        final Collection<Pyramid> pyramids = pyramidSet.getPyramids();

        Pyramid pyramid = null;
        for (Pyramid p : pyramids) {
            if (CRS.equalsIgnoreMetadata(p.getCoordinateReferenceSystem(), pyramidCRS)) {
                pyramid = p;
                break;
            }
        }
        double[] scales;
        if (pyramid != null) {
            scales = pyramid.getScales();
        } else {
            scales = computeScales(inputCoverage, pyramidCRS);
        }
        return scales;
    }

    private double[] computeScales(GridCoverage2D coverage, final CoordinateReferenceSystem pyramidCRS)
            throws TransformException, OutOfDomainOfValidityException {
        final Envelope env = getPyramidWorldEnvelope(pyramidCRS);
        final double spanX = env.getSpan(0);

        final GridGeometry2D gg = coverage.getGridGeometry();
        final Envelope covEnv   = CRS.transform(gg.getEnvelope(), env.getCoordinateReferenceSystem());
        
        
        final double baseScale = covEnv.getSpan(0) / gg.getExtent2D().getSpan(0);
        double scale = spanX / TILE_SIZE;
        
        return (double[]) CoverageUtilities.toWellKnownScale(env, new NumberRange(Double.class, baseScale, true, scale, true)).getValue();
        
        
        /*calculate scales
        
        double[] scales = new double[0];
        while (true) {
            if (scale <= baseScale) {
                //fit to exact match to preserve base quality.
                scale = baseScale;
            }
            scales = ArraysExt.insert(scales, scales.length, 1);
            scales[scales.length - 1] = scale;

            if (scale <= baseScale) {
                break;
            }
            scale = scale / 2;
        }
        return scales;*/
    }

    /**
     * Create provider and data and link them to already existing dataset.
     *
     * @param providerID
     * @param store
     * @param domainId
     * @param datasetId
     * @return new provider
     * @throws ProcessException
     */
    protected DataProvider createProvider(final String providerID, CoverageStore store, final Integer domainId,
                                          final Integer datasetId) throws ProcessException {
        final DataProvider outProvider;
        try {
            //get store configuration
            final ParameterValueGroup storeConf = store.getConfiguration();
            final String namespace = value(AbstractCoverageStoreFactory.NAMESPACE, storeConf);
            final URL pyramidFolder = value(XMLCoverageStoreFactory.PATH, storeConf);

            //create provider configuration
            final String factoryName = ProviderFactoryType.COVERAGE_STORE.getType();
            final DataProviderFactory factory = DataProviders.getInstance().getFactory(factoryName);
            final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(providerID);
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue(factoryName);
            final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());

            final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidFolder);
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue(namespace);
            outProvider = DataProviders.getInstance().createProvider(providerID, factory, pparams, datasetId);

            if (domainId != null) {
                SpringHelper.executeInTransaction(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        int count = domainRepository.addProviderDataToDomain(providerID, domainId);
                        LOGGER.info("Added " + count + " data to domain " + domainId);
                    }
                });
            }

        } catch (Exception ex) {
            throw new ProcessException("Failed to create pyramid provider "+ex.getMessage(), this, ex);
        }
        return outProvider;
    }
}
