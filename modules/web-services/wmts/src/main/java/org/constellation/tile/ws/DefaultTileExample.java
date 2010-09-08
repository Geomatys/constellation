
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
public final class DefaultTileExample {

    private DefaultTileExample() {}
    
    public static final TileMatrixSet BLUEMARBLE;
    static {
        BLUEMARBLE = new TileMatrixSet(new CodeType("BlueMarble"), "urn:ogc:def:crs:OGC:1.3:CRS84");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L13",  1,   1,  480, 240, 0.75,                -180, 90));
        tm.add(createTileMatrix("L12",  2,   1,  480, 480, 0.375,               -180, 90));
        tm.add(createTileMatrix("L11",  4,   2,  480, 480, 0.1875,              -180, 90));
        tm.add(createTileMatrix("L10",  6,   3,  480, 480, 0.125,               -180, 90));
        tm.add(createTileMatrix("L09",  10,  5,  480, 480, 0.075,               -180, 90));
        tm.add(createTileMatrix("L08",  12,  6,  480, 480, 0.0625,              -180, 90));
        tm.add(createTileMatrix("L07",  18,  9,  480, 480, 0.0416666666666667,  -180, 90));
        tm.add(createTileMatrix("L06",  20,  10, 480, 480, 0.0375,              -180, 90));
        tm.add(createTileMatrix("L05",  30,  15, 480, 480, 0.025,               -180, 90));
        tm.add(createTileMatrix("L04",  36,  18, 480, 480, 0.0208333333333333,  -180, 90));
        tm.add(createTileMatrix("L03",  60,  30, 480, 480, 0.0125,              -180, 90));
        tm.add(createTileMatrix("L02",  90,  45, 480, 480, 0.00833333333333333, -180, 90));
        tm.add(createTileMatrix("L01",  180, 90, 480, 480, 0.00416666666666667, -180, 90));
        BLUEMARBLE.setTileMatrix(tm);
    }

    /*public static final TileMatrixSet BLUEMARBLE;
    static {
        BLUEMARBLE = new TileMatrixSet(new CodeType("BlueMarble"), "urn:ogc:def:crs:OGC:1.3:CRS84");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L10",  1,   1,   480, 480, 0.125,               -180, 90));
        tm.add(createTileMatrix("L09",  2,   2,   480, 480, 0.075,               -180, 90));
        tm.add(createTileMatrix("L08",  4,   4,   480, 480, 0.0625,              -180, 90));
        tm.add(createTileMatrix("L07",  8,   8,   480, 480, 0.0416666666666667,  -180, 90));
        tm.add(createTileMatrix("L06",  16,  6,  480, 480, 0.0375,              -180, 90));
        tm.add(createTileMatrix("L05",  21,  32,  480, 480, 0.025,               -180, 90));
        tm.add(createTileMatrix("L04",  64,  43,  480, 480, 0.0208333333333333,  -180, 90));
        tm.add(createTileMatrix("L03",  128, 43, 480, 480, 0.0125,              -180, 90));
        tm.add(createTileMatrix("L02",  256, 85, 480, 480, 0.00833333333333333, -180, 90));
        tm.add(createTileMatrix("L01",  512, 512, 480, 480, 0.00416666666666667, -180, 90));
        BLUEMARBLE.setTileMatrix(tm);
    }*/

    public static final TileMatrixSet ORTHO_THAU;
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

    public static final TileMatrixSet THAU_SCAN_250_IGN;
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

    public static final TileMatrixSet THAU_SCAN_25_IGN;
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

