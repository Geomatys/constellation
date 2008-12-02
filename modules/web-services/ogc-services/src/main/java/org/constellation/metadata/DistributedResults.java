/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilhem
 */
public class DistributedResults {
        
        /**
         * The number of records matched on all distributed servers.
         */
        public int nbMatched;
        
        /**
         * The merged list of records.
         */
        public List<Object> additionalResults;
        
        public DistributedResults() {
            this.nbMatched         = 0;
            this.additionalResults = new ArrayList<Object>(); 
        }
        
        public DistributedResults(int nbMatched, List<Object> additionalResults) {
            this.nbMatched         = nbMatched;
            this.additionalResults = additionalResults; 
        }
        
        
        
    }
