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
@DiscriminatorValue(value = "Pinners")
public class Pinner extends PinterestObject {

    private String username;
    private String baseUsername;
    private int pinsCount;
    private int followersCount;
    private int followingCount;
    private int boardsCount;
    private int attempts = 0; //when inviting, how many failed attempts before removing.
    private String pinnerId;
    private boolean followedByMe;
    private long timeFollow;

    protected Pinner() {
    }

    public Pinner(PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);
    }

    public Pinner(String username, String baseUsername, AQuery mappedQuery, int pinsCount, int followersCount, int followingCount, int boardsCount, String pinnerId, boolean followedByMe, long timeFollow, PinterestObjectResources resource) {
        super(resource, mappedQuery);
        this.username = username;
        this.baseUsername = baseUsername;
        this.pinsCount = pinsCount;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.boardsCount = boardsCount;
        this.pinnerId = pinnerId;
        this.followedByMe = followedByMe;
        this.timeFollow = timeFollow;
    }

    public Pinner(Pinner otherPinner, AQuery parent) {
        super(otherPinner, parent);
        this.username = otherPinner.getUsername();
        this.baseUsername = otherPinner.getBaseUsername();
        this.pinsCount = otherPinner.getPinsCount();
        this.followersCount = otherPinner.getFollowersCount();
        this.followingCount = otherPinner.getFollowingCount();
        this.boardsCount = otherPinner.getBoardsCount();
        this.pinnerId = otherPinner.getPinnerId();
        this.followedByMe = otherPinner.isFollowedByMe();
        this.timeFollow = otherPinner.getTimeFollow();
        this.attempts = otherPinner.getAttempts();
    }

    @Override
    public Pinner copy(AQuery parent) {
        return new Pinner(this, parent);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBaseUsername() {
        return baseUsername;
    }

    public void setBaseUsername(String baseUsername) {
        this.baseUsername = baseUsername;
    }

    public int getPinsCount() {
        return pinsCount;
    }

    public void setPinsCount(int pinsCount) {
        this.pinsCount = pinsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getBoardsCount() {
        return boardsCount;
    }

    public void setBoardsCount(int boardsCount) {
        this.boardsCount = boardsCount;
    }

    public boolean isFollowedByMe() {
        return followedByMe;
    }

    public void setFollowedByMe(boolean followedByMe) {
        this.followedByMe = followedByMe;
    }

    public String getPinnerId() {
        return pinnerId;
    }

    public void setPinnerId(String pinnerId) {
        this.pinnerId = pinnerId;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
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
        hash += (getPinnerId() != null ? getPinnerId().hashCode() : 0);
        hash += (getUsername() != null ? getUsername().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Pinner)) {
            return false;
        }
        Pinner other = (Pinner) object;
        if ((this.getPinnerId() == null && other.getPinnerId() != null) || (this.getPinnerId() != null && !this.getPinnerId().equals(other.getPinnerId()))) {
            return false;
        } else if ((this.getUsername() == null && other.getUsername() != null) || (this.getUsername() != null && !this.getUsername().equals(other.getUsername()))) {
            return false;
        }
        return true;
    }
}
