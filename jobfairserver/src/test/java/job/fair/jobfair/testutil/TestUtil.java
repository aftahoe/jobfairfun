package job.fair.jobfair.testutil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Question;
import job.fair.jobfair.jpa.entity.User;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Created by annawang on 1/22/16.
 */
public class TestUtil {
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("gb2312"));

    public static final String JOB1_UUID = "job1uuid";

    //With ID = 1
    public static final String TUTORMEET_TEST_EMAIL_VALUE = "alex@tutormeet.com";
    public static final String TUTORMEET_TEST_PASSWORD = "tutormeet1";
    public static final String TUTORMEET_TEST_NAME_VALUE = "Alex Cone";
    public static final String TUTORMEET_COMPANY_NAME = "TutorGroup";
    // With ID = 2
    public static final String TEST_EMAIL = "Preload51job@gmail.com";
    public static final String TEST_INTRODUCTION_VALUE = "my introduction";
    public static final String TEST_ETHIC_VALUE = "my work ethic";
    public static final String TEST_PHONE_VALUE = "my cell phone";
    public static final int TEST_EXPERIENCE_VALUE = 3;
    public static final int TEST_SALARY_VALUE = 100000;
    public static final String TEST_WE_CHAT_ID_VALUE = "my we chat id";
    public static final String TEST_RESUME_FILE = "resume.txt";
    public static final String TEST_RESUME_DIR = "/tmp/1/";
    public static final String EVALUATION_UUID_1 = "evalUUID1";
    public static final String EVALUATION_UUID_2 = "evalUUID2";
    public static final String EVALUATION_UUID_3 = "evalUUID3";
    public static final String EVALUATION_UUID_4 = "evalUUID4";
    public static final String EVALUATION_UUID_11 = "evalUUID11";
    public static final String EVALUATION_UUID_22 = "evalUUID22";
    public static final String EVALUATION_UUID_33 = "evalUUID33";
    public static final String EVALUATION_UUID_44 = "evalUUID44";
    public static final String EVALUATION_TEXT_1 = "How do you feel that this candidate’s skills fit the job?";
    public static final String EVALUATION_TEXT_2 = "Do you feel that this candidate has the soft skills necessary to function in this job?";
    public static final String EVALUATION_TEXT_3 = "Do you feel that this candidate is good culture?";
    public static final String EVALUATION_TEXT_4 = "You are confident that this candidate will bring value to the company that we do not currently possess.";
    public static final int JOB1_POSITION_COUNT = 3;
    public static final String JOB1_POSITION_NAME = "Front-End Developer";
    public static final String JOB2_UUID = "job2uuid";
    public static final int JOB2_POSITION_COUNT = 4;
    public static final String JOB2_POSITION_NAME = "Product Designer";
    public static final String JOB3_UUID = "job3uuid";
    public static final int JOB3_POSITION_COUNT = 5;
    public static final String JOB3_POSITION_NAME = "QA Engineer";
    public static final String JOB4_UUID = "job4uuid";
    public static final int JOB4_POSITION_COUNT = 6;
    public static final String JOB4_POSITION_NAME = "Mobile Developer";
    public static final String JOB5_UUID = "job5uuid";
    public static final int JOB5_POSITION_COUNT = 2;
    public static final String JOB5_POSITION_NAME = "Project Manager";
    public static final String JOB6_POSITION_NAME = "Front-End Developer";
    public static final String JOB6_UUID = "job6UUID";
    public static final int JOB6_POSITION_COUNT = 3;
    public static final int JOB1_COMPENSATION = 10000000;
    public static final int JOB2_COMPENSATION = 60000;
    public static final int JOB3_COMPENSATION = 80000;
    public static final int JOB4_COMPENSATION = 130000;
    public static final int JOB5_COMPENSATION = 190000;
    public static final int JOB6_COMPENSATION = 190000;
    public static final String TEST_PASSWORD = "password";

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    public static Candidate createTutorMeetSignUpCandidate() {
        Candidate candidate = new Candidate();
        candidate.setEmail(TUTORMEET_TEST_EMAIL_VALUE);
        candidate.setName(TUTORMEET_TEST_NAME_VALUE);
        candidate.setPassword(TUTORMEET_TEST_PASSWORD);
        candidate.setLocale("en");
        return candidate;
    }

    public static Company createTestCompany() {
        Company testCompany = new Company();

        testCompany.setName(TUTORMEET_COMPANY_NAME);
        testCompany.setLink("http://www.tutor-group.com/");

        Job job1 = new Job();
        job1.setUuid(JOB1_UUID);
        job1.setPositionCount(JOB1_POSITION_COUNT);
        job1.setPositionName(JOB1_POSITION_NAME);
        job1.setLocation("Santa Clara, CA");
        job1.setDescription("TutorGroup is seeking a creative Front End Developer to drive user experience innovations for apple.com. This developer will not only be responsible for defining the architectural strategy for front end technologies - including HTML5, CSS3 and JavaScript - but for evangelizing that technology across the team and TutorGroup as a whole.\n" +
                "- Comfortable with source version control software (CVS, SVN, and Git)\n" +
                "- Well -versed in fundamental visual and interactive design discipline\n" +
                "- Strive to use web standards to build solutions using semantic markup and CSS");
        job1.setCompensation(JOB1_COMPENSATION);

        Question question1 = new Question();
        question1.setUuid("question1uuid");
        question1.setQuestionText("What is the main concept behind email segmentation?");
        question1.setAnswer((short) 1);
        question1.addQuestionOption("To make sure we are connecting at the right time with the right people");
        question1.addQuestionOption("To bug people as much as possible");
        question1.addQuestionOption("It is in the bible");
        question1.addQuestionOption("To let people opt out of recieving emails");
        job1.addQuestion(question1);

        Question question2 = new Question();
        question2.setUuid("question2uuid");
        question2.setQuestionText("this is the second question?");
        question2.setAnswer((short) 3);
        question2.addQuestionOption("option1");
        question2.addQuestionOption("option2");
        job1.addQuestion(question2);


        Collection<String> requiredSkills = new ArrayList<>();
        requiredSkills.add("HTML");
        requiredSkills.add("JAVA");
        requiredSkills.add("JavaScript");
        job1.setRequiredSkills(requiredSkills);
        testCompany.addJob(job1);

        Job job2 = new Job();
        job2.setUuid(JOB2_UUID);
        job2.setPositionCount(JOB2_POSITION_COUNT);
        job2.setPositionName(JOB2_POSITION_NAME);
        job2.setLocation("Santa Clara, CA");
        job2.setCompensation(JOB2_COMPENSATION);
        //job2.addQuestion(question1);
        //job2.addQuestion(question2);
        testCompany.addJob(job2);

        Job job3 = new Job();
        job3.setUuid(JOB3_UUID);
        job3.setPositionCount(JOB3_POSITION_COUNT);
        job3.setPositionName(JOB3_POSITION_NAME);
        job3.setLocation("Sunnyvale, CA");
        job3.setCompensation(JOB3_COMPENSATION);
        //job3.addQuestion(question1);
        //job3.addQuestion(question2);
        testCompany.addJob(job3);

        Job job4 = new Job();
        job4.setUuid(JOB4_UUID);
        job4.setPositionCount(JOB4_POSITION_COUNT);
        job4.setPositionName(JOB4_POSITION_NAME);
        job4.setLocation("Reno, NV");
        job4.setCompensation(JOB4_COMPENSATION);
        //job4.addQuestion(question1);
        //job4.addQuestion(question2);
        testCompany.addJob(job4);

        Job job5 = new Job();
        job5.setUuid(JOB5_UUID);
        job5.setPositionCount(JOB5_POSITION_COUNT);
        job5.setPositionName(JOB5_POSITION_NAME);
        job5.setLocation("Baker, NV");
        job5.setCompensation(JOB5_COMPENSATION);
        //job5.addQuestion(question1);
        //job5.addQuestion(question2);
        testCompany.addJob(job5);

        Job job6 = new Job();
        job6.setUuid(JOB6_UUID);
        job6.setPositionCount(JOB6_POSITION_COUNT);
        job6.setPositionName(JOB6_POSITION_NAME);
        job6.setLocation("Baker, NV");
        job6.setCompensation(JOB6_COMPENSATION);
        //job5.addQuestion(question1);
        //job5.addQuestion(question2);
        testCompany.addJob(job6);

        setEvaludationQuestions(testCompany, null);

        return testCompany;
    }

    public static Application createApplication() {
        Application application = new Application();
        application.setIntroduction(TEST_INTRODUCTION_VALUE);
        application.setWorkethic(TEST_ETHIC_VALUE);
        application.setCellphone(TEST_PHONE_VALUE);
        application.setWechatid(TEST_WE_CHAT_ID_VALUE);
        application.addSkill("JAVA");
        application.addSkill("HTML");
        application.setScore((float) 0.5);
        application.setExperience(TEST_EXPERIENCE_VALUE);
        application.setSalary(TEST_SALARY_VALUE);
        return application;
    }

    public static Candidate createCandidate() {
        Candidate c = new Candidate();
        c.setEmail(TEST_EMAIL);
        c.setPassword(TEST_PASSWORD);
        c.setTutorMeetToken("testtoken");
        c.setTutorMeetApitoken("testapitoken");
        c.setName("testname");
        return c;
    }

    public static Interviewer createInterviewer() {
        Interviewer c = new Interviewer();
        c.setEmail("interviewer@gmail.com");
        c.setPassword("password");
        c.setTutorMeetToken("testtoken");
        c.setTutorMeetApitoken("testapitoken");
        c.setName("interviewer tutor");
        return c;
    }

    public static User createTutorMeetUser() {
        User user = new User(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, TestUtil.TUTORMEET_TEST_PASSWORD);
        user.setUseruuid("testuseruuid");
        return user;
    }

    public static Candidate createTutorMeetCandidate() {
        Candidate c = new Candidate();
        c.setEmail(TUTORMEET_TEST_EMAIL_VALUE);
        c.setPassword(TUTORMEET_TEST_PASSWORD);
        c.setUuid("testuseruuid");
        return c;
    }

