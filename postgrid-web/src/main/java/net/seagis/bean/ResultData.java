package net.seagis.bean;

//~--- non-JDK imports --------------------------------------------------------

import net.seagis.catalog.CatalogException;

//~--- JDK imports ------------------------------------------------------------

import java.sql.SQLException;

import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;

import javax.naming.NamingException;

public class ResultData {
    private boolean        editable      = false;
    private ArrayDataModel model         = null;
    private String         sortColumn    = null;
    private boolean        sortAscending = true;
    private Layers[]       layers;
    private boolean        property      = false;

    private String serverPath=new String("/home/geodata/images/Mediterranee/Caraibes");    
    private String layerName=new String("Caraibes");
    
    
    public ResultData() throws SQLException, NamingException, CatalogException {
        layers = (new Reader()).getAll();
        model  = new ArrayDataModel(layers);
    }

    public DataModel getLayers() {
        return model;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean newValue) {
        editable = newValue;
    }
    public void addLayers() throws SQLException, NamingException, CatalogException {
        (new Writer()).setLayersAndSeries(getServerPath(),getLayerName());
        
        /*layers = (new Reader()).getAll();
        model  = new ArrayDataModel(layers);*/
         
         model.setWrappedData((new Reader()).getAll());
    }
    public String deleteLayers() throws SQLException, NamingException, CatalogException {
       // System.out.println("l26");

        if (!getAnyLayersMarkedForDeletion()) {
            return null;
        }

        //System.out.println("l29");

        Layers[] currentLayers = (Layers[]) model.getWrappedData();
        Layers[] newLayers     = new Layers[currentLayers.length - getNumberOfLayersMarkedForDeletion()];
        Layers[] oldLayers     = new Layers[getNumberOfLayersMarkedForDeletion()];

        for (int i = 0, j = 0, k = 0; i < currentLayers.length; ++i) {
            Layers Layers = (Layers) currentLayers[i];

            if (!Layers.isMarkedForDeletion()) {
                //System.out.println("l35 pas deleter");
                newLayers[j++] = Layers;
            } else {
                //System.out.println("l61 deleter");
                oldLayers[k] = Layers;
                k++;
            }
        }

        //System.out.println("l42");
        //System.out.println(oldLayers.length);

        
           if (oldLayers.length > 0) {
             (new Writer()).setAll(oldLayers);
          }
         
        model.setWrappedData(newLayers);

        return null;
    }

    public int getNumberOfLayersMarkedForDeletion() {
        Layers[] currentLayers = (Layers[]) model.getWrappedData();
        int      cnt           = 0;

        for (int i = 0; i < currentLayers.length; ++i) {
            Layers Layers = (Layers) currentLayers[i];

            if (Layers.isMarkedForDeletion()) {
                ++cnt;
            }
        }

        return cnt;
    }

    public boolean getAnyLayersMarkedForDeletion() {
        Layers[] currentLayers = (Layers[]) model.getWrappedData();

        for (int i = 0; i < currentLayers.length; ++i) {
            Layers layer = (Layers) currentLayers[i];

            //System.out.println("dans getAnyLayersMarkedForDeletion avant if isMarkedForDeletion "+layer.isMarkedForDeletion());
            if (layer.isMarkedForDeletion()) {
                return true;
            }
        }

        return false;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    public boolean isProperty() {
        return property;
    }

    public void setProperty(boolean property) {
        this.property = property;
    }
    
    public void changeProperty(){
        
        System.out.println(">>>>>>>>>" + property);
        
        if (this.property) property = false;
        else property = true;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String ServerPath) {
        this.serverPath = ServerPath;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
