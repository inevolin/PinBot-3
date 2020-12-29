/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import model.pinterestobjects.Comment;

@Entity
@DiscriminatorValue(value = "Comment")
public class CommentConfiguration extends AConfiguration {

    
    // a list of comments mapped to a specific query ; instead of a list of comments for all queries.
    //@ElementCollection
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> mapping;

    public Set<Comment> getMapping() {
        if (mapping == null) {
            mapping = new HashSet<>();
        }
        return mapping;
    }

    public void setMapping(Set<Comment> mapping) {
        this.mapping = mapping;
    }

    //hashCode & equals MUST be defined, or Campaign::HashSet<Configuration> won't work properly.

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CommentConfiguration)) {
            return false;
        }
        CommentConfiguration other = (CommentConfiguration) object;
        if ( ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }
}
