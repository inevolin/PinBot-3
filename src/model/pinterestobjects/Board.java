/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pinterestobjects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import model.configurations.queries.AQuery;

/**
 *
 * @author UGent
 */
@Entity
@DiscriminatorValue(value = "Boards")
public class Board extends PinterestObject {

    @Lob
    private String name, urlName;
    private String boardId;
    private String userId, username;
    private boolean followedByMe;
    private int attempts = 0;
    private int pinsCount;
    private String description, category; //for boards Manager

    public Board() {

    }

    public Board(PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);        
    }

    public Board(String name, String urlName, String boardId, String userId, String username, AQuery mappedQuery, boolean followedByMe, int pinsCount, PinterestObjectResources resource) {
        super(resource, mappedQuery);
        this.name = name;
        this.urlName = urlName;
        this.boardId = boardId;
        this.userId = userId;
        this.username = username;
        this.followedByMe = followedByMe;
        this.pinsCount = pinsCount;
    }

    public Board(Board otherBoard, AQuery parent) {
        super(otherBoard, parent);
        this.name = otherBoard.getName();
        this.urlName = otherBoard.getUrlName();
        this.boardId = otherBoard.getBoardId();
        this.userId = otherBoard.getUserId();
        this.username = otherBoard.getUsername();
        this.followedByMe = otherBoard.isFollowedByMe();
        this.pinsCount = otherBoard.getPinsCount();
        this.attempts = otherBoard.getAttempts();
        this.category = otherBoard.getCategory();
        this.description = otherBoard.getDescription();
    }

    @Override
    public Board copy(AQuery parent) {
        return new Board(this, parent);
    }

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isFollowedByMe() {
        return followedByMe;
    }

    public void setFollowedByMe(boolean followedByMe) {
        this.followedByMe = followedByMe;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getPinsCount() {
        return pinsCount;
    }

    public void setPinsCount(int pinsCount) {
        this.pinsCount = pinsCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    //hashCode & equals MUST be defined, or Account::HashSet<Board> won't work properly.
    @Override
    public int hashCode() {
        int hash = 0;
        //hash += (getId() != null ? getId().hashCode() : 0);
        hash += (getBoardId() != null ? getBoardId().hashCode() : 0);
        hash += (getName() != null ? getName().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Board)) {
            return false;
        }
        Board other = (Board) object;

        if ((this.getBoardId() == null && other.getBoardId() != null) || (this.getBoardId() != null && !this.getBoardId().equals(other.getBoardId()))) {
            return false;
        } else if ((this.getName() == null && other.getName() != null) || (this.getName() != null && !this.getName().equals(other.getName()))) {
            return false;
        }
        return true;
    }
}
