/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.map;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;
import org.geotools.coverage.io.CoverageReader;
import org.geotools.coverage.wi.WorldImageFactory;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.ElevationModel;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.MapBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.RandomStyleFactory;
import org.geotools.style.StyleConstants;
import org.geotools.style.StyleFactory;
import org.geotools.style.function.InterpolationPoint;
import org.geotools.style.function.Method;
import org.geotools.style.function.Mode;
import org.geotools.style.function.ThreshholdsBelongTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.ContrastMethod;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;

/**
 *
 * @author sorel
 */
public class PostGRIDContextBuilder {

    public static final MapBuilder MAP_BUILDER = MapBuilder.getInstance();
    public static final StyleFactory SF = CommonFactoryFinder.getStyleFactory(null);
    public static final RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();

    public static MapContext buildPostGridContext() {
      
        MapContext context = null;
        MapLayer layer = null;

        try {
            context = MAP_BUILDER.createContext(DefaultGeographicCRS.WGS84);
            layer = createPostGridLayer2();
//            addElevationModel(layer);
            context.layers().add(layer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return context;
    }

    public static MapLayer createPostGridLayer() throws IOException, CatalogException, SQLException {
        Database database = null;
        LayerTable layers = null;
        Layer selectedLayer = null;

        database = new Database();
        layers = database.getTable(LayerTable.class);

        Set<Layer> entries = layers.getEntries();
        for (Layer lay : entries) {
            System.out.println(lay.getName());
        }
//        selectedLayer = layers.getEntry("SPOT5_Guyane_Panchro");
//        selectedLayer = layers.getEntry("BlueMarble");
        selectedLayer = layers.getEntry("AO_Coriolis_(Temp)");
//        selectedLayer = layers.getEntry("Mars3D_Gascogne_(UZ-VZ)");

        final PostGridReader reader = new PostGridReader(database,selectedLayer);

        PostGridMapLayer layer = new PostGridMapLayer(reader);
        layer.setStyle(createCategorizeStyle());
//        layer.graphicBuilders().add(new GridMarkGraphicBuilder());

        return layer;
    }

    public static MapLayer createPostGridLayer2() throws IOException, CatalogException, SQLException {
        Database database = null;
        LayerTable layers = null;
        Layer selectedLayer = null;

        database = new Database();
        layers = database.getTable(LayerTable.class);

        Set<Layer> entries = layers.getEntries();
        for (Layer lay : entries) {
            System.out.println(lay.getName());
        }
//        selectedLayer = layers.getEntry("SPOT5_Guyane_Panchro");
//        selectedLayer = layers.getEntry("UNESCO");
        selectedLayer = layers.getEntry("Ortho2000");
//        selectedLayer = layers.getEntry("BlueMarble");
//        selectedLayer = layers.getEntry("AO_Coriolis_(Temp)");
//        selectedLayer = layers.getEntry("Mars3D_Gascogne_(UZ-VZ)");


//        final CoverageReference ref = selectedLayer.getCoverageReference();
//        final CoordinateReferenceSystem crs = ref.getCoordinateReferenceSystem();
//        final int nbdim = crs.getCoordinateSystem().getDimension();
//
//        for(int i=0 ; i<nbdim ; i++){
//            CoordinateSystemAxis axi = crs.getCoordinateSystem().getAxis(i);
//            System.out.println(axi.getUnit());
//        }
//
//        final SampleDimension[] dims = ref.getSampleDimensions();
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>");
//        System.out.println(dims.length);
//        for(final SampleDimension dim : dims){
//            System.out.println(dim.getDescription());
//            System.out.println(dim.getUnits());
//        }
//
//        System.out.println(crs);

        final PostGridReader reader = new PostGridReader(database,selectedLayer);

        PostGridMapLayer2 layer = new PostGridMapLayer2(reader);
        layer.setStyle(createRasterStyle());
//        layer.graphicBuilders().add(new GridMarkGraphicBuilder());

        return layer;
    }

    public static MutableStyle createInterpolationStyle() {

        final List<InterpolationPoint> values = new ArrayList<InterpolationPoint>();
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.WHITE), 0));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.BLUE), 3000));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.YELLOW), 22000));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.GREEN), 23000));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.YELLOW), 24000));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.RED), 33000));
        final Literal lookup = StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
        final Literal fallback = StyleConstants.DEFAULT_FALLBACK;
        final Function interpolateFunction = SF.createInterpolateFunction(
                lookup, values, Method.COLOR, Mode.LINEAR, fallback);

        final ChannelSelection selection = SF.createChannelSelection(
                SF.createSelectedChannelType("0", SF.literalExpression(1)));

        Expression opacity = SF.literalExpression(1f);
        OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        ColorMap colorMap = SF.createColorMap(interpolateFunction);
        ContrastEnhancement enchance = StyleConstants.DEFAULT_CONTRAST_ENHANCEMENT;
        ShadedRelief relief = StyleConstants.DEFAULT_SHADED_RELIEF;
        Symbolizer outline = null; //createRealWorldLineSymbolizer();
        Unit uom = NonSI.FOOT;
        String geom = StyleConstants.DEFAULT_GEOM;
        String name = "raster symbol name";
        Description desc = StyleConstants.DEFAULT_DESCRIPTION;

        RasterSymbolizer symbol = SF.createRasterSymbolizer(opacity, selection, overlap, colorMap, enchance, relief, outline, uom, geom, name, desc);

        return SF.createStyle(symbol);
    }

    public static MutableStyle createHistoStyle() {

        final List<InterpolationPoint> values = new ArrayList<InterpolationPoint>();
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.WHITE), -10));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.BLUE), 0));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.YELLOW), 19));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.GREEN), 20));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.YELLOW), 21));
        values.add(SF.createInterpolationPoint(SF.colorExpression(Color.RED), 30));
        final Literal lookup = StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
        final Literal fallback = StyleConstants.DEFAULT_FALLBACK;
        final Function interpolateFunction = SF.createInterpolateFunction(
                lookup, values, Method.COLOR, Mode.LINEAR, fallback);

        final ChannelSelection selection = SF.createChannelSelection(
                SF.createSelectedChannelType("0", SF.literalExpression(1)));

        Expression opacity = SF.literalExpression(1f);
        OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        ColorMap colorMap = SF.createColorMap(interpolateFunction);
        ContrastEnhancement enchance = SF.createContrastEnhancement(ContrastMethod.HISTOGRAM, SF.literalExpression(1.8f));
