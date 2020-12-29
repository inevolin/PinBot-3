package model;

import Managers.DALManager;
import common.Http;
import model.configurations.AConfiguration;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import model.configurations.CommentConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.InviteConfiguration;
import model.configurations.LikeConfiguration;
import model.configurations.MessageConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.UnfollowConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email"})})
public class Account implements Serializable {
    // Do not remove enums; JPA could fail & throw exceptions. Use @Deprecated instead !!!

    public enum STATUS {
        LOGGEDIN, BLOCKED, BANNED, @Deprecated
        NOT_CONFIGURED, UNAUTHORIZED, BUSY, LOGIN_ERROR, ABORT_REQUEST
    };

    public Account() {
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
        }
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

    private String Email, Password, Username;

    @Transient
    transient public static int MAX_RELOGIN_ATTEMPTS = 3;
    @Transient
    transient public int reLoginAttempts = 0;
    @Transient
    public String base_url;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account")
    private Set<Campaign> campaigns;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private transient Campaign selectCampaign;

    public Campaign getSelectCampaign() {
        return selectCampaign;
    }

    protected void setSelectCampaign(Campaign selectCampaign) {
        this.selectCampaign = selectCampaign;
    }

    public Set<Campaign> getCampaigns() {
        if (campaigns == null) {
            campaigns = new HashSet<Campaign>();
        }
        return campaigns;
    }

    public void setCampaigns(Set<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    @Transient
    public void enableCampaign(Campaign camp) {
        for (Campaign c : getCampaigns()) {
            c.setSelectedCampaign(false);
        }

        camp.setSelectedCampaign(true);
        setSelectCampaign(camp);
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    private STATUS status;
    private String AppVersion;
    @Transient
    transient private Date lastlySaved, lastLogin;

    @Transient
    transient private CookieStore cookieStore;

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastlySaved() {
        return lastlySaved;
    }

    public void setLastlySaved(Date lastlySaved) {
        this.lastlySaved = lastlySaved;
    }

    public String getCsrf() {
        return Http.GetCookieVal(cookieStore, "csrftoken");
    }

    public String getAppVersion() {
        return AppVersion;
    }

    public void setAppVersion(String AppVersion) {
        this.AppVersion = AppVersion;
    }

    public STATUS getStatus() {
        if (status == null) {
            status = STATUS.UNAUTHORIZED;
        }
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, optional = true)
    private Proxy proxy;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (Email != null ? Email.hashCode() : 0);
        return hash;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "account")
    private Set<Board> boards;

    public Board findBoard(String boardId) {
        for (Board b : getBoards()) { //don't use stream().filter() for Entity methods, even if transient !!!!
            if (b.getBoardId().equalsIgnoreCase(boardId)) {
                return b;
            }
        }
        return null;
    }

    public Set<Board> getBoards() {
        if (boards == null) {
            boards = new HashSet<>();
        }
        return boards;
    }

    public void setBoards(Set<Board> boards) {
        this.boards = boards;
    }

    //////////////////////////////////////////
    /////////////// DUPLICATES ///////////////
    //////////////////////////////////////////
    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_PIN")
    private Set<PinterestObject> duplicates_pin;
    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_REPIN")
    private Set<PinterestObject> duplicates_repin;
    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_LIKE")
    private Set<PinterestObject> duplicates_like;
    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_INVITE")
    private Set<PinterestObject> duplicates_invite;
    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_COMMENT")
    private Set<PinterestObject> duplicates_comment;

    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_MESSAGE")
    private Set<PinterestObject> duplicates_message;

    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_FOLLOW")
    private Set<PinterestObject> duplicates_follow;

    @Deprecated
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_UNFOLLOW")
    private Set<PinterestObject> duplicates_unfollow;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "DUPS_INTEREST")
    private transient Set<PinterestObject> duplicates_interest;

    public Set<PinterestObject> getDuplicates_interest() {
        if (duplicates_interest == null) {
            duplicates_interest = new HashSet<>();
        }
        return duplicates_interest;
    }

    public void setDuplicates_interest(Set<PinterestObject> duplicates_interest) {
        this.duplicates_interest = duplicates_interest;
    }

