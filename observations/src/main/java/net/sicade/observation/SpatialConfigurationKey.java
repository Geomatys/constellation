/*
 * (C) 2007, Geomatys
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation;

/**
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class SpatialConfigurationKey extends ConfigurationKey {
    
    /**
     * La valeur de la clé contenant une partie spatiale.
     */
    private final transient String spatialValue;
    
    /**
     * Construit une nouvelle clé spatiale.
     *
     * @param name          Le nom de la clé à créer. Ce nom doit être unique.
     * @param defaultValue  Valeur par défaut associée à la clé, utilisée que si l'utilisateur n'a
     *                      pas spécifié explicitement de valeur.
     * @param spatialValue  Valeur de la partie spatiale.
     */
    public SpatialConfigurationKey(final String name, final String defaultValue,
            final String spatialValue) {
        super(name, defaultValue);
        this.spatialValue = spatialValue;
    }
    
    /**
     * Retourne la valeur de la partie spatiale associée à cette clé.
     */
    public String getSpatialValue() {
        return spatialValue;
    }
}