//        ContrastEnhancement enchance = StyleConstants.DEFAULT_CONTRAST_ENHANCEMENT;
        ShadedRelief relief = StyleConstants.DEFAULT_SHADED_RELIEF;
        Symbolizer outline = null; //createRealWorldLineSymbolizer();
        Unit uom = NonSI.FOOT;
        String geom = StyleConstants.DEFAULT_GEOM;
        String name = "raster symbol name";
        Description desc = StyleConstants.DEFAULT_DESCRIPTION;

        RasterSymbolizer symbol = SF.createRasterSymbolizer(opacity, selection, overlap, colorMap, enchance, relief, outline, uom, geom, name, desc);

        return SF.createStyle(symbol);
    }



    public static MutableStyle createCategorizeStyle() {

        final Map<Expression,Expression> values = new HashMap<Expression,Expression>();
        values.put(StyleConstants.CATEGORIZE_LESS_INFINITY, SF.colorExpression(Color.WHITE));
        values.put(SF.literalExpression(3000), SF.colorExpression(Color.BLUE));
        values.put(SF.literalExpression(22000), SF.colorExpression(Color.YELLOW));
        values.put(SF.literalExpression(23000), SF.colorExpression(Color.GREEN));
        values.put(SF.literalExpression(24000), SF.colorExpression(Color.YELLOW));
        values.put(SF.literalExpression(26000), SF.colorExpression(Color.RED));
        final Literal lookup = StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
        final Literal fallback = StyleConstants.DEFAULT_FALLBACK;
        final Function categorizeFunction = SF.createCategorizeFunction(
                lookup, values, ThreshholdsBelongTo.PRECEDING, fallback);

        final ChannelSelection selection = SF.createChannelSelection(
                SF.createSelectedChannelType("0", SF.literalExpression(1)));


        Expression opacity = SF.literalExpression(1f);
        OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        ColorMap colorMap = SF.createColorMap(categorizeFunction);
        ContrastEnhancement enchance = StyleConstants.DEFAULT_CONTRAST_ENHANCEMENT;
        ShadedRelief relief = StyleConstants.DEFAULT_SHADED_RELIEF;
        Symbolizer outline = null; //createRealWorldLineSymbolizer();
        Unit uom = NonSI.FOOT;
        String geom = StyleConstants.DEFAULT_GEOM;
        String name = "raster symbol name";
        Description desc = StyleConstants.DEFAULT_DESCRIPTION;

        RasterSymbolizer symbol = SF.createRasterSymbolizer(opacity, selection, overlap, colorMap, enchance, relief, outline, uom, geom, name, desc);

        return SF.createStyle(symbol);
    }


    public static MutableStyle createRasterStyle(){

//        final List<InterpolationPoint> values = new ArrayList<InterpolationPoint>();
//        values.add( SF.createInterpolationPoint(SF.colorExpression(Color.BLACK), 0));
//        values.add( SF.createInterpolationPoint(SF.colorExpression(Color.BLUE), 30));
//        values.add( SF.createInterpolationPoint(SF.colorExpression(new Color(0,150,0)), 100));
//        values.add( SF.createInterpolationPoint(SF.colorExpression(new Color(100,50,50)), 200));
//        values.add( SF.createInterpolationPoint(SF.colorExpression(Color.WHITE), 250));
//        final Literal lookup = StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
//        final Literal fallback = StyleConstants.DEFAULT_FALLBACK;
//        final Function function = SF.createInterpolateFunction(
//                lookup, values, Method.COLOR, Mode.LINEAR, fallback);
//
        final ChannelSelection selection = StyleConstants.DEFAULT_RASTER_CHANNEL_RGB;

//        final SelectedChannelType sctR = SF.createSelectedChannelType("0", SF.literalExpression(1));
//        final SelectedChannelType sctG = SF.createSelectedChannelType("2", SF.literalExpression(1));
//        final SelectedChannelType sctB = SF.createSelectedChannelType("1", SF.literalExpression(1));
//        final ChannelSelection selection = SF.createChannelSelection( new SelectedChannelType[]{
//            sctR,
//            sctG,
//            sctB
//        });

//        final ChannelSelection selection = SF.createChannelSelection( SF.createSelectedChannelType("1", SF.literalExpression(1)) );

        Expression opacity = SF.literalExpression(1f);
        OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
//        ColorMap colorMap = SF.createColorMap(function);
        ColorMap colorMap = null;
        ContrastEnhancement enchance = SF.createContrastEnhancement(ContrastMethod.NONE, SF.literalExpression(1f));
        ShadedRelief relief = StyleConstants.DEFAULT_SHADED_RELIEF;
        Symbolizer outline = null; //createRealWorldLineSymbolizer();
        Unit uom = NonSI.PIXEL;
        String geom = StyleConstants.DEFAULT_GEOM;
        String name = "raster symbol name";
        Description desc = StyleConstants.DEFAULT_DESCRIPTION;

        RasterSymbolizer symbol = SF.createRasterSymbolizer(opacity, selection, overlap, colorMap, enchance, relief, outline, uom, geom, name, desc);

        return SF.createStyle(symbol);
    }

    private static void addElevationModel(MapLayer layer) {
//        final CoverageReader reader =  buildMNTReader();
        final CoverageReader reader =  buildSRTMReader();
        ElevationModel elevation = MAP_BUILDER.createElevationModel(reader,null);
        layer.setElevationModel(elevation);
    }


    private static CoverageReader buildSRTMReader(){

        try{
            Database database = null;
            LayerTable layers = null;
            Layer selectedLayer = null;

            database = new Database();
            layers = database.getTable(LayerTable.class);

            selectedLayer = layers.getEntry("SRTM");

            return new PostGridReader(database,selectedLayer);
        }catch(Exception ex){
            ex.printStackTrace();

        }
        return null;
    }

    private static CoverageReader buildMNTReader(){

        CoverageReader cover = null;
        try {
            File gridFile;
            
            gridFile = new File("/home/sorel/GIS_DATA/mnt/16_bit_dem_large.tif");
            try {
                cover = readWorldImage(gridFile);
            } catch (DataSourceException ex) {
                ex.printStackTrace();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cover;
    }

    public static CoverageReader readWorldImage( File gridFile ) throws IOException, NoninvertibleTransformException{
       WorldImageFactory factory = new WorldImageFactory();
       return factory.createMosaicReader(gridFile);
    }

}