//    public static String getAppliedCandidateJSONString() throws JsonProcessingException {
//        Application c = createAppliedCandidate();
//        c.setIntroduction(TEST_INTRODUCTION_VALUE);
//        c.setWechatid(TEST_WE_CHAT_ID_VALUE);
//        c.setCellphone(TEST_PHONE_VALUE);
//        c.setWorkethic(TEST_ETHIC_VALUE);
//
//        c.setId(1L);
//        c.addSkill("JAVA");
//        c.addSkill("HTML");
//
//
//        ObjectMapper mapper = new ObjectMapper();
//        return mapper.writeValueAsString(c);
//    }

    public static MockMultipartFile testResumeMultipartFile() {
        byte[] content = "this is the test resume content".getBytes();
        return new MockMultipartFile("resume", "resume.txt", "text/plain", content);
    }

    public static void setEvaludationQuestions(Company company, String[] uuids) {

        company.addEvaluationQuestion(
                new EvaluationQuestion(uuids == null ? EVALUATION_UUID_1 : uuids[0], EVALUATION_TEXT_1));
        company.addEvaluationQuestion(
                new EvaluationQuestion(uuids == null ? EVALUATION_UUID_2 : uuids[1], EVALUATION_TEXT_2));
        company.addEvaluationQuestion(
                new EvaluationQuestion(uuids == null ? EVALUATION_UUID_3 : uuids[2], EVALUATION_TEXT_3));
        company.addEvaluationQuestion(
                new EvaluationQuestion(uuids == null ? EVALUATION_UUID_4 : uuids[3], EVALUATION_TEXT_4));

    }

    public static Interviewer createLoginInterviewer() {
        Interviewer in = new Interviewer();
        in.setEmail(TUTORMEET_TEST_EMAIL_VALUE);
        in.setPassword(TUTORMEET_TEST_PASSWORD);
        return in;
    }

    public static Timestamp getYesterdayStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        calendar.set(Calendar.MINUTE, 0); // set minutes to zero
        calendar.set(Calendar.SECOND, 0); //set seconds to zero
        calendar.set(Calendar.MILLISECOND, 000);
        return new Timestamp(calendar.getTime().getTime());
    }

    public static Timestamp getYesterdayEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime().getTime());
    }

    public static Timestamp getTodayStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        calendar.set(Calendar.MINUTE, 0); // set minutes to zero
        calendar.set(Calendar.SECOND, 0); //set seconds to zero
        calendar.set(Calendar.MILLISECOND, 000);
        return new Timestamp(calendar.getTime().getTime());
    }

    public static Timestamp getTodayEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime().getTime());
    }
}
