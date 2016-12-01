package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by annawang on 1/27/16.
 */
@Entity
@Table(name = "EVALUATIONQUESTION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvaluationQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    @Column(name = "QUESTIONTEXT")
    private String questionText;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "COMPANY")
    private Company company;


    public EvaluationQuestion() {
    }

    public EvaluationQuestion(String uuid, String text) {
        this.uuid = uuid;
        this.questionText = text;
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


    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
