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

	
	private Filter chain[];
	public static final int AND       = 1;	     
	public static final int OR        = 2;   
        public static final int NOT       = 3;
        public static final int XOR       = 4;
	public static final int DEFAULT   = OR;
	
	private int actionType[];
	
	public SerialChainFilter(Filter chain[]){
		this.chain      = chain;
		this.actionType = new int[] {DEFAULT};
	}
	
	public SerialChainFilter(Filter chain[], int actionType[]){
		this.chain      = chain;
		this.actionType = actionType;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#bits(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public BitSet bits(IndexReader reader) throws CorruptIndexException, IOException {
		
		int chainSize  = chain.length;
		int actionSize = actionType.length;
		
		BitSet bits    = chain[0].bits(reader);
                
                //if there is only an operand not we don't enter the loop
                int j = 0;
                if (actionType[j] == NOT) {
                    bits.flip(0, reader.maxDoc());
                    j++;
                }
                
		for( int i = 1; i < chainSize; i++) {
                        
                    int action;
                    if (j < actionSize ) {
			 action = actionType[j];
                         j++;
                    } else action = DEFAULT;
                    
                    BitSet nextFilterResponse = chain[i].bits(reader);
                    
                    //if the next operator is NOT we have to process the action before the current operand
                    if (j < actionSize && actionType[j] == NOT) {
                        nextFilterResponse.flip(0, reader.maxDoc());
                        j++;
                    }
                    
	            switch (action) {
			
			case (AND):
				bits.and(nextFilterResponse);
				break;
			case (OR) :
				bits.or(nextFilterResponse);
				break;
                        case (XOR) :
				bits.xor(nextFilterResponse);
				break;
                        default   :
                                bits.or(nextFilterResponse);
				break;
			
			}
	
		}
		return bits;
	}

    /**
	 * @return the chain
	 */
	Filter[] getChain() {
		return chain;
	}

	/**
	 * @return the actionType
	 */
	int[] getActionType() {
		return actionType;
	}

	/** 
     * Returns true if <code>o</code> is equal to this.
     * 
     * @see org.apache.lucene.search.RangeFilter#equals
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialChainFilter)) return false;
        SerialChainFilter other = (SerialChainFilter) o;

        if (this.chain.length != other.getChain().length ||
        	this.actionType.length != other.getActionType().length)
        	return false;
        
        for (int i = 0; i < this.chain.length; i++) {
        	if (this.actionType[i] != other.getActionType()[i]  ||
        		(!this.chain[i].equals(other.getChain()[i])))
        		return false;
        }
        return true;
    }
    
    /** 
     * Returns a hash code value for this object.
     * 
     * @see org.apache.lucene.search.RangeFilter#hashCode
     */
    public int hashCode() {
      if (chain.length == 0)
    	  return 0;

      int h = chain[0].hashCode() ^ new Integer(actionType[0]).hashCode(); 
      for (int i = 1; i < this.chain.length; i++) {
    	  h ^= chain[i].hashCode();
    	  h ^= new Integer(actionType[i]).hashCode();
      }

      return h;
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("SerialChainFilter(");
    	for (int i = 0; i < chain.length; i++) {
    		switch(actionType[i]) {
			case (AND):
				buf.append("AND");
				break;
			case (OR):
				buf.append("OR");
				break;
			default:
				buf.append(actionType[i]);
    		}
    		buf.append(" " + chain[i].toString() + " ");
    	}
    	return buf.toString().trim() + ")";
    }
}
