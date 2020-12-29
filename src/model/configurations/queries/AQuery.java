/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations.queries;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;

@Entity
public class AQuery implements Serializable {

    protected AQuery() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /*
        The query variable holds the entered query by the user.
     */
    private String query; // example: /some_username/following/people/
    //   This variable indicates whether the End has been Reached for this specific query while doing a scrape (see ScrapeSession for more info).     
    private Boolean blacklisted;
    private Long timespan_blacklisted;
    private PinterestObject.PinterestObjectResources resource;

    public Long getTimespan_blacklisted() {
        return timespan_blacklisted;
    }
    // which board (or null) is mapped to this query. Used in Pin/Repin/Invite features.
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    @JoinColumn(name = "BOARDMAPPED_ID")
    private Board boardMapped;

    public Board getBoardMapped() {
        return boardMapped;
    }

    public void setBoardMapped(Board boardMapped) {
        this.boardMapped = boardMapped;
    }

    public Boolean getBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(Boolean blacklisted) {
        this.blacklisted = blacklisted;
        if (blacklisted) {
            Date now = new Date();
            timespan_blacklisted = now.getTime();
        }
    }

    public AQuery(String query, Board boardMapped, PinterestObject.PinterestObjectResources res) {
        this.query = query;
        if (boardMapped != null) {
            this.boardMapped = boardMapped.copy(this); // !!!!
        }
        this.resource = res;
    }

    public AQuery(AQuery aQuery, PinterestObject parent) {
        //this.id = aQuery.getId();
        this.query = aQuery.getQuery();
        this.blacklisted = aQuery.getBlacklisted();
        this.timespan_blacklisted = aQuery.getTimespan_blacklisted();
        this.resource = aQuery.getResource();
        if (aQuery.boardMapped != null && parent != null && !parent.equals(aQuery.boardMapped)) {
            this.boardMapped = aQuery.boardMapped.copy(aQuery); // !!!!
        } else if (aQuery.getBoardMapped() != null) {
            this.boardMapped = aQuery.getBoardMapped().copy(null); //copy!!!
        }
    }

    public AQuery copy(PinterestObject parent) {
        return new AQuery(this, parent);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public PinterestObject.PinterestObjectResources getResource() {
        return resource;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (query != null ? query.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AQuery)) {
            return false;
        }
        AQuery other = (AQuery) object;
        if ((this.query == null && other.query != null) || (this.query != null && !this.query.equals(other.query))) {
            return false;
        }
        return true;
    }

}
