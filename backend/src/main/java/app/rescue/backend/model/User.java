package app.rescue.backend.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Transient
    private Image profileImage;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "community_standing", nullable = false)
    private Long communityStanding = Long.valueOf(1);

    @Enumerated(EnumType.STRING)
    private Role userRole;

    @Column(name = "locked")
    private Boolean locked = false;

    @Column(name = "enabled")
    private Boolean enabled = false;

    /*
    idea gia edw
    o xristis pataei ena koumpi "invite a friend",
    auto dimiourgei ena link tou tipou "www.asdasf.com/signup/ref/[user_id]"
    opou user_id to UUID tou xristi,
    auto to link pigainei se mia selida gia signup kai molis oloklirwthei to signug
    me string manipulation
    e.g. k = myStr.lastIndexOf('/'); string.substring(k, string.length() - 1));
    pairnw to id kai to bazw sto pedio edw, meta dimiourgw filia anamesa stous dio xristes
     */
    @ManyToOne
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(name = "referral_token", nullable = false, unique = true)
    private String referralToken = UUID.randomUUID().toString();

    public String getReferralToken() {
        return referralToken;
    }

    public void setReferralToken(String refToken) {
        this.referralToken = refToken;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Long getCommunityStanding() {
        return communityStanding;
    }

    public void setCommunityStanding(Long communityStanding) {
        this.communityStanding = communityStanding;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Image profileImage) {
        if (profileImage.getData() != null) {
            this.profileImage = profileImage;
        }
        else {
            //TODO add default profile photo if nothing was provided
            this.profileImage = defaultProfileImage();
        }
    }

    private Image defaultProfileImage() {
        return new Image("profileImage", null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(authority);
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}