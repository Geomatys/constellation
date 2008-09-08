/**
 * Copyright 2007 Patrick O'Leary 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 * 
 */
package net.seagis.lucene.Filter;

import java.io.IOException;
import java.util.BitSet;

import java.util.List;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

/**
 * 
 * Provide a serial chain filter, passing the bitset in with the
 * index reader to each of the filters in an ordered fashion.
 * 
 * Based off chain filter, but will some improvements to allow a narrowed down
 * filtering. Traditional filter required iteration through an IndexReader.
 * 
 * By implementing the ISerialChainFilter class, you can create a bits(IndexReader reader, BitSet bits)
 * 
 * 
 * @author Patrick O'Leary
 *
 */
public class SerialChainFilter extends Filter {

    private List<Filter> chain;
    
    public static final int AND     = 1;	     
    public static final int OR      = 2;   
    public static final int NOT     = 3;
    public static final int XOR     = 4;
    public static final int DEFAULT = OR;
	
    private int actionType[];

    public SerialChainFilter(List<Filter> chain) {
        this.chain      = chain;
        this.actionType = new int[]{DEFAULT};
    }

    public SerialChainFilter(List<Filter> chain, int actionType[]) {
        this.chain      = chain;
        this.actionType = actionType;
    }
	
	   /* (non-Javadoc)
     * @see org.apache.lucene.search.Filter#bits(org.apache.lucene.index.IndexReader)
     */
    @Override
    public BitSet bits(IndexReader reader) throws CorruptIndexException, IOException {

        int chainSize  = chain.size();
        int actionSize = actionType.length;

        BitSet bits    = chain.get(0).bits(reader);

        //if there is only an operand not we don't enter the loop
        int j = 0;
        if (actionType[j] == NOT) {
            bits.flip(0, reader.maxDoc());
            j++;
        }

        for (int i = 1; i < chainSize; i++) {

            int action;
            if (j < actionSize) {
                action = actionType[j];
                j++;
            } else {
                action = DEFAULT;
            }

            BitSet nextFilterResponse = chain.get(i).bits(reader);

            //if the next operator is NOT we have to process the action before the current operand
            if (j < actionSize && actionType[j] == NOT) {
                nextFilterResponse.flip(0, reader.maxDoc());
                j++;
            }

            switch (action) {

                case (AND):
                    bits.and(nextFilterResponse);
                    break;
                case (OR):
                    bits.or(nextFilterResponse);
                    break;
                case (XOR):
                    bits.xor(nextFilterResponse);
                    break;
                default:
                    bits.or(nextFilterResponse);
                    break;

            }

        }
        return bits;
    }

      /**
     * @return the chain
     */
    public List<Filter> getChain() {
        return chain;
    }

    /**
     * @return the actionType
     */
    public int[] getActionType() {
        return actionType;
    }

    /**
     * Return the flag correspounding to the specified filterName.
     * 
     * @param filterName A filter name : And, Or, Xor or Not.
     * 
     * @return an int flag.
     */
    public static int valueOf(final String filterName) {

        if (filterName.equals("And")) {
            return AND;
        } else if (filterName.equals("Or")) {
            return OR;
        } else if (filterName.equals("Xor")) {
            return XOR;
        } else if (filterName.equals("Not")) {
            return NOT;
        } else {
            return DEFAULT;
        }
    }

    /**
     * Return the filterName correspounding to the specified flag.
     * 
     * @param flag an int flag.
     * 
     * @return A filter name : And, Or, Xor or Not. 
     */
    public static String ValueOf(final int flag) {
        switch (flag) {
            case (AND):
                return "AND";
            case (OR):
                return "OR";
            case (NOT):
                return "NOT";
            case (XOR):
                return "XOR";
            default:
               return "unknow";
        }
    }
    
    /** 
     * Returns true if <code>o</code> is equal to this.
     * 
     * @see org.apache.lucene.search.RangeFilter#equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialChainFilter)) return false;
        SerialChainFilter other = (SerialChainFilter) o;

        if (this.chain.size() != other.getChain().size() ||
        	this.actionType.length != other.getActionType().length)
        	return false;
        
        for (int i = 0; i < this.chain.size(); i++) {
            if (this.actionType[i] != other.getActionType()[i]  || !this.chain.get(i).equals(other.getChain().get(i)))
                return false;
        }
        return true;
    }
    
    /** 
     * Returns a hash code value for this object.
     * 
     * @see org.apache.lucene.search.RangeFilter#hashCode
     */
    @Override
    public int hashCode() {
      if (chain.size() == 0)
    	  return 0;

      int h = chain.get(0).hashCode() ^ new Integer(actionType[0]).hashCode(); 
      for (int i = 1; i < this.chain.size(); i++) {
    	  h ^= chain.get(i).hashCode();
    	  h ^= new Integer(actionType[i]).hashCode();
      }

      return h;
    }
    
    @Override
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("[SerialChainFilter]").append('\n');
        if (chain != null && chain.size() == 1) {
            buf.append("NOT ").append('\n');
            buf.append('\t').append(chain.get(0));
            
        } else if (chain != null && chain.size() > 0) {
            buf.append('\t').append(chain.get(0)).append('\n');
            
            for (int i = 0; i < actionType.length; i++) {
                switch(actionType[i]) {
                    case (AND):
                        buf.append("AND");
                        break;
                    case (OR):
                        buf.append("OR");
                        break;
                    case (NOT):
                        buf.append("NOT");
                        break;
                    case (XOR):
                        buf.append("XOR");
                        break;
                    default:
                        buf.append(actionType[i]);
                }
                buf.append('\n');
                if (chain.size() > i + 1)
                    buf.append('\t').append(" " + chain.get(i + 1).toString()).append('\n');
            }
        }
        buf.append('\n');
    	return buf.toString().trim();
    }
}
