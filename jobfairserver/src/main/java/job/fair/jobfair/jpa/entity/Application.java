package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by annawang on 3/7/16.
 */
@Entity
@Table(name = "APPLICATION")
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;
    // These are for front-end passin
    @Transient
    String jobId;
    @Transient
    String candidateId;
    @Transient
    int queuePosition;
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "RESUME")
    private String resume;
    @Column(name = "INTRODUCTION")
    private String introduction;
    @Column(name = "WORKETHIC", length = 512)
    private String workethic;
    @Column(name = "CELLPHONE")
    private String cellphone;
    @Column(name = "WECHATID")
    private String wechatid;
    @Column(name = "EXPERIENCE")
    private int experience;
    @Column(name = "SALARY")
    private int salary;
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "SKILLSS")
    private Set<String> skills;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Column(name = "SCORE")
    private float score = -1;
    @ManyToOne
    @PrimaryKeyJoinColumn(name = "CANDIDATE", referencedColumnName = "id")
    private Candidate candidate;
    @ManyToOne
    @PrimaryKeyJoinColumn(name = "JOB", referencedColumnName = "id")
    private Job job;
    @Column(name = "timestamp", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public Application() {
    }

    // we don't want to expose the id value, so use the uuid instead
    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    //@JsonProperty("id")
    @JsonIgnore
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getWechatid() {
        return wechatid;
    }

    public void setWechatid(String wechatid) {
        this.wechatid = wechatid;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getWorkethic() {
        return workethic;
    }

    public void setWorkethic(String workethic) {
        this.workethic = workethic;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    public void addSkill(String skill) {
        if (this.getSkills() == null) {
            this.skills = new HashSet<>();
        }
        this.skills.add(skill);
    }

    @JsonIgnore
    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @JsonIgnore
    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @JsonIgnore
    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    @PrePersist
    public void onCreate() {
        timestamp = new Date();
    }

    @JsonIgnore
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Condidate properties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("name")
    public String getCandidateName() {
        if (this.getCandidate() == null) return null;
        return this.getCandidate().getName();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("candidateId")
    public String getCandidateId() {
        if (this.getCandidate() == null) return null;
        return this.getCandidate().getUuid();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("email")
    public String getCandidateEmail() {
        if (this.getCandidate() == null) return null;
        return this.getCandidate().getEmail();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("status")
    public Candidate.CandidateStatus getCandidateStatus() {
        if (this.getCandidate() == null) return null;
        return this.getCandidate().getStatus();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("avatar")
    public String getCandidateAvatar() {
        if (this.getCandidate() == null) return null;
        return this.getCandidate().getAvatar();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("iconPathPrefix")
    public String getCandidateIconpathprefix() {
        if (this.getCandidate() == null) return null;
        return this.getCandidate().getIconPathPrefix();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("jobId")
    public String getJobId() {
        if (this.getJob() == null) return null;
        return this.getJob().getUuid();
    }


    public String getInternalJobUUID() {
        return this.jobId;
    }

    public void setInternalJobUUID(String jobId) {
        this.jobId = jobId;
    }

    public String getInternalCandidateUUID() {
        return this.candidateId;
    }

    public void setInternalCandidateUUID(String candidateId) {
        this.candidateId = candidateId;
    }
}
