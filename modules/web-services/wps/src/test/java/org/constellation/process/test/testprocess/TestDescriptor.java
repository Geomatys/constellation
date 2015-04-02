package org.constellation.process.test.testprocess;


import java.util.HashMap;
import java.util.Map;
import org.opengis.parameter.ParameterDescriptor;
import org.apache.sis.parameter.DefaultParameterDescriptor;
import org.apache.sis.parameter.DefaultParameterDescriptorGroup;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.test.TestProcessingRegistry;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Théo Zozime
 */
public class TestDescriptor extends AbstractProcessDescriptor {

    /** Process name : test **/
    public static final String NAME = "wpstest";
     
    /**
     * Input parameters
     */
    private static final Map<String, String> BBOX_PROPS = new HashMap<>();
    private static final Map<String, String> DOUBLE_PROPS = new HashMap<>();
    private static final Map<String, String> STRING_PROPS = new HashMap<>();
    private static final Map<String, String> INPUT_PROPS = new HashMap<>();
    
    static {
        BBOX_PROPS.put("name", "bbox");
        DOUBLE_PROPS.put("name", "double");
        STRING_PROPS.put("name", "string");
        INPUT_PROPS.put("name", "InputParameters");
    }
    
    public static final ParameterDescriptor<Envelope> BBOX_IN =
            new DefaultParameterDescriptor(BBOX_PROPS, 0, 1, Envelope.class, null, null, null);
    public static final ParameterDescriptor<Double> DOUBLE_IN =
            new DefaultParameterDescriptor(DOUBLE_PROPS, 0, 1, Double.class, null, null, null);
    public static final ParameterDescriptor<String> STRING_IN =
            new DefaultParameterDescriptor(STRING_PROPS, 0, 1, String.class, null, null, null);
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup(INPUT_PROPS, 1, 1, BBOX_IN, DOUBLE_IN, STRING_IN);
    
    
    /**
     * Output parameters
     */
    private static final Map<String, Object> STRING_OUT_PROPS = new HashMap<>();
    private static final Map<String, Object> OUTPUT_PROPS = new HashMap<>();
    
    static {
        STRING_OUT_PROPS.put("name", "result");
        OUTPUT_PROPS.put("name", "OutputParameters");
        OUTPUT_PROPS.put("remarks", new SimpleInternationalString("test"));
    }
    
    public static final ParameterDescriptor<String> STRING_OUT =
            new DefaultParameterDescriptor(STRING_OUT_PROPS, 1, 1, String.class, null, null, null);
    public static final ParameterDescriptorGroup OUTPUT_DESC =
            new DefaultParameterDescriptorGroup(OUTPUT_PROPS, 1, 1, STRING_OUT);
    
    private TestDescriptor() {
        super(NAME, TestProcessingRegistry.IDENTIFICATION,
             new SimpleInternationalString("Test process to test wps implementation"),
             INPUT_DESC, OUTPUT_DESC);
    }
    
    public static final ProcessDescriptor INSTANCE = new TestDescriptor();
    
    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new TestProcess(input);
    }
    
}