    public static final TileMatrixSet UNESCO;
    static {
        UNESCO = new TileMatrixSet(new CodeType("UNESCO"), "urn:ogc:def:crs:EPSG:4326");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("L20", 1,   1,    200, 100, 1.80000000002088,-180,90));
        tm.add(createTileMatrix("L19", 2,   1,    200, 200, 0.90000000001044,-180,90));
        tm.add(createTileMatrix("L18", 3,   2,    200, 200, 0.60000000000696,-180,90));
        tm.add(createTileMatrix("L17", 4,   2,    200, 200, 0.45000000000522,-180,90));
        tm.add(createTileMatrix("L16", 6,   3,    200, 200, 0.30000000000348,-180,90));
        tm.add(createTileMatrix("L15", 8,   4,    200, 200, 0.22500000000261,-180,90));
        tm.add(createTileMatrix("L14", 9,   4,    200, 200, 0.20000000000232,-180,90));
        tm.add(createTileMatrix("L13", 12,  6,    200, 200, 0.15000000000174,-180,90));
        tm.add(createTileMatrix("L12", 18,  8,    200, 200, 0.10000000000116,-180,90));
        tm.add(createTileMatrix("L11", 24,  11,   200, 200, 0.07500000000087,-180,90));
        tm.add(createTileMatrix("L10", 27,  12,   200, 200, 0.06666666666744,-180,90));
        tm.add(createTileMatrix("L09",  36,  16,  200, 200, 0.05000000000058,-180,90));
        tm.add(createTileMatrix("L08",  54,  23,  200, 200, 0.03333333333372,-180,90));
        tm.add(createTileMatrix("L07",  72,  31,  200, 200, 0.02500000000029,-180,90));
        tm.add(createTileMatrix("L06",  81,  34,  200, 200, 0.02222222222248,-180,90));
        tm.add(createTileMatrix("L05",  108, 46,  200, 200, 0.01666666666686,-180,90));
        tm.add(createTileMatrix("L04",  162, 68,  200, 200, 0.01111111111124,-180,90));
        tm.add(createTileMatrix("L03",  216, 91,  200, 200, 0.00833333333343,-180,90));
        tm.add(createTileMatrix("L02",  324, 136, 200, 200, 0.00555555555562,-180,90));
        tm.add(createTileMatrix("L01",  648, 272, 200, 200, 0.00277777777781,-180,90));
        UNESCO.setTileMatrix(tm);
    }

    public static final TileMatrixSet ORTHO_ALPES_JPEG;
    static {
        ORTHO_ALPES_JPEG = new TileMatrixSet(new CodeType("ORTHO_ALPES_JPEG"), "urn:ogc:def:crs:EPSG:2154");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add( createTileMatrix("L11",    16,    16,  256, 256, 512,       0, 8388608 ) );    // 2097152
        tm.add( createTileMatrix("L10",    32,    32,  256, 256, 256,       0, 8388608 ) );    // 1048576
        tm.add( createTileMatrix("L09",    64,    64,  256, 256, 128,       0, 8388608 ) );    //  524288
        tm.add( createTileMatrix("L08",   128,   128,  256, 256,  64,       0, 8388608 ) );    //  262144
        tm.add( createTileMatrix("L07",   256,   256,  256, 256,  32,       0, 8388608 ) );    //  131072
        tm.add( createTileMatrix("L06",   512,   512,  256, 256,  16,       0, 8388608 ) );    //   65536
        tm.add( createTileMatrix("L05",  1024,  1024,  256, 256,   8,       0, 8388608 ) );    //   32768
        tm.add( createTileMatrix("L04",  2048,  2048,  256, 256,   4,       0, 8388608 ) );    //   16384
        tm.add( createTileMatrix("L03",  4096,  4096,  256, 256,   2,       0, 8388608 ) );    //    8192
        tm.add( createTileMatrix("L02",  8129,  8129,  256, 256,   1,       0, 8388608 ) );    //    4096
        tm.add( createTileMatrix("L01", 16384, 16384,  256, 256, 0.5,       0, 8388608 ) );    //    2048
        ORTHO_ALPES_JPEG.setTileMatrix(tm);
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
        } else if ("UNESCO".equals(name)) {
            return UNESCO;
        } else if ("ORTHO_ALPES_JPEG".equals(name)) {
            return ORTHO_ALPES_JPEG;
        }
        return null;
    }

    public static Path getPathForMatrixSet(String name) {
        if ("ThauScan25IGN".equals(name)) {
            return new Path(false, "/France/Scan/IGN/BassinDeThau/Scan25/");
        } else if ("ThauScan250IGN".equals(name)) {
            return new Path(false, "/France/Scan/IGN/BassinDeThau/Scan250/");
        }  else if ("OrthoThau".equals(name)) {
            return new Path(false, "/France/OrthoLittoral/Thau/");
        } else if ("BlueMarble".equals(name)) {
            return new Path(false, "/Monde/BlueMarble/S480/");
        } else if ("UNESCO".equals(name)) {
            return new Path(false, "/Monde/UNESCO/");
         }else if ("ORTHO_ALPES_JPEG".equals(name)) {
            return new Path(true, "/media/ign/ORTHO_ALPES_JPEG");
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
        tm.getTopLeftCorner().add(tlc1);
        tm.getTopLeftCorner().add(tlc2);

        tm.setTileHeight(mHeight);
        tm.setTileWidth(mWidth);

        return tm;
    }

    public static class Path {
        public final boolean isAbsolute;

        public final String path;

        public Path(boolean isAbsolute, String path) {
            this.isAbsolute = isAbsolute;
            this.path       = path;
        }

    }
}
