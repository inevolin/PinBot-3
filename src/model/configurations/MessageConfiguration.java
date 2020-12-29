/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author UGent
 */
@Entity
@DiscriminatorValue(value = "Message")
public class MessageConfiguration extends AConfiguration {

    private Boolean Criteria_Users;

    private int Criteria_UserFollowersMin;

    private int Criteria_UserFollowersMax;

    private int Criteria_UserFollowingMin;

    private int Criteria_UserFollowingMax;

    private int Criteria_UserBoardsMin;

    private int Criteria_UserBoardsMax;

    private int Criteria_UserPinsMin;

    private int Criteria_UserPinsMax;

    private Boolean messageNonFollowers; // message all who are not following us back.

    private Boolean messageOnlyRecordedFollowings; // message only if PinBot has followed them + recorded timestmap when followed (in 'duplicates' field)

    private Long timeBetweenFollowAndMessage; //used with 'unfollowOnlyRecordedFollowings' to check how much time should be passed after following, before messaging them.

    private List<String> messages;

    public Boolean getCriteria_Users() {
        return Criteria_Users;
    }

    public void setCriteria_Users(Boolean Criteria_Users) {
        this.Criteria_Users = Criteria_Users;
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

    public Boolean getMessageNonFollowers() {
        return messageNonFollowers;
    }

    public void setMessageNonFollowers(Boolean messageNonFollowers) {
        this.messageNonFollowers = messageNonFollowers;
    }

    public Boolean getMessageOnlyRecordedFollowings() {
        return messageOnlyRecordedFollowings;
    }

    public void setMessageOnlyRecordedFollowings(Boolean messageOnlyRecordedFollowings) {
        this.messageOnlyRecordedFollowings = messageOnlyRecordedFollowings;
    }

    public Long getTimeBetweenFollowAndMessage() {
        return timeBetweenFollowAndMessage;
    }

    public void setTimeBetweenFollowAndMessage(Long timeBetweenFollowAndMessage) {
        this.timeBetweenFollowAndMessage = timeBetweenFollowAndMessage;
    }

    public List<String> getMessages() {
        if (messages == null) {
            return new ArrayList<String>();
        }
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    //hashCode & equals MUST be defined, or Campaign::HashSet<Configuration> won't work properly.
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MessageConfiguration)) {
            return false;
        }
        MessageConfiguration other = (MessageConfiguration) object;
        if (((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }
}
