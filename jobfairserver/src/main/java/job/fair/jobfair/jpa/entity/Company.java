package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import job.fair.commons.IdGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "COMPANY",
        indexes = {@Index(name = "link_index", columnList = "link", unique = true)})
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;

    @Column(name = "NAME")
    private String name;

    @Column(name = "LINK")
    private String link;

    @Column(name = "EMAILDOMAIN")
    private String emailDomain;

    @Column(name = "ICON")
    private String icon;


    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "JOBFAIR")
    private Jobfair jobfair;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Job> jobs;


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<EvaluationQuestion> evaluationQuestions;

    @Transient
    private long count;

    public Company() {
    }//must have

    public Company(int id, String name, String link) {
        this.name = name;
        this.link = link;
        this.id = id;
        this.jobs = new LinkedList<>();
    }

    @JsonIgnore // we don't want to expose the id value
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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Jobfair getJobfair() {
        return jobfair;
    }

    public void setJobfair(Jobfair jobfair) {
        this.jobfair = jobfair;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public void addJob(Job job) {
        if (this.jobs == null) {
            this.jobs = new LinkedList<>();
        }
        if (job.getUuid() == null || job.getUuid().isEmpty())
            job.setUuid(IdGenerator.createId());
        job.setCompany(this);
        this.jobs.add(job);
    }

    @JsonIgnore
    public List<EvaluationQuestion> getEvaluationQuestions() {
        return evaluationQuestions;
    }

    public void setEvaluationQuestions(List<EvaluationQuestion> evaluationQuestions) {
        this.evaluationQuestions = evaluationQuestions;
    }

    public void addEvaluationQuestion(EvaluationQuestion question) {
        if (this.evaluationQuestions == null) {
            this.evaluationQuestions = new ArrayList<EvaluationQuestion>();
        }
        if (question.getUuid() == null || question.getUuid().isEmpty())
            question.setUuid(IdGenerator.createId());
        question.setCompany(this);
        this.evaluationQuestions.add(question);
    }
}
