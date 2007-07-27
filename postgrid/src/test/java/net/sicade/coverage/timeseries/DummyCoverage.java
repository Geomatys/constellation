package net.sicade.coverage.timeseries;

import net.sicade.catalog.CRS;
import org.geotools.coverage.AbstractCoverage;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.SampleDimension;
import org.opengis.geometry.DirectPosition;

/**
 *
 * @author Touraïvane
 */
public class DummyCoverage extends AbstractCoverage {
    double [][][] donnees;
    int [] size = {10, 7, 12};
    
    /**
     * Creates a new instance of DummyCoverage
     */
    public DummyCoverage() {
        this(10, 7, 12);
    }
    
    public DummyCoverage(int x, int y, int t) {
        super("fd", CRS.XYT.getCoordinateReferenceSystem(), null, null);
        size[0] = x;
        size[1] = y;
        size[2] = t;
        donnees = new double[x][y][t];
        init();
    }
    
    private void init() {
        int x = 0;
        for (int i = 0; i<size[0]; i++) {
            for (int j = 0; j<size[1]; j++) {
                for (int k = 0; k<size[2]; k++) {
                    donnees[i][j][k] = x++;
                }
            }
        }
    }
    
    public void print() {
        int x = 0;
        for (int i = 0; i<size[0]; i++) {
            for (int j = 0; j<size[1]; j++) {
                for (int k = 0; k<size[2]; k++) {
                    
                    System.out.print(donnees[i][j][k]+" ");
                }
                System.out.println("");
            }
            System.out.println("   --------   ");
        }
    }
    
    public double[]  evaluate(DirectPosition directPosition, double[] dest) throws CannotEvaluateException {
        assert directPosition.getDimension() == 3;
        if (dest == null) dest = new double[1];
        dest[0]= donnees[(int)directPosition.getOrdinate(0)]
                [(int)directPosition.getOrdinate(1)]
                [(int)directPosition.getOrdinate(2)];
        return dest;
    }
    
    public Object evaluate(DirectPosition directPosition) throws CannotEvaluateException {
        return evaluate(directPosition, (double[])null);
    }
    
    public int getNumSampleDimensions() {
        return 1;
    }
    
    public SampleDimension getSampleDimension(int param) throws IndexOutOfBoundsException {
        return null;
    }
}


class DummyCoverage2 extends DummyCoverage {
    public DummyCoverage2() {
        this(10, 7, 12);
    }
    
    public DummyCoverage2(int x, int y, int t) {
        super(x, y, t);
        init();
    }
    
    private void init() {
        int x = 0;
        for (int k = 0; k<size[2]; k++) {
            for (int i = 0; i<size[0]; i++) {
                for (int j = 0; j<size[1]; j++) {
                    donnees[i][j][k] = x++;
                }
            }
        }
    }
}