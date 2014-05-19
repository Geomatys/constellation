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
package org.constellation;

import org.constellation.portrayal.PortrayalServiceIF;
import org.constellation.portrayal.internal.CstlPortrayalService;
import org.constellation.register.PrimitiveRegisterIF;
import org.constellation.register.internal.PrimitiveRegister;

/**
 * The root class of the Constellation server Engine, this class provides the
 * static fields and methods used by services.
 * <p>
 * <b>TODO:<b> This will obviously evolve to include dynamic registration; for 
 * now, we are merely trying to get the system functional.
 * </p>
 * 
 * @author Adrian Custer
 * @since 0.3
 *
 */
public final class Cstl {

    //We don't want any instances of this class.
    private Cstl() {
    }

    /**
     * Provides access to the interface through which to interact with the
     * local register; the interface is currently experimental.
     *
     * @return An implementation of the primitive {@link PrimitiveRegisterIF
     *           Register Interface}.
     */
    public static PrimitiveRegisterIF getRegister() {
        return PrimitiveRegister.internalGetInstance();
    }

    /**
     * Provides access to the portrayal service through which internal resources
     * can be rendered onto a two dimensional image.
     *
     * @return An implementation of the {@link PortrayalSerivceIF Portrayal
     *           Service Interface}.
     */
    public static PortrayalServiceIF getPortrayalService() {
        return CstlPortrayalService.getInstance();
    }
}
