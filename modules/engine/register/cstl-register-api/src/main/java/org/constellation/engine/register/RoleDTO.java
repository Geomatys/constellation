package org.constellation.engine.register;

public class RoleDTO implements Role{

    private String name;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
