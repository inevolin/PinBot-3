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

@Entity
@DiscriminatorValue(value = "Repin")
public class RepinConfiguration extends AConfiguration {

    private Set<String> descUrls;
    private Integer descUrlRate;
    private Set<String> sourceUrls;
    private Integer sourceUrlRate;
    private Long blacklisted_duration;//in milliseconds

    //this queue is stored permanentaly scraped Pins
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pin> queue;

    public Set<Pin> getQueue() {
        if (queue == null) {
            queue = new HashSet<>();
        }
        return queue;
    }

    public void setQueue(Set<Pin> queue) {
        this.queue = queue;
    }

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

    public Long getBlacklisted_duration() {
        if (blacklisted_duration == null) {
            blacklisted_duration = 0L; //don't wait.
        }
        return blacklisted_duration;
    }

    public void setBlacklisted_duration(Long blacklisted_duration) {
        this.blacklisted_duration = blacklisted_duration;
    }

    //hashCode & equals MUST be defined, or Campaign::HashSet<Configuration> won't work properly.

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RepinConfiguration)) {
            return false;
        }
        RepinConfiguration other = (RepinConfiguration) object;
        if ( ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }
}
