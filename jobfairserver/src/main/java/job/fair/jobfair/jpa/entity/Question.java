package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by annawang on 1/27/16.
 */
@Entity
@Table(name = "QUESTION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    @Column(name = "QUESTIONTEXT")
    private String questionText;
    @Column(name = "ANSWER")
    private short answer;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "JOB")
    private Job job;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "QUESTIONOPTION")
    private Collection<String> questionOptions;

    public Question() {
    }

    // we don't want to expose the id value, so put @JsonProperty for uuid instead
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

    public short getAnswer() {
        return answer;
    }

    public void setAnswer(short answer) {
        this.answer = answer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Collection<String> getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(Collection<String> questionOptions) {
        this.questionOptions = questionOptions;
    }

    public void addQuestionOption(String option) {
        if (questionOptions == null) {
            questionOptions = new ArrayList<>();
        }
        this.questionOptions.add(option);
    }
}
