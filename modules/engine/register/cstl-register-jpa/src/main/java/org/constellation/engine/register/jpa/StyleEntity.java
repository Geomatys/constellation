package org.constellation.engine.register.jpa;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.User;

@Entity
@Table(schema = "`admin`", name = "`style`")
public class StyleEntity implements Style {

    @Id
    @Column(name = "`id`")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "`name`")
    private String name;

    @ManyToOne(targetEntity=ProviderEntity.class)
    @JoinColumn(name = "`provider`")
    private Provider provider;

    @Column(name = "`type`")
    private String type;

    @Column(name = "`date`")
    private long date;

    @Column(name = "`title`")
    private int title;

    @Column(name = "`description`")
    private int description;

    @Column(name = "`body`")
    private String body;

    @ManyToMany(mappedBy="styles", targetEntity=DomainEntity.class)
    private Set<Domain> domains;
    
    @ManyToOne(targetEntity=UserEntity.class)
    @JoinColumn(name = "`owner`")
    private User owner;

    @ManyToMany(targetEntity=DataEntity.class)
    @JoinTable(schema = "`admin`", name = "`styled_data`", joinColumns = @JoinColumn(name = "`style`"), inverseJoinColumns = @JoinColumn(name = "`data`"))
    private List<Data> datas;

    @Override
    public String toString() {
        return "Style [id=" + id + ", name=" + name + ", provider=" + provider + ", type=" + type + ", date=" + date
                + ", title=" + title + ", description=" + description + ", body=" + body + ", owner=" + owner + "]";
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int getTitle() {
        return title;
    }

    @Override
    public void setTitle(int title) {
        this.title = title;
    }

    @Override
    public int getDescription() {
        return description;
    }

    @Override
    public void setDescription(int description) {
        this.description = description;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

}
