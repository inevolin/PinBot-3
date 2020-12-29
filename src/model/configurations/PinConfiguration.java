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
import model.pinterestobjects.Pin;

/**
 *
 * @author UGent
 */
@Entity
@DiscriminatorValue(value = "Pin")
public class PinConfiguration extends AConfiguration {

    private Set<String> descUrls;
    private Integer descUrlRate;
    private Set<String> sourceUrls;
    private Integer sourceUrlRate;

    //this queue is stored permanentaly scraped/imported Pins
    // Pin has a mapped query, which has a mapped board.
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true)
    private Set<Pin> queue;

    public Set<String> getDescUrls() {
        if (descUrls == null) {
            descUrls = new HashSet<String>();
        }
        return descUrls;
    }

    public void setDescUrls(Set<String> descUrls) {
        this.descUrls = descUrls;
    }

    public Integer getDescUrlRate() {
        return descUrlRate;
    }

    public void setDescUrlRate(Integer descUrlRate) {
        this.descUrlRate = descUrlRate;
    }

    public Set<String> getSourceUrls() {
        if (sourceUrls == null) {
            sourceUrls = new HashSet<String>();
        }
        return sourceUrls;
    }

    public void setSourceUrls(Set<String> sourceUrls) {
        this.sourceUrls = sourceUrls;
    }

    public Integer getSourceUrlRate() {
        return sourceUrlRate;
    }

    public void setSourceUrlRate(Integer sourceUrlRate) {
        this.sourceUrlRate = sourceUrlRate;
    }

    public Set<Pin> getQueue() {
        if (queue == null) {
            queue = new HashSet<>();
        }
        return queue;
    }

    public void setQueue(Set<Pin> queue) {
        this.queue = queue;
    }

    //hashCode & equals MUST be defined, or Campaign::HashSet<Configuration> won't work properly.
    

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PinConfiguration)) {
            return false;
        }
        PinConfiguration other = (PinConfiguration) object;
        if ( ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }
}
