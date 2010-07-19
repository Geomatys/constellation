
package org.constellation.tile.ws;

import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.wmts.xml.v100.TileMatrix;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultTileExample {

    public static CodeType tileMatrixSetIdentifier = new CodeType("Ortho200");

    public static String supportedCRS         = "urn:ogc:def:crs:OGC:1.3:CRS84";

    public static int tileWidth;
    
    public static int tileHeight;



    // for each level of the pyramid
    
    private double scaleDenominator;
    private List<Double> topLeftCorner;
    private int matrixWidth;
    private int matrixHeight;

    public static List<TileMatrix> getTileMatrix() {
        final List<TileMatrix> result = new ArrayList<TileMatrix>();
        return result;
    }

    
}
