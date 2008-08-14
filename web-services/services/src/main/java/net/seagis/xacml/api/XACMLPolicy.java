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
package net.seagis.xacml.api;

import java.util.List;


/**
 *  Represents a Policy or a PolicySet in the XACML World
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 5, 2007 
 *  @version $Revision$
 */
public interface XACMLPolicy extends ContextMapOp {

    /**
     * Type identifying a PolicySet
     */
    int POLICYSET = 0;
    /**
     * Type identifying a Policy
     */
    int POLICY = 1;

    /**
     * Return a type (PolicySet or Policy)
     * @return int value representing type
     */
    int getType();

    /**
     * A PolicySet can contain policies within.
     * Setter to set the policies inside a policyset
     * @param policies a list of policies
     */
    void setEnclosingPolicies(List<XACMLPolicy> policies);

    /**
     * Return the enclosing policies for a PolicySet
     * @return a list of policies
     */
    List<XACMLPolicy> getEnclosingPolicies();
}
