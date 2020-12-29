/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author UGent
 */
@Entity
@DiscriminatorValue(value = "Follow")
public class FollowConfiguration extends AConfiguration {

    private Boolean Criteria_Users;
    private int Criteria_UserFollowersMin, Criteria_UserFollowersMax;
    private int Criteria_UserFollowingMin, Criteria_UserFollowingMax;
    private int Criteria_UserBoardsMin, Criteria_UserBoardsMax;
    private int Criteria_UserPinsMin, Criteria_UserPinsMax;

    private Boolean Criteria_Boards;
    private int Criteria_BoardsFollowersMin, Criteria_BoardsFollowersMax;
    private int Criteria_BoardsPinsMin, Criteria_BoardsPinsMax;

    private Boolean followUsers, followBoards;

    public Boolean getCriteria_Users() {
        return Criteria_Users;
    }

    public void setCriteria_Users(Boolean Criteria_Users) {
        this.Criteria_Users = Criteria_Users;
    }

    public Boolean getCriteria_Boards() {
        return Criteria_Boards;
    }

    public void setCriteria_Boards(Boolean Criteria_Boards) {
        this.Criteria_Boards = Criteria_Boards;
    }

    public Boolean getFollowUsers() {
        return followUsers;
    }

    public void setFollowUsers(Boolean followUsers) {
        this.followUsers = followUsers;
    }

    public Boolean getFollowBoards() {
        return followBoards;
    }

    public void setFollowBoards(Boolean followBoards) {
        this.followBoards = followBoards;
    }

    public int getCriteria_BoardsFollowersMin() {
        return Criteria_BoardsFollowersMin;
    }

    public void setCriteria_BoardsFollowersMin(int Criteria_BoardsFollowersMin) {
        this.Criteria_BoardsFollowersMin = Criteria_BoardsFollowersMin;
    }

    public int getCriteria_BoardsFollowersMax() {
        return Criteria_BoardsFollowersMax;
    }

    public void setCriteria_BoardsFollowersMax(int Criteria_BoardsFollowersMax) {
        this.Criteria_BoardsFollowersMax = Criteria_BoardsFollowersMax;
    }

    public int getCriteria_BoardsPinsMin() {
        return Criteria_BoardsPinsMin;
    }

    public void setCriteria_BoardsPinsMin(int Criteria_BoardsPinsMin) {
        this.Criteria_BoardsPinsMin = Criteria_BoardsPinsMin;
    }

    public int getCriteria_BoardsPinsMax() {
        return Criteria_BoardsPinsMax;
    }

    public void setCriteria_BoardsPinsMax(int Criteria_BoardsPinsMax) {
        this.Criteria_BoardsPinsMax = Criteria_BoardsPinsMax;
    }

    public int getCriteria_UserFollowersMin() {
        return Criteria_UserFollowersMin;
    }

    public void setCriteria_UserFollowersMin(int Criteria_UserFollowersMin) {
        this.Criteria_UserFollowersMin = Criteria_UserFollowersMin;
    }

    public int getCriteria_UserFollowersMax() {
        return Criteria_UserFollowersMax;
    }

    public void setCriteria_UserFollowersMax(int Criteria_UserFollowersMax) {
        this.Criteria_UserFollowersMax = Criteria_UserFollowersMax;
    }

    public int getCriteria_UserFollowingMin() {
        return Criteria_UserFollowingMin;
    }

    public void setCriteria_UserFollowingMin(int Criteria_UserFollowingMin) {
        this.Criteria_UserFollowingMin = Criteria_UserFollowingMin;
    }

    public int getCriteria_UserFollowingMax() {
        return Criteria_UserFollowingMax;
    }

    public void setCriteria_UserFollowingMax(int Criteria_UserFollowingMax) {
        this.Criteria_UserFollowingMax = Criteria_UserFollowingMax;
    }

    public int getCriteria_UserBoardsMin() {
        return Criteria_UserBoardsMin;
    }

    public void setCriteria_UserBoardsMin(int Criteria_UserBoardsMin) {
        this.Criteria_UserBoardsMin = Criteria_UserBoardsMin;
    }

    public int getCriteria_UserBoardsMax() {
        return Criteria_UserBoardsMax;
    }

    public void setCriteria_UserBoardsMax(int Criteria_UserBoardsMax) {
        this.Criteria_UserBoardsMax = Criteria_UserBoardsMax;
    }

    public int getCriteria_UserPinsMin() {
        return Criteria_UserPinsMin;
    }

    public void setCriteria_UserPinsMin(int Criteria_UserPinsMin) {
        this.Criteria_UserPinsMin = Criteria_UserPinsMin;
    }

    public int getCriteria_UserPinsMax() {
        return Criteria_UserPinsMax;
    }

    public void setCriteria_UserPinsMax(int Criteria_UserPinsMax) {
        this.Criteria_UserPinsMax = Criteria_UserPinsMax;
    }

    //hashCode & equals MUST be defined, or Campaign::HashSet<Configuration> won't work properly.
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FollowConfiguration)) {
            return false;
        }
        FollowConfiguration other = (FollowConfiguration) object;
        if ( ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }
}
