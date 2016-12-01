package job.fair.jobfair.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by annawang on 2/15/16.
 */
@Entity
@Table(name = "INTERVIEWER")
public class Interviewer {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "UUID", unique = true, nullable = false)
    private String uuid;
    // From TutorMeet Login
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

    @Column(name = "MEETINGID")
    private String meetingID;

    public Interviewer() {
    }

    public int getTutorMeetUserSN() {
        return tutorMeetUserSN;
    }

    public void setTutorMeetUserSN(int tutorMeetUserSN) {
        this.tutorMeetUserSN = tutorMeetUserSN;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }


    // Getters and Setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTutorMeetToken() {
        return tutorMeetToken;
    }

    public void setTutorMeetToken(String tutorMeetToken) {
        this.tutorMeetToken = tutorMeetToken;
    }

    public String getTutorMeetApitoken() {
        return tutorMeetApitoken;
    }

    public void setTutorMeetApitoken(String tutorMeetApitoken) {
        this.tutorMeetApitoken = tutorMeetApitoken;
    }

}
