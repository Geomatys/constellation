/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.coverage.process;

import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.AbstractCstlProcess;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PyramidCoverageDescriptor extends AbstractPyramidCoverageDescriptor {
    
    public static final String NAME = "coverage.pyramid";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Build a pyramid from the specified file.");

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(IN_COVERAGE_REF, ORIGINAL_DATA, PYRAMID_NAME, PYRAMID_FOLDER,
                    PROVIDER_OUT_ID, PYRAMID_DATASET, PYRAMID_CRS, UPDATE);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup(OUT_PYRAMID_PROVIDER_CONF);

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public PyramidCoverageDescriptor() {
        super(NAME, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final PyramidCoverageDescriptor INSTANCE = new PyramidCoverageDescriptor();
    
    @Override
    protected AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new PyramidCoverageProcess(this, input);
    }
}
