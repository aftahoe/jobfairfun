package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by annawang on 2/25/16.
 */
@Entity
@Table(name = "EVALUATIONREPORT")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvaluationReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @MapKeyColumn(name = "EVALUATIONQUESTION_UUID")
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "EVAL_SCORE")
    private Map<String, Integer> scores;

    @Column(name = "NOTE", length = 512)
    private String note;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "RECOMMENDATION_TYPE")
    private Type recommendationType;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "APPLICATION", referencedColumnName = "id")
    private Application application;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "INTERVIEWER", referencedColumnName = "id")
    private Interviewer interviewer;

    //@Column(name = "timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Column(name = "timestamp", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // Those 2 are for front-end to pass in the uuids
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Transient
    private String candidateId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Transient
    private String jobId;

    public EvaluationReport() {
    }

    public EvaluationReport(EvaluationReport report) {
        this.setNote(report.getNote());
        this.setRecommendationType(report.getRecommendationType());
        this.setScores(report.getScores());
        this.setApplication(report.getApplication());
        this.setInterviewer(report.getInterviewer());
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @JsonIgnore
    public Interviewer getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(Interviewer interviewer) {
        this.interviewer = interviewer;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Type getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(Type recommendationType) {
        this.recommendationType = recommendationType;
    }

    @PrePersist
    public void onCreate() {
        timestamp = new Date();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public static enum Type {
        RECOMMENDED,
        NOTRECOMMENDED;

        @JsonValue
        public int getOrdinal() {
            return this.ordinal();
        }
    }
}
