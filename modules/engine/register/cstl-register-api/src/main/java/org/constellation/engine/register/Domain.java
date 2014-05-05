package org.constellation.engine.register;

import java.util.Set;

public class Domain  {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    private int id;
    private String name;
    private String description;

    
    private Set<Integer> layers;
    
    private Set<Integer> services;
    
    private Set<Integer> datas;
    
    private Set<Integer> styles;
    
    public Domain() {
    }
    
    public Domain(String name, String description) {
        this(0, name, description);
    }
    public Domain(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;        
    }
    
    public int getId() {
        return id;
    }

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


    public Set<Integer> getLayers() {
        return layers;
    }

    public Set<Integer> getServices() {
        return this.services;
    }

    public Set<Integer> getDatas() {
        return this.datas;
    }


    public Set<Integer> getStyles() {
        return this.styles;
    }


    public void setServices(Set<Integer> services) {
        this.services = services;
        
    }

    @Override
    public String toString() {
        return "DomainDTO [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
    
    


}
