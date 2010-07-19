
package org.constellation.tile.ws;

import java.math.BigInteger;
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
        tm.add(createTileMatrix("1",    "480", "240",0.75, -180, 90));
        tm.add(createTileMatrix("2",    "480", "480",0.375, -180, 90));
        tm.add(createTileMatrix("8",    "480", "480",0.1875, -180, 90));
        tm.add(createTileMatrix("18",   "480", "480",0.125, -180, 90));
        tm.add(createTileMatrix("50",   "480", "480",0.075, -180, 90));
        tm.add(createTileMatrix("72",   "480", "480",0.0625, -180, 90));
        tm.add(createTileMatrix("162",  "480", "480",0.0416666666666667, -180, 90));
        tm.add(createTileMatrix("200",  "480", "480",0.0375, -180, 90));
        tm.add(createTileMatrix("450",  "480", "480",0.025, -180, 90));
        tm.add(createTileMatrix("648",  "480", "480",0.0208333333333333, -180, 90));
        tm.add(createTileMatrix("1800", "480", "480",0.0125, -180, 90));
        tm.add(createTileMatrix("4050", "480", "480",0.00833333333333333, -180, 90));
        tm.add(createTileMatrix("16200","480", "480",0.00416666666666667, -180, 90));
        BLUEMARBLE.setTileMatrix(tm);
    }

    public final static TileMatrixSet ORTHO_THAU;
    static {
        ORTHO_THAU = new TileMatrixSet(new CodeType("OrthoThau"), "urn:ogc:def:crs:EPSG:27582");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("1",    "273", "312",128, 690000.25,1844999.75));
        tm.add(createTileMatrix("3",    "46",  "125",64, 690000.25,1844999.75));
        tm.add(createTileMatrix("7",    "93",  "250",32, 690000.25,1844999.75));
        tm.add(createTileMatrix("21",   "187", "500",16, 690000.25,1844999.75));
        tm.add(createTileMatrix("69",   "375", "500",8, 690000.25,1844999.75));
        tm.add(createTileMatrix("261",  "250", "500",4, 690000.25,1844999.75));
        tm.add(createTileMatrix("975",  "500", "500",2, 690000.25,1844999.75));
        tm.add(createTileMatrix("3900", "500", "500",1, 690000.25,1844999.75));
        tm.add(createTileMatrix("15600","500", "500",0.5, 690000.25,1844999.75));
        ORTHO_THAU.setTileMatrix(tm);
    }

    public final static TileMatrixSet THAU_SCAN_250_IGN;
    static {
        THAU_SCAN_250_IGN = new TileMatrixSet(new CodeType("ThauScan250IGN"), "urn:ogc:def:crs:EPSG:27582");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("1",   "384", "284",800, 507812.5,1910887.5));
        tm.add(createTileMatrix("4",   "257", "56",400, 507812.5,1910887.5));
        tm.add(createTileMatrix("12",  "2",   "113",200, 507812.5,1910887.5));
        tm.add(createTileMatrix("35",  "4",   "226",100, 507812.5,1910887.5));
        tm.add(createTileMatrix("117", "8",   "452",50, 507812.5,1910887.5));
        tm.add(createTileMatrix("450", "16",  "392",25, 507812.5,1910887.5));
        THAU_SCAN_250_IGN.setTileMatrix(tm);
    }

    public final static TileMatrixSet THAU_SCAN_25_IGN;
    static {
        THAU_SCAN_25_IGN = new TileMatrixSet(new CodeType("ThauScan25IGN"), "urn:ogc:def:crs:EPSG:27593");
        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
        tm.add(createTileMatrix("7",    "200", "250",40, 690001.25,149998.75));
        tm.add(createTileMatrix("18",   "400", "500",20, 690001.25,149998.75));
        tm.add(createTileMatrix("68",   "400", "500",10, 690001.25,149998.75));
        tm.add(createTileMatrix("260",  "400", "500",5, 690001.25,149998.75));
        tm.add(createTileMatrix("1040", "400", "500",2.5, 690001.25,149998.75));
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

    public static TileMatrix createTileMatrix(String id, String mWidth, String mHeight, double scale, double tlc1, double tlc2) {
        final TileMatrix tm = new TileMatrix();
        tm.setIdentifier(new CodeType(id));
        tm.setMatrixHeight(new BigInteger(mHeight));
        tm.setMatrixWidth(new BigInteger(mWidth));
        scale *= 1852 * 60; // conversion des degres vers des metres
        tm.setScaleDenominator(1 / scale);
        tm.getTopLeftCorner().add(-180.0);
        tm.getTopLeftCorner().add(90.0);

        tm.setTileHeight(getHeightFromScale(scale));
        tm.setTileWidth(getWidthFromScale(scale));

        return tm;
    }


    public static BigInteger getHeightFromScale(double scale) {
        if (scale == 0.00416666666666667) {
            return new BigInteger("90");
        } else if (scale == 0.00833333333333333) {
            return new BigInteger("45");
        } else if (scale == 0.0125) {
            return new BigInteger("30");
        } else if (scale == 0.0208333333333333) {
            return new BigInteger("18");
        } else if (scale == 0.025) {
            return new BigInteger("15");
        } else if (scale == 0.0375) {
            return new BigInteger("10");
        } else if (scale == 0.0416666666666667) {
            return new BigInteger("9");
        } else if (scale == 0.0625) {
            return new BigInteger("6");
        } else if (scale == 0.075) {
            return new BigInteger("5");
        } else if (scale == 0.125) {
            return new BigInteger("3");
        } else if (scale == 0.1875) {
            return new BigInteger("2");
        } else if (scale == 0.375) {
            return new BigInteger("1");
        } else if (scale == 0.75) {
            return new BigInteger("1");
        }
        return null;
    }

    public static BigInteger getWidthFromScale(double scale) {
        if (scale == 0.00416666666666667) {
            return new BigInteger("180");
        } else if (scale == 0.00833333333333333) {
            return new BigInteger("90");
        } else if (scale == 0.0125) {
            return new BigInteger("60");
        } else if (scale == 0.0208333333333333) {
            return new BigInteger("36");
        } else if (scale == 0.025) {
            return new BigInteger("30");
        } else if (scale == 0.0375) {
            return new BigInteger("20");
        } else if (scale == 0.0416666666666667) {
            return new BigInteger("18");
        } else if (scale == 0.0625) {
            return new BigInteger("12");
        } else if (scale == 0.075) {
            return new BigInteger("10");
        } else if (scale == 0.125) {
            return new BigInteger("6");
        } else if (scale == 0.1875) {
            return new BigInteger("4");
        } else if (scale == 0.375) {
            return new BigInteger("2");
        } else if (scale == 0.75) {
            return new BigInteger("1");
        }
        return null;
    }
}
