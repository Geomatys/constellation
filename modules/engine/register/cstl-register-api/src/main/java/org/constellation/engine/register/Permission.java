package org.constellation.engine.register;

import java.io.Serializable;

public class Permission implements Serializable {
    
    
    public final static int SERVICE_READ_ACCESS_PERMISSION_ID = 1; 
    
    public final static int SERVICE_WRITE_ACCESS_PERMISSION_ID = 2; 
    
    public static final int SERVICE_CREATION = 3;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    private int id;
    
    private String name;
    
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Permission [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
    
    

}
