package org.constellation.engine.register;

public class Property {

    private String key;

    private String value;

    
    public Property() {
    }
    
    public Property(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "PropertyDTO [key=" + key + ", value=" + value + "]";
    }
    
    

}
