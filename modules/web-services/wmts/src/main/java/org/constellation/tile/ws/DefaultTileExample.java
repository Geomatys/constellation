
package org.constellation.tile.ws;

import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.wmts.xml.v100.TileMatrix;
import org.geotoolkit.wmts.xml.v100.TileMatrixSet;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultTileExample {

    public final static TileMatrixSet BLUEMARBLE;
    static {
        BLUEMARBLE = new TileMatrixSet(new CodeType("BlueMarble"), "urn:ogc:def:crs:OGC:1.3:CRS84");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L13", 1,   1,  480, 240, 0.75,                -180, 90));
        tm.add(createTileMatrix("L12", 2,   1,  480, 480, 0.375,               -180, 90));
        tm.add(createTileMatrix("L11", 4,   2,  480, 480, 0.1875,              -180, 90));
        tm.add(createTileMatrix("L10", 6,   3,  480, 480, 0.125,               -180, 90));
        tm.add(createTileMatrix("L9",  10,  5,  480, 480, 0.075,               -180, 90));
        tm.add(createTileMatrix("L8",  12,  6,  480, 480, 0.0625,              -180, 90));
        tm.add(createTileMatrix("L7",  18,  9,  480, 480, 0.0416666666666667,  -180, 90));
        tm.add(createTileMatrix("L6",  20,  10, 480, 480, 0.0375,              -180, 90));
        tm.add(createTileMatrix("L5",  30,  15, 480, 480, 0.025,               -180, 90));
        tm.add(createTileMatrix("L4",  36,  18, 480, 480, 0.0208333333333333,  -180, 90));
        tm.add(createTileMatrix("L3",  60,  30, 480, 480, 0.0125,              -180, 90));
        tm.add(createTileMatrix("L2",  90,  45, 480, 480, 0.00833333333333333, -180, 90));
        tm.add(createTileMatrix("L1",  180, 90, 480, 480, 0.00416666666666667, -180, 90));
        BLUEMARBLE.setTileMatrix(tm);
    }

    public final static TileMatrixSet ORTHO_THAU;
    static {
        ORTHO_THAU = new TileMatrixSet(new CodeType("OrthoThau"), "urn:ogc:def:crs:EPSG:27582");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L9", 1,   1,   273, 312, 128, 690000.25,1844999.75));
        tm.add(createTileMatrix("L8", 2,   2,   500, 500, 64,  690000.25,1844999.75));
        tm.add(createTileMatrix("L7", 3,   3,   500, 500, 32,  690000.25,1844999.75));
        tm.add(createTileMatrix("L6", 5,   5,   500, 500, 16,  690000.25,1844999.75));
        tm.add(createTileMatrix("L5", 9,   10,  500, 500, 8,   690000.25,1844999.75));
        tm.add(createTileMatrix("L4", 18,  20,  500, 500, 4,   690000.25,1844999.75));
        tm.add(createTileMatrix("L3", 35,  40,  500, 500, 2,   690000.25,1844999.75));
        tm.add(createTileMatrix("L2", 70,  80,  500, 500, 1,   690000.25,1844999.75));
        tm.add(createTileMatrix("L1", 140, 160, 500, 500, 0.5, 690000.25,1844999.75));
        ORTHO_THAU.setTileMatrix(tm);
    }

    public final static TileMatrixSet THAU_SCAN_250_IGN;
    static {
        THAU_SCAN_250_IGN = new TileMatrixSet(new CodeType("ThauScan250IGN"), "urn:ogc:def:crs:EPSG:27582");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L6", 1,  1,  384,  284, 800, 507812.5,1910887.5));
        tm.add(createTileMatrix("L5", 2,  2,  512,  512, 400, 507812.5,1910887.5));
        tm.add(createTileMatrix("L4", 4,  3,  512,  512, 200, 507812.5,1910887.5));
        tm.add(createTileMatrix("L3", 7,  5,  512,  512, 100, 507812.5,1910887.5));
        tm.add(createTileMatrix("L2", 13, 9,  512,  512, 50,  507812.5,1910887.5));
        tm.add(createTileMatrix("L1", 25, 18, 512,  512, 25,  507812.5,1910887.5));
        THAU_SCAN_250_IGN.setTileMatrix(tm);
    }

    public final static TileMatrixSet THAU_SCAN_25_IGN;
    static {
        THAU_SCAN_25_IGN = new TileMatrixSet(new CodeType("ThauScan25IGN"), "urn:ogc:def:crs:EPSG:27593");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L5", 3, 3,  200, 250, 40,  690001.25,149998.75));
        tm.add(createTileMatrix("L4", 5, 5,  400, 500, 20,  690001.25,149998.75));
        tm.add(createTileMatrix("L3", 10,10, 400, 500, 10,  690001.25,149998.75));
        tm.add(createTileMatrix("L2", 20,20, 400, 500, 5,   690001.25,149998.75));
        tm.add(createTileMatrix("L1", 40,40, 400, 500, 2.5, 690001.25,149998.75));
        THAU_SCAN_25_IGN.setTileMatrix(tm);
    }

    //    layer,numTiles,width,height,uniformSize,scaleX,scaleY,horizontalSRID


    public static TileMatrixSet getTileMatrixSet(String name) {
        if ("ThauScan25IGN".equals(name)) {
            return THAU_SCAN_25_IGN;
        } else if ("ThauScan250IGN".equals(name)) {
            return THAU_SCAN_250_IGN;
        }  else if ("OrthoThau".equals(name)) {
            return ORTHO_THAU;
        } else if ("BlueMarble".equals(name)) {
            return BLUEMARBLE;
        }
        return null;
    }

    public static String getPathForMatrixSet(String name) {
        if ("ThauScan25IGN".equals(name)) {
            return "/France/Scan/IGN/BassinDeThau/Scan25/";
        } else if ("ThauScan250IGN".equals(name)) {
            return "/France/Scan/IGN/BassinDeThau/Scan250/";
        }  else if ("OrthoThau".equals(name)) {
            return "/France/OrthoLittoral/Thau/";
        } else if ("BlueMarble".equals(name)) {
            return "/Monde/BlueMarble/S480/";
        }
        return null;
    }

    public static TileMatrix createTileMatrix(String id, int nX, int nY, int mWidth, int mHeight, double scale, double tlc1, double tlc2) {
        final TileMatrix tm = new TileMatrix();
        tm.setIdentifier(new CodeType(id));
        tm.setMatrixHeight(nY);
        tm.setMatrixWidth(nX);
        scale *= 1852 * 60; // conversion des degres vers des metres
        tm.setScaleDenominator(1 / scale);
        tm.getTopLeftCorner().add(-180.0);
        tm.getTopLeftCorner().add(90.0);

        tm.setTileHeight(mHeight);
        tm.setTileWidth(mWidth);

        return tm;
    }

}
