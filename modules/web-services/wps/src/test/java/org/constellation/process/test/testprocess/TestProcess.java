package org.constellation.process.test.testprocess;

import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.geometry.Envelope;

import static org.constellation.process.test.testprocess.TestDescriptor.*;
import static org.geotoolkit.parameter.Parameters.*;


/**
 *
 * @author Theo Zozime
 *
 * Process for testing purposes.
 * Takes 3 optional parameters : a bounding box, and two literal
 * The result is string containing the values of the three parameters
 */
public class TestProcess extends AbstractProcess {


    public TestProcess(final ParameterValueGroup input) {
        super(INSTANCE, input);
    }

    @Override
    protected void execute() throws ProcessException {
        final Envelope envelope = value(BBOX_IN, inputParameters);
        final double doubleValue = value(DOUBLE_IN, inputParameters);
        final String stringValue = value(STRING_IN, inputParameters);

        getOrCreate(STRING_OUT, outputParameters).setValue(envelope.toString() + " " + doubleValue + " " + stringValue);
    }

}
