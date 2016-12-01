package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by annawang on 2/5/16.
 */
@Entity
@Table(name = "JOBFAIR")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Jobfair implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    @Column(name = "NAME")
    private String name;
    @Column(name = "STARTDATE")
    private Timestamp startDate;
    @Column(name = "ENDDATE")
    private Timestamp endDate;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "LOCATION")
    private String location;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobfair", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<Company> companies;

    @Transient
    private long companyCount;

    public Jobfair() {
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

    public long getCompanyCount() {
        return companyCount;
    }

    public void setCompanyCount(long companyCount) {
        this.companyCount = companyCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Set<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(Set<Company> companies) {
        this.companies = companies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addCompany(Company company) {
        if (this.companies == null) {
            companies = new HashSet<>();
        }
        if (company.getUuid() == null || company.getUuid().isEmpty())
            company.setUuid(IdGenerator.createId());
        company.setJobfair(this);
        companies.add(company);
    }
}
