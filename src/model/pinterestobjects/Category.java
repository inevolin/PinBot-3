/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pinterestobjects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import model.configurations.queries.AQuery;

/**
 *
 * @author UGent
 */
@Entity
@DiscriminatorValue(value = "Categories")
public class Category extends PinterestObject {

    private String name, key;

    protected Category() {
    }

    public Category(PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    //hashCode & equals MUST be defined, or Account::HashSet<Board> won't work properly.
    @Override
    public int hashCode() {
        int hash = 0;
        //hash += (getId() != null ? getId().hashCode() : 0);
        hash += (getKey() != null ? getKey().hashCode() : 0);
        hash += (getName() != null ? getName().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Category)) {
            return false;
        }
        Category other = (Category) object;

        if ((this.getKey() == null && other.getKey() != null) || (this.getKey() != null && !this.getKey().equals(other.getKey()))) {
            return false;
        } else if ((this.getName() == null && other.getName() != null) || (this.getName() != null && !this.getName().equals(other.getName()))) {
            return false;
        }
        return true;
    }

    @Override
    public PinterestObject copy(AQuery parent) {
        Category newC = new Category();
        newC.key = this.key;
        newC.name = this.name;
        newC.setResource(this.getResource());
        return newC;
    }
}