    public void clearDuplicates(AConfiguration cf) {
        //testThis
        if (cf instanceof PinConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_pin, this);
        } else if (cf instanceof RepinConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_repin, this);
        } else if (cf instanceof LikeConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_like, this);
        } else if (cf instanceof InviteConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_invite, this);
        } else if (cf instanceof CommentConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_comment, this);
        } else if (cf instanceof MessageConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_message, this);
        } else if (cf instanceof FollowConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_follow_pinners, this);
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_follow_boards, this);
        } else if (cf instanceof UnfollowConfiguration) {
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_unfollow_pinners, this);
            pinbot3.PinBot3.dalMgr.clearObjectsExernally(DALManager.TYPES.duplicates_unfollow_boards, this);
        }
    }

    public void addDuplicate(AConfiguration cf, PinterestObject obj) {
        //testThis
        if (cf instanceof PinConfiguration) {
            pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_pin, this);
        } else if (cf instanceof RepinConfiguration) {
            pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_repin, this);
        } else if (cf instanceof LikeConfiguration) {
            pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_like, this);
        } else if (cf instanceof InviteConfiguration) {
            pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_invite, this);
        } else if (cf instanceof CommentConfiguration) {
            pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_comment, this);
        } else if (cf instanceof MessageConfiguration) {
            pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_message, this);
        } else if (cf instanceof FollowConfiguration) {
            if (obj instanceof Pinner) {
                pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_follow_pinners, this);
            } else if (obj instanceof Board) {
                pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_follow_boards, this);
            }
        } else if (cf instanceof UnfollowConfiguration) {
            if (obj instanceof Pinner) {
                pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_unfollow_pinners, this);
            } else if (obj instanceof Board) {
                pinbot3.PinBot3.dalMgr.appendExternalObject(obj, DALManager.TYPES.duplicates_unfollow_boards, this);
            }
        }
    }

    public PinterestObject getDuplicate(AConfiguration cf, PinterestObject obj) {
        //testThis
        PinterestObject found = null;
        if (cf instanceof PinConfiguration) {
            found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_pin, this);
        } else if (cf instanceof RepinConfiguration) {
            found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_repin, this);
        } else if (cf instanceof LikeConfiguration) {
            found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_like, this);
        } else if (cf instanceof InviteConfiguration) {
            found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_invite, this);
        } else if (cf instanceof CommentConfiguration) {
            found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_comment, this);
        } else if (cf instanceof MessageConfiguration) {
            found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_message, this);
        } else if (cf instanceof FollowConfiguration) {
            if (obj instanceof Pinner) {
                found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_follow_pinners, this);
            } else if (obj instanceof Board) {
                found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_follow_boards, this);
            }
        } else if (cf instanceof UnfollowConfiguration) {
            if (obj instanceof Pinner) {
                found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_unfollow_pinners, this);
            } else if (obj instanceof Board) {
                found = pinbot3.PinBot3.dalMgr.getObjectExternally(obj, DALManager.TYPES.duplicates_unfollow_boards, this);
            }
        }
        return found;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_pin() {
        if (duplicates_pin == null) {
            duplicates_pin = new HashSet<>();
        }
        return duplicates_pin;
    }

    @Deprecated
    protected void setDuplicates_pin(Set<PinterestObject> duplicates_pin) {
        this.duplicates_pin = duplicates_pin;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_repin() {
        if (duplicates_repin == null) {
            duplicates_repin = new HashSet<>();
        }
        return duplicates_repin;
    }

    @Deprecated
    protected void setDuplicates_repin(Set<PinterestObject> duplicates_repin) {
        this.duplicates_repin = duplicates_repin;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_like() {
        if (duplicates_like == null) {
            duplicates_like = new HashSet<>();
        }
        return duplicates_like;
    }

    @Deprecated
    protected void setDuplicates_like(Set<PinterestObject> duplicates_like) {
        this.duplicates_like = duplicates_like;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_invite() {
        if (duplicates_invite == null) {
            duplicates_invite = new HashSet<>();
        }
        return duplicates_invite;
    }

    @Deprecated
    protected void setDuplicates_invite(Set<PinterestObject> duplicates_invite) {
        this.duplicates_invite = duplicates_invite;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_comment() {
        if (duplicates_comment == null) {
            duplicates_comment = new HashSet<>();
        }
        return duplicates_comment;
    }

    @Deprecated
    protected void setDuplicates_comment(Set<PinterestObject> duplicates_comment) {
        this.duplicates_comment = duplicates_comment;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_message() {
        if (duplicates_message == null) {
            duplicates_message = new HashSet<>();
        }
        return duplicates_message;
    }

    @Deprecated
    protected void setDuplicates_message(Set<PinterestObject> duplicates_message) {
        this.duplicates_message = duplicates_message;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_follow() {
        if (duplicates_follow == null) {
            duplicates_follow = new HashSet<>();
        }
        return duplicates_follow;
    }

    @Deprecated
    protected void setDuplicates_follow(Set<PinterestObject> duplicates_follow) {
        this.duplicates_follow = duplicates_follow;
    }

    @Deprecated
    public Set<PinterestObject> getDuplicates_unfollow() {
        if (duplicates_unfollow == null) {
            duplicates_unfollow = new HashSet<>();
        }
        return duplicates_unfollow;
    }

    @Deprecated
    protected void setDuplicates_unfollow(Set<PinterestObject> duplicates_unfollow) {
        this.duplicates_unfollow = duplicates_unfollow;
    }

    //////////////////////////////////////////
    //////////////////////////////////////////
    //////////////////////////////////////////
    //////////////////////////////////////////
    //needs tags for the database 
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "MY_FOLLOWING")
    private Set<Pinner> following;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "MY_FOLLOWERS")
    private Set<Pinner> followers;

    private transient Set<Pinner> getMyFollowing;

    public Set<Pinner> myFollowing() {
        if (getMyFollowing == null) {
            getMyFollowing = new HashSet<>();
            Set<PinterestObject> objs = pinbot3.PinBot3.dalMgr.getExternalObjects(DALManager.TYPES.following, this);
            for (PinterestObject o : objs) {
                getMyFollowing.add((Pinner) o);
            }
        }
        return getMyFollowing;
    }
    private transient Set<Pinner> getMyFollowers;

    public Set<Pinner> myFollowers() {
        if (getMyFollowers == null) {
            getMyFollowers = new HashSet<>();
            Set<PinterestObject> objs = pinbot3.PinBot3.dalMgr.getExternalObjects(DALManager.TYPES.followers, this);
            for (PinterestObject o : objs) {
                getMyFollowers.add((Pinner) o);
            }
        }
        return getMyFollowers;
    }

    @Deprecated
    public Set<Pinner> getFollowing() {
        if (following == null) {
            following = new HashSet<>();
        }
        return following;
    }

    @Deprecated
    public void setFollowing(Set<Pinner> following) {
        this.following = following;
    }

    @Deprecated
    public Set<Pinner> getFollowers() {
        if (followers == null) {
            followers = new HashSet<>();
        }
        return followers;
    }

    @Deprecated
    public void setFollowers(Set<Pinner> followers) {
        this.followers = followers;
    }

    //////////////////////////////////////////    
    private int myFollowing, myFollowers, myLikes, myBoards, myPins;

    @Transient
    transient private int myInterests;

    public int getMyInterests() {
        return myInterests;
    }

    public void setMyInterests(int myInterests) {
        this.myInterests = myInterests;
    }

    public int getMyLikes() {
        return myLikes;
    }

    public void setMyLikes(int myLikes) {
        this.myLikes = myLikes;
    }

    public int getMyBoards() {
        return myBoards;
    }

    public void setMyBoards(int myBoards) {
        this.myBoards = myBoards;
    }

    public int getMyPins() {
        return myPins;
    }

    public void setMyPins(int myPins) {
        this.myPins = myPins;
    }

    public int getMyFollowing() {
        return myFollowing;
    }

    public void setMyFollowing(int myFollowing) {
        this.myFollowing = myFollowing;
    }

    public int getMyFollowers() {
        return myFollowers;
    }

    public void setMyFollowers(int myFollowers) {
        this.myFollowers = myFollowers;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Account)) {
            return false;
        }
        Account other = (Account) object;
        if ((this.Email == null && other.Email != null) || (this.Email != null && !this.Email.equals(other.Email))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Email;
    }

}
