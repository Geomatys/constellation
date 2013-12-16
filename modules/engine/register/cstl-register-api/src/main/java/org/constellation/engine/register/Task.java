package org.constellation.engine.register;

public interface Task {

    public abstract void setOwner(User owner);

    public abstract User getOwner();

    public abstract void setEnd(int end);

    public abstract int getEnd();

    public abstract void setStart(long start);

    public abstract long getStart();

    public abstract void setDescription(int description);

    public abstract int getDescription();

    public abstract void setTitle(int title);

    public abstract int getTitle();

    public abstract void setType(String type);

    public abstract String getType();

    public abstract void setState(String state);

    public abstract String getState();

    public abstract void setIdentifier(String identifier);

    public abstract String getIdentifier();

}