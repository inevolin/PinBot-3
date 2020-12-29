/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pinterestobjects;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;
import model.configurations.queries.AQuery;

/**
 *
 * @author UGent
 */
@Entity
@DiscriminatorValue(value = "Pins")
public class Pin extends PinterestObject {

    @Lob
    private String hashUrl = "", pinUrl = "";
    @Lob
    private String description = "";
    @Lob
    private String sourceUrl = "";

    private String pinId;
    private String username;
    private String boardName_EXT;
    private String boardId_EXT;
    private int attempts = 0; //when pinning/repinning, how many failed attempts before removing.
    private Long timeBeforeNextPin; //in ms
    private boolean likedByMe;
    private String destinationBoardId; // instead of mapping [PinObj]<->[AQuery] ; each [Pin] will have this field to get mapped board by its Pinterest-ID.
    @Transient
    transient private Date previouslyPinned; //we use this for persistent pins that are not deleted after being pinned; but pinned again after X time.

    protected Pin() {
    }

    public Pin(PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);
        this.destinationBoardId = mappedQuery == null || mappedQuery.getBoardMapped() == null || ((Board) mappedQuery.getBoardMapped()).getBoardId() == null ? null : ((Board) mappedQuery.getBoardMapped()).getBoardId();
    }

    public Pin(AQuery mappedQuery, String pinId, String username, String boardName_EXT, String boardId_EXT, Long timeBeforeNextPin, boolean likedByMe, Date previouslyPinned, PinterestObjectResources resource, String destinationBoardId) {
        super(resource, mappedQuery);
        this.pinId = pinId;
        this.username = username;
        this.boardName_EXT = boardName_EXT;
        this.boardId_EXT = boardId_EXT;
        this.timeBeforeNextPin = timeBeforeNextPin;
        this.likedByMe = likedByMe;
        this.previouslyPinned = previouslyPinned;
        this.destinationBoardId = destinationBoardId;
    }

    public Pin(Pin otherPin, AQuery parent) {
        super(otherPin, parent);
        this.destinationBoardId = otherPin.getDestinationBoardId();
        this.pinId = otherPin.getPinId();
        this.username = otherPin.getUsername();
        this.boardName_EXT = otherPin.getBoardName_EXT();
        this.boardId_EXT = otherPin.getBoardId_EXT();
        this.timeBeforeNextPin = otherPin.getTimeBeforeNextPin();
        this.likedByMe = otherPin.isLikedByMe();
        this.previouslyPinned = otherPin.getPreviouslyPinned();
        this.hashUrl = otherPin.getHashUrl();
        this.pinUrl = otherPin.getPinUrl();
        this.description = otherPin.getDescription();
        this.sourceUrl = otherPin.getSourceUrl();
        this.attempts = otherPin.getAttempts();
    }

    @Override
    public Pin copy(AQuery parent) {
        return new Pin(this, parent);
    }

    public String getDestinationBoardId() {
        return destinationBoardId;
    }

    public void setDestinationBoardId(String destinationBoardId) {
        this.destinationBoardId = destinationBoardId;
    }

    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId = pinId;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Long getTimeBeforeNextPin() {
        return timeBeforeNextPin;
    }

    public void setTimeBeforeNextPin(Long timeBeforeNextPin) {
        this.timeBeforeNextPin = timeBeforeNextPin;
    }

    public Date getPreviouslyPinned() {
        return previouslyPinned;
    }

    public void setPreviouslyPinned(Date previouslyPinned) {
        this.previouslyPinned = previouslyPinned;
    }

    @Lob
    public String getSourceUrl() {
        return sourceUrl;
    }

    @Lob
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHashUrl() {
        return hashUrl;
    }

    public void setHashUrl(String hashUrl) {
        this.hashUrl = hashUrl;
    }

    public String getPinUrl() {
        return pinUrl;
    }

    public void setPinUrl(String pinUrl) {
        this.pinUrl = pinUrl;
    }

    public boolean isLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        this.likedByMe = likedByMe;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBoardName_EXT() {
        return boardName_EXT;
    }

    public void setBoardName_EXT(String boardName_EXT) {
        this.boardName_EXT = boardName_EXT;
    }

    public String getBoardId_EXT() {
        return boardId_EXT;
    }

    public void setBoardId_EXT(String boardId_EXT) {
        this.boardId_EXT = boardId_EXT;
    }

    //hashCode & equals MUST be defined, or Account::HashSet<Board> won't work properly.
    @Override
    public int hashCode() {
        int hash = 0;
        //we cannot use Id, otherwise DupChecker won't work (scraped objects have no ID; existing ones do).
        //hash += (getId() != null ? getId().hashCode() : 0);
        hash += (getHashUrl() != null ? getHashUrl().hashCode() : 0);
        //hash += (getPinUrl() != null ? getPinUrl().hashCode() : 0); 
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Pin)) {
            return false;
        }
        Pin other = (Pin) object;
        boolean equal = true;
        //make sure hashUrl and pinId are different
        if (((this.getHashUrl() == null && other.getHashUrl() != null) || (this.getHashUrl() != null && !this.getHashUrl().equals(other.getHashUrl())))
                && ((this.getPinId() == null & other.getPinId() != null) || (this.getPinId() != null && !this.getPinId().equals(other.getPinId())))) {
            if (    this.getHashUrl() != null && this.getHashUrl().contains(".") && this.getHashUrl().contains("/") &&
                    other.getHashUrl() != null && other.getHashUrl().contains(".") && other.getHashUrl().contains("/")
                ) { //make sure filenames (of images) are different
                String filenameThis = this.getHashUrl().substring(this.getHashUrl().lastIndexOf('/') + 1, this.getHashUrl().lastIndexOf('.'));
                String filenameOther = other.getHashUrl().substring(other.getHashUrl().lastIndexOf('/') + 1, other.getHashUrl().lastIndexOf('.'));
                if (!filenameThis.equals(filenameOther)) {
                    equal = false;
                }
            } else {
                equal = false;
            }
        }
        return equal; //any of the three criteria (hashUrl, pinId, filename) are equal, so we skip this one.
        //important: filename of image could be the same for external sources (nonPinterest!!!)
    }
}
