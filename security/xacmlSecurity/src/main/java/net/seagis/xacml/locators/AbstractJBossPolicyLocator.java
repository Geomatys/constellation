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
package net.seagis.xacml.locators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.seagis.xacml.api.ContextMapOp;
import net.seagis.xacml.api.PolicyLocator;
import net.seagis.xacml.api.XACMLPolicy;
import net.seagis.xacml.jaxb.Option;


/**
 *  Base Class for Policy Locators
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public abstract class AbstractJBossPolicyLocator implements PolicyLocator, ContextMapOp {
   private List<Option> options = null;

   protected Map<String, Object> map = new HashMap<String, Object>();

   private Set<XACMLPolicy> policies;

   /**
    * @see PolicyLocator#setOptions(List)
    */
   public void setOptions(List<Option> theoptions) {
      this.options = theoptions;
   }

   /**
    * @see ContextMapOp#get(String)
    */
   @SuppressWarnings("unchecked")
   public <T> T get(String key) {
      return (T) map.get(key);
   }

   /**
    * @see ContextMapOp#set(String, Object)
    */
   public <T> void set(String key, T obj) {
      map.put(key, obj);
   }

   /**
    * @see PolicyLocator#setPolicies(Set)
    */
   public abstract void setPolicies(Set<XACMLPolicy> policies);
}
