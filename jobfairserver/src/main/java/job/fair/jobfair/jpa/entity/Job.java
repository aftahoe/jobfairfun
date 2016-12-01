package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import job.fair.commons.IdGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by annawang on 1/19/16.
 */
@Entity
@Table(name = "JOB")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Job implements Serializable {
    public static final String SORTING_ASC_COMPANY_NAME = "companyName";
    public static final String SORTING_DESC_COMPANY_NAME = "-companyName";
    public static final String SORTING_ASC_POSITION_NAME = "positionName";
    public static final String SORTING_DESC_POSITION_NAME = "-positionName";
    public static final String SORTING_ASC_LOCATION = "location";
    public static final String SORTING_DESC_LOCATION = "-location";
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    @Column(name = "POSITIONNUMBER")
    private int positionNumber;
    @Column(name = "POSITIONNAME")
    private String positionName;
    @Column(name = "POSITIONCOUNT")
    private int positionCount;
    @Column(name = "LOCATION")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String location;
    @Column(name = "COMPENSATION")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int compensation;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "ISSUEDATE")
    private String issueDate;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Column(name = "CONTRACTTYPE")
    private String contractType;
    @Lob
    @Column(name = "DESCRIPTION")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "COMPANY", nullable = false)
    private Company company;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "job", fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<Question> questionSet;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "REQUIREDSKILLS")
    private Collection<String> requiredSkills;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Transient
    private int candidateWaitingCount = -1;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Transient
    private int recommendationCount = -1;

    public Job() {
    }

    public Job(long id, String uuid, int positionNumber, String positionName, int positionCount) {
        this.id = id;
        this.uuid = uuid;
        this.positionNumber = positionNumber;
        this.positionName = positionName;
        this.positionCount = positionCount;
    }

    @JsonIgnore // we don't want to expose the id value, so set uuid with @JsonProperty instead
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("companyName")
    public String getCompanyName() {
        if (this.getCompany() == null) return null;
        return this.getCompany().getName();
    }

    public Collection<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Collection<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public int getCandidateWaitingCount() {
        return candidateWaitingCount;
    }

    public void setCandidateWaitingCount(int candidateWaitingCount) {
        this.candidateWaitingCount = candidateWaitingCount;
    }

    public int getRecommendationCount() {
        return recommendationCount;
    }

    public void setRecommendationCount(int recommendationCount) {
        this.recommendationCount = recommendationCount;
    }

    public int getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(int positionNumber) {
        this.positionNumber = positionNumber;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCompensation() {
        return compensation;
    }

    public void setCompensation(int compensation) {
        this.compensation = compensation;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Question> getQuestionSet() {
        return questionSet;
    }

    public void setQuestionSet(List<Question> questionSet) {
        this.questionSet = questionSet;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public void setPositionCount(int positionNum) {
        this.positionCount = positionNum;
    }

    public void addQuestion(Question question) {
        if (questionSet == null) {
            questionSet = new ArrayList<>();
        }
        if (question.getUuid() == null || question.getUuid().isEmpty())
            question.setUuid(IdGenerator.createId());
        question.setJob(this);
        this.questionSet.add(question);
    }

    public void addRequiredSkills(String skill) {
        if (requiredSkills == null) {
            requiredSkills = new HashSet<>();
        }
        requiredSkills.add(skill);
    }
}
