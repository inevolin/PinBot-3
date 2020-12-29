/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pinterestobjects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import model.configurations.queries.AQuery;

@Entity
@DiscriminatorValue(value = "Interest")
public class Interest extends PinterestObject {

    private String id;
    private String url_name;
    private boolean followedByMe;
    private long timeFollow;

    protected Interest() {
    }

    public Interest(PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);
    }

    public Interest(String url_name, boolean followedByMe, long timeFollow, PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);
        this.url_name = url_name;
        this.followedByMe = followedByMe;
        this.timeFollow = timeFollow;
    }

    public Interest(Interest other, AQuery parent) {
        super(other, parent);
        this.followedByMe = other.isFollowedByMe();
        this.timeFollow = other.getTimeFollow();
        this.url_name = other.getUrl_name();
    }

    @Override
    public Interest copy(AQuery parent) {
        return new Interest(this, parent);
    }

    public String getUrl_name() {
        return url_name;
    }

    public void setUrl_name(String url_name) {
        this.url_name = url_name;
    }

    public boolean isFollowedByMe() {
        return followedByMe;
    }

    public void setFollowedByMe(boolean followedByMe) {
        this.followedByMe = followedByMe;
    }

    public long getTimeFollow() {
        return timeFollow;
    }

    public void setTimeFollow(long timeFollow) {
        this.timeFollow = timeFollow;
    }

    //hashCode & equals MUST be defined, or Account::HashSet<Pinner> won't work properly.
    @Override
    public int hashCode() {
        int hash = 0;
        //hash += (getId() != null ? getId().hashCode() : 0);
        hash += (getUrl_name() != null ? getUrl_name().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Interest)) {
            return false;
        }
        Interest other = (Interest) object;
        if ((this.getUrl_name() == null && other.getUrl_name() != null) || (this.getUrl_name() != null && !this.getUrl_name().equals(other.getUrl_name()))) {
            return false;
        }
        return true;
    }
}
