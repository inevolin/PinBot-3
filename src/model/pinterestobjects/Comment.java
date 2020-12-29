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

@Entity
@DiscriminatorValue(value = "Comments")
public class Comment extends PinterestObject {

    @Lob
    private String username, text;
    private String commentId;
    private String searchQuery; //required for Comment GUI

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    protected Comment() {
    }

    public Comment(PinterestObjectResources resource, AQuery mappedQuery) {
        super(resource, mappedQuery);
        if (mappedQuery != null) {
            this.searchQuery = mappedQuery.getQuery();
        }
    }

    public Comment(String username, String text, String commentId, AQuery mappedQuery, PinterestObjectResources resource) {
        super(resource, mappedQuery);
        this.username = username;
        this.text = text;
        this.commentId = commentId;
        if (mappedQuery != null) {
            this.searchQuery = mappedQuery.getQuery();
        }
    }

    public Comment(Comment otherComment, AQuery parent) {
        super(otherComment, parent);
        this.username = otherComment.getUsername();
        this.text = otherComment.getText();
        this.commentId = otherComment.getCommentId();
        if (otherComment.mappedQuery != null) {
            this.searchQuery = otherComment.mappedQuery.getQuery();
        }
    }

    @Override
    public Comment copy(AQuery parent) {
        return new Comment(this, parent);
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //hashCode & equals MUST be defined, or Account::HashSet<Board> won't work properly.
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getText() != null ? getText().hashCode() : 0);
        //hash += (getMappedQuery() != null ? getMappedQuery().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Comment)) {
            return false;
        }
        Comment other = (Comment) object;

        if ((this.getText() == null && other.getText() != null) || (this.getText() != null && !this.getText().equals(other.getText()))) {
            return false;
        }
        /*else if ((this.getMappedQuery() == null && other.getMappedQuery() != null) || (this.getMappedQuery() != null && !this.getMappedQuery().equals(other.getMappedQuery()))) {
            return false;
        }*/
        return true;
    }

}
