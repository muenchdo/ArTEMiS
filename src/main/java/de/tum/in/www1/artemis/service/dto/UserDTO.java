package de.tum.in.www1.artemis.service.dto;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import de.tum.in.www1.artemis.config.Constants;
import de.tum.in.www1.artemis.domain.Authority;
import de.tum.in.www1.artemis.domain.GuidedTourSetting;
import de.tum.in.www1.artemis.domain.User;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserDTO extends AuditingEntityDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 100)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 6)
    private String langKey;

    private ZonedDateTime lastNotificationRead;

    private Set<String> authorities;

    private Set<String> groups;

    private Set<GuidedTourSetting> guidedTourSettings;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this(user.getId(), user.getLogin(), user.getName(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getActivated(), user.getImageUrl(), user.getLangKey(),
                user.getCreatedBy(), user.getCreatedDate(), user.getLastModifiedBy(), user.getLastModifiedDate(), user.getLastNotificationRead(),
                user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet()), user.getGroups(), user.getGuidedTourSettings());
    }

    public UserDTO(Long id, String login, String name, String firstName, String lastName, String email, boolean activated, String imageUrl, String langKey, String createdBy,
            Instant createdDate, String lastModifiedBy, Instant lastModifiedDate, ZonedDateTime lastNotificationRead, Set<String> authorities, Set<String> groups,
            Set<GuidedTourSetting> guidedTourSettings) {

        this.id = id;
        this.login = login;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.activated = activated;
        this.imageUrl = imageUrl;
        this.langKey = langKey;
        this.setCreatedBy(createdBy);
        this.setCreatedDate(createdDate);
        this.setLastModifiedBy(lastModifiedBy);
        this.setLastModifiedDate(lastModifiedDate);
        this.lastNotificationRead = lastNotificationRead;
        this.authorities = authorities;
        this.groups = groups;
        this.guidedTourSettings = guidedTourSettings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public ZonedDateTime getLastNotificationRead() {
        return lastNotificationRead;
    }

    public void setLastNotificationRead(ZonedDateTime lastNotificationRead) {
        this.lastNotificationRead = lastNotificationRead;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public Set<GuidedTourSetting> getGuidedTourSettings() {
        return guidedTourSettings;
    }

    public void setGuidedTourSettings(Set<GuidedTourSetting> guidedTourSettings) {
        this.guidedTourSettings = guidedTourSettings;
    }

    @Override
    public String toString() {
        return "UserDTO{" + "login='" + login + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", email='" + email + '\'' + ", imageUrl='"
                + imageUrl + '\'' + ", activated=" + activated + ", langKey='" + langKey + '\'' + ", createdBy=" + getCreatedBy() + ", createdDate=" + getCreatedDate()
                + ", lastModifiedBy='" + getLastModifiedBy() + '\'' + ", lastModifiedDate=" + getLastModifiedDate() + ", lastNotificationRead=" + lastNotificationRead
                + ", authorities=" + authorities + ",guidedTourSettings=" + guidedTourSettings + "}";
    }
}
