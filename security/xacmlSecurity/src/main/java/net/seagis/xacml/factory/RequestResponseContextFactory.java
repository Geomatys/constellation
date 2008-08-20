/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package net.seagis.xacml.factory;

import net.seagis.xacml.JBossRequestContext;
import net.seagis.xacml.JBossResponseContext;
import net.seagis.xacml.api.RequestContext;
import net.seagis.xacml.api.ResponseContext;


/**
 *  Factory to create the Request and ResponseContext objects
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class RequestResponseContextFactory {

    /**
     * Create a bare RequestContext object
     * @see RequestContext#setRequest(org.jboss.security.xacml.core.model.context.RequestType)
     * @return a RequestContext object
     */
    public static RequestContext createRequestCtx() {
        return new JBossRequestContext();
    }

    /**
     * Create a ResponseContext object
     * @return a ResponseContext object
     */
    public static ResponseContext createResponseContext() {
        return new JBossResponseContext();
    }
}
