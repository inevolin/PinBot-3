/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pinterestobjects;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import model.configurations.queries.AQuery;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
        name = "objType",
        discriminatorType = DiscriminatorType.STRING
)
public abstract class PinterestObject implements Serializable {

    public static enum PinterestObjectResources {
        SearchResource, BoardFeedResource, UserPinsResource, IndividualPin, UserFollowersResource, UserFollowingResource, BoardFollowersResource, BoardFollowingResource, ProfileBoardsResource, External, PinCommentListResource, IndividualUser, CategoriesResource,
        PinLikesResource, // ScrapePinners_AggregatedActivityFeedResource
        RepinFeedResource, // ScrapeBoards_AggregatedActivityFeedResource
        InterestFollowingResource, //#followingCount =  #FollowingUsers + #Topics/interest
    };

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timespan = 0L; //time when the object was created (0L if it's prior DBv3)

    public Long getTimespan() {
        return timespan;
    }

    public void setTimespan(Long timespan) {
        this.timespan = timespan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //meaning MappedBy:=
    //PinterestObject is NOT the owner of the relationship, but mappedQuery is!!!
    //meaning: we cannot delete a PinterestObject if it has a mappedQuery in some collection.
    //@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true, orphanRemoval = true, mappedBy = "boardMapped")
    //@JoinColumn(name = "MAPPEDQUERY_ID")
    @Transient
    transient protected AQuery mappedQuery;

    /**
     * Transient Used only by Algos -- not stored in database!
     */
    public AQuery getMappedQuery() {
        return mappedQuery;
    }

    /**
     * Transient Used only by Algos -- not stored in database!
     */
    public void setMappedQuery(AQuery mappedQuery) {
        //use this only to remove (cascade)
        this.mappedQuery = mappedQuery; //it should be a copy ==> use constructor instead!!
    }

    protected PinterestObject() {
        timespan = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
    }

    public PinterestObject(PinterestObjectResources resource, AQuery mappedQuery) {
        timespan = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        this.resource = resource;
        this.mappedQuery = mappedQuery;
        if (mappedQuery != null) {
            this.mappedQuery = mappedQuery.copy(this);
        }
    }

    public PinterestObject(PinterestObject otherPinterestObject, AQuery parent) {
        timespan = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        this.resource = otherPinterestObject.getResource();
        if (otherPinterestObject.mappedQuery != null && parent != null && !parent.equals(otherPinterestObject.mappedQuery)) {
            this.mappedQuery = otherPinterestObject.mappedQuery.copy(otherPinterestObject);
        }
    }

    //parameter to prevent cyclic loop
    abstract public PinterestObject copy(AQuery parent);

    private PinterestObjectResources resource;

    public PinterestObjectResources getResource() {
        return resource;
    }

    public void setResource(PinterestObjectResources resource) {
        this.resource = resource;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        Class c = this.getClass();
        hash += c.getName().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PinterestObject)) {
            return false;
        }

        Class c = this.getClass();
        Class oc = ((PinterestObject) object).getClass();
        if (((c.getName() == null && oc.getName() != null) || (c.getName() != null && !c.getName().equals(oc.getName())))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.pinterestobjects.PinterestObject [ id=" + id + " ]";
    }

}
