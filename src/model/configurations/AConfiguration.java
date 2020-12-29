/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations;

import model.configurations.queries.AQuery;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 *
 * @author UGent
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
        name = "configType",
        discriminatorType = DiscriminatorType.STRING
)
public abstract class AConfiguration implements Serializable {

    @Transient
    transient public Boolean isInterrupt = false;

    public AConfiguration() {
        queries = new ArrayList<>();
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //used by CampaignManager
    public enum RunStatus {
        IDLE, ACTIVE, TIMEOUT, FINISHED, ERROR, SLEEPING, PREMATURE_FINISH
    };
    @Transient
    transient public Integer CountCompleted;
    @Transient
    transient public Integer CountTotal;
    @Transient
    transient public RunStatus status;
    @Transient
    transient public LocalDateTime runNext;
    @Transient
    transient public Future<?> future; //to abort thread
    @Transient
    transient public final static Integer MaxErrorCount = 8;//if in a single 'run' more than 8 failures, abort algo (each pin may fail twice, if 4 pins failed then stop).
    @Transient
    transient public Integer ErrorCount = 0;
    //////

    private Boolean Autopilot;

    private LocalTime AutopilotStart;

    private Boolean isActive;

    private Integer TimeoutMin;

    private Integer TimeoutMax;

    private Integer actionMin;

    private Integer actionMax;

    private Integer scrapeAmount; //used for EditQueue

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)//
    private List<AQuery> queries; //our search queries; this may be empty/null for pin/repin/unfollow of course.

    public List<AQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<AQuery> queries) {
        this.queries = queries;
    }

    public Integer getActionMin() {
        return actionMin;
    }

    public void setActionMin(Integer actionMin) {
        this.actionMin = actionMin;
    }

    public Integer getActionMax() {
        return actionMax;
    }

    public void setActionMax(Integer actionMax) {
        this.actionMax = actionMax;
    }

    public Integer getTimeoutMin() {
        return TimeoutMin;
    }

    public void setTimeoutMin(Integer TimeoutMin) {
        this.TimeoutMin = TimeoutMin;
    }

    public Integer getTimeoutMax() {
        return TimeoutMax;
    }

    public void setTimeoutMax(Integer TimeoutMax) {
        this.TimeoutMax = TimeoutMax;
    }

    public Integer getScrapeAmount() {
        return scrapeAmount;
    }

    public void setScrapeAmount(Integer scrapeAmount) {
        this.scrapeAmount = scrapeAmount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getAutopilot() {
        return Autopilot;
    }

    public void setAutopilot(Boolean Autopilot) {
        this.Autopilot = Autopilot;
    }

    public LocalTime getAutopilotStart() {
        return AutopilotStart;
    }

    public void setAutopilotStart(LocalTime AutopilotStart) {
        this.AutopilotStart = AutopilotStart;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        Class c = this.getClass();
        hash += this.getId() == null ? 0 : this.getId();
        hash += c.getName().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AConfiguration)) {
            return false;
        }

        Class c = this.getClass();
        AConfiguration other = ((AConfiguration) object);
        Class oc = other.getClass();
        if (((c.getName() == null && oc.getName() != null) || (c.getName() != null && !c.getName().equals(oc.getName())))) {
            return false;
        } else if (((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Configurations.AConfiguration [ id=" + id + " ]";
    }

}
