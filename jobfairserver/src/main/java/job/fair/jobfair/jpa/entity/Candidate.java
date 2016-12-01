package job.fair.jobfair.jpa.entity;

/**
 * Created by annawang on 1/26/16.
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "CANDIDATE")
public class Candidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    // From TutorMeet Login
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Column(name = "TUTORMEETUSERSN")
    private int tutorMeetUserSN;
    @Column(name = "EMAIL")
    private String email;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "NAME")
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "TUTORMEETTOKEN")
    private String tutorMeetToken;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "TUTORMEETAPITOKEN")
    private String tutorMeetApitoken;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "AVATAR")
    private String avatar;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "LOCALE")
    private String locale;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS")
    private CandidateStatus status;
    @Column(name = "JOBQUEUE")
    private String jobqueue;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "FAVORATEJOB")
    private Collection<Job> favoriteJobs;
//    @MapKeyColumn(name = "JOB_ID")
//    @ElementCollection(fetch = FetchType.EAGER)
//    @Column(name = "SCORES")
//    private Map<Long, Float> scores;

    @Transient
    private Status queueStatus;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Transient
    private String iconPathPrefix;


    public Candidate() {
    }

    public Candidate(long id, String uuid, CandidateStatus status) {
        this.setId(id);
        this.setUuid(uuid);
        this.setStatus(status);
    }
    //============= Setters and Getters

    // we don't want to expose the id value, so use the uuid instead
    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonProperty("id")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIconPathPrefix() {
        return iconPathPrefix;
    }

    public void setIconPathPrefix(String iconPathPrefix) {
        this.iconPathPrefix = iconPathPrefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getTutorMeetToken() {
        return tutorMeetToken;
    }

    public void setTutorMeetToken(String tutorMeetToken) {
        this.tutorMeetToken = tutorMeetToken;
    }

    @JsonIgnore
    public String getTutorMeetApitoken() {
        return tutorMeetApitoken;
    }

    public void setTutorMeetApitoken(String tutorMeetApitoken) {
        this.tutorMeetApitoken = tutorMeetApitoken;
    }

    @JsonIgnore
    public int getTutorMeetUserSN() {
        return tutorMeetUserSN;
    }

    public void setTutorMeetUserSN(int tutorMeetUserSN) {
        this.tutorMeetUserSN = tutorMeetUserSN;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getJobqueue() {
        return jobqueue;
    }

    @JsonIgnore
    public void setJobqueue(String jobqueue) {
        this.jobqueue = jobqueue;
    }

    @JsonIgnore
    public Status getQueueStatus() {
        return queueStatus;
    }

    @JsonIgnore
    public void setQueueStatus(Status queueStatus) {
        this.queueStatus = queueStatus;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public Collection<Job> getFavoriteJobs() {
        return favoriteJobs;
    }

    public void setFavoriteJobs(Collection<Job> favoriteJobs) {
        this.favoriteJobs = favoriteJobs;
    }

    public void addFavoriteJob(Job job) {
        if (this.favoriteJobs == null) {
            this.favoriteJobs = new ArrayList<>();
        }
        this.favoriteJobs.add(job);
    }

    public void removeFavoriteJob(String jobuuid) {
        if (this.favoriteJobs == null) {
            return;
        }
        this.favoriteJobs.removeIf(job -> jobuuid.equals(job.getUuid()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Candidate candidate = (Candidate) o;

        if (id != candidate.id) return false;
        if (tutorMeetUserSN != candidate.tutorMeetUserSN) return false;
        if (uuid != null ? !uuid.equals(candidate.uuid) : candidate.uuid != null) return false;
        if (email != null ? !email.equals(candidate.email) : candidate.email != null) return false;
        if (password != null ? !password.equals(candidate.password) : candidate.password != null) return false;
        if (name != null ? !name.equals(candidate.name) : candidate.name != null) return false;
        if (tutorMeetToken != null ? !tutorMeetToken.equals(candidate.tutorMeetToken) : candidate.tutorMeetToken != null)
            return false;
        if (tutorMeetApitoken != null ? !tutorMeetApitoken.equals(candidate.tutorMeetApitoken) : candidate.tutorMeetApitoken != null)
            return false;
        if (avatar != null ? !avatar.equals(candidate.avatar) : candidate.avatar != null) return false;
        if (locale != null ? !locale.equals(candidate.locale) : candidate.locale != null) return false;
        if (status != candidate.status) return false;
        if (jobqueue != null ? !jobqueue.equals(candidate.jobqueue) : candidate.jobqueue != null) return false;
        if (favoriteJobs != null ? !favoriteJobs.equals(candidate.favoriteJobs) : candidate.favoriteJobs != null)
            return false;
        if (queueStatus != candidate.queueStatus) return false;
        return iconPathPrefix != null ? iconPathPrefix.equals(candidate.iconPathPrefix) : candidate.iconPathPrefix == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + tutorMeetUserSN;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (tutorMeetToken != null ? tutorMeetToken.hashCode() : 0);
        result = 31 * result + (tutorMeetApitoken != null ? tutorMeetApitoken.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (jobqueue != null ? jobqueue.hashCode() : 0);
        result = 31 * result + (favoriteJobs != null ? favoriteJobs.hashCode() : 0);
        result = 31 * result + (queueStatus != null ? queueStatus.hashCode() : 0);
        result = 31 * result + (iconPathPrefix != null ? iconPathPrefix.hashCode() : 0);
        return result;
    }

    //TODO do we need this?
    public static enum Status {
        READY,
        PENDING,
        RUNNING,
        FINISHED,
        ERROR,
        ABORT;

        public boolean isReady() {
            return this == READY;
        }

        public boolean isRunning() {
            return this == RUNNING;
        }

        public boolean isPending() {
            return this == PENDING;
        }
    }

    public static enum CandidateStatus {
        AVAILABLE,
        AWAY,
        INTERVIEW;

        @JsonValue
        public int getOrdinal() {
            return this.ordinal();
        }
    }
}
