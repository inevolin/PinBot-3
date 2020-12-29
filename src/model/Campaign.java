/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import model.configurations.AConfiguration;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 *
 * @author UGent
 */
@Entity
public class Campaign implements Serializable {

    public Campaign() {

    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean selectedCampaign;

    private String campaignName;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "campaign")
    private Set<AConfiguration> configurations;

    public boolean isSelectedCampaign() {
        return selectedCampaign;
    }

    public void setSelectedCampaign(boolean selectedCampaign) {
        this.selectedCampaign = selectedCampaign;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public Set<AConfiguration> getConfigurations() {
        if (configurations == null) {
            configurations = new HashSet<AConfiguration>();
        }
        return configurations;
    }

    public void setConfigurations(Set<AConfiguration> configurations) {
        this.configurations = configurations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (campaignName != null ? campaignName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Campaign)) {
            return false;
        }
        Campaign other = (Campaign) object;
        if ((this.campaignName == null && other.campaignName != null) || (this.campaignName != null && !this.campaignName.equals(other.campaignName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Campaign[ id=" + id + " ]";
    }

}
