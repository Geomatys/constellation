/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.coverage.web;

public class Version implements Comparable<Version> {
        /**
         * the version number.
         */
        private String versionNumber;
        
        /**
         * indicate if this version of the service implement the OWS specification.
         */
        private boolean isOWS;
        
        /**
         * Build a new version.
         */
        public Version(String versionNumber, boolean isOWS){
            this.versionNumber = versionNumber;
            this.isOWS         = isOWS;
        }
        
        public String getVersionNumber() {
            return versionNumber;
        }
        
        public boolean isOWS() {
            return isOWS;
        }
        
        @Override
        public String toString(){
            return versionNumber;
        }

        public int compareTo(Version that) {
            if (this.versionNumber.equals(that.versionNumber)){
                return 0;
            } else {
                String thisNumber = this.versionNumber;
                String thatNumber = that.versionNumber;
                while (thisNumber.length() != 0) {
                    String temp1 = thisNumber.charAt(0) + "";
                    String temp2 = thatNumber.charAt(0) + "";
                    int i = Integer.parseInt(temp1);
                    int j = Integer.parseInt(temp2);
                    if (i < j) {
                        return -1;
                    } else if (j < i) {
                        return 1;
                    }
                    if (thisNumber.indexOf(".") != -1) {
                        thisNumber = thisNumber.substring(2, thisNumber.length());
                    } else {
                        thisNumber = "";
                    }
                    if (thatNumber.indexOf(".") != -1) {
                        thatNumber = thatNumber.substring(2, thatNumber.length());
                    } else {
                        thatNumber = "";
                    }
                    
                    // if one of the number is more longer than the other
                    if ((thatNumber.equals("") && !thisNumber.equals(""))){
                        return 1;
                    }
                    if ((thisNumber.equals("") && !thatNumber.equals(""))){
                        return -1;
                    }
                }
            }
            return 0;
        }
    }
