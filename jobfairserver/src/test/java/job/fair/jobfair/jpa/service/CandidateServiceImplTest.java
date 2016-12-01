package job.fair.jobfair.jpa.service;

import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.testutil.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by annawang on 1/26/16.
 */
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("jpa")
//@TransactionConfiguration(defaultRollback = true)
//@Transactional
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CandidateServiceImplTest {

    private static String candidateUUID;
    @Autowired
    protected CandidateService candidateService;
    @Autowired
    protected CompanyService companyService;
    @Autowired
    protected InterviewerService interviewerService;
    @Autowired
    protected CompanyRepository companyRepository;
    @Autowired
    protected CandidateRepository candidateRepository;
    @Autowired
    protected UserService userService;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private Candidate candidate;

    @Before
    public void setup() throws Exception {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();

        candidate = TestUtil.createCandidate();


    }

    @Test
    public void isCandidateExist() throws Exception {
        boolean isExistingCandidate = this.candidateService.isExistingCandidate(candidate);
        assertFalse(isExistingCandidate);
    }

    @Test
    public void testAddCandidate() throws Exception {
        this.candidateService.addCandidate(candidate);
        assertTrue(candidateService.isExistingCandidate(candidate));
    }

    @Test
    public void testGetCandidateByEmail() throws Exception {
        Candidate c = this.candidateService.getCandidateByEmail(TestUtil.TEST_EMAIL);
        candidateUUID = c.getUuid(); // for get evaluation result
        Assert.assertEquals(TestUtil.TEST_EMAIL, c.getEmail());
    }

    @Test
    public void testGetCandidateByEmail_candidateNotFound() throws Exception {
        Candidate candidate = this.candidateService.getCandidateByEmail("testnon@gmail.com");

        assertTrue(candidate == null);
    }

    @Test(expected = CandidateException.class)
    public void testSignupWithDuplicateUser() throws Exception {
        this.candidateService.signup(TestUtil.createTutorMeetSignUpCandidate());
    }

//    @Test
//    public void testUserProfile() throws Exception {
//        Candidate profile = this.candidateService.getLatestApplication(1);
//        assertEquals("testname", profile.getName());
//        assertEquals("my introduction", profile.getIntroduction());
//        assertEquals("my work ethic", profile.getWorkethic());
//        assertEquals("my we chat id", profile.getWechatid());
//        assertEquals("my cell phone", profile.getCellphone());
//        assertEquals(2, profile.getSkills().size());
//    }

//    @Test
//    public void testUserProfileUpdate() throws Exception {
//        Candidate newCandidateData = new Candidate();
//        newCandidateData.setName("updated name");
//        newCandidateData.setIntroduction("updated introduction");
//        newCandidateData.setWorkethic("updated work ethic");
//        newCandidateData.setWechatid("updated we chat id");
//        newCandidateData.setCellphone("updated cell phone");
//        newCandidateData.addSkill("C++");
//        newCandidateData.addSkill("JAVA");
//        newCandidateData.addSkill("IOS");
//        Candidate updatedCandidate = this.candidateService.updateCandidateProfile(TEST_EMAIL, newCandidateData);
//        assertEquals("updated name", updatedCandidate.getName());
//        assertEquals("updated introduction", updatedCandidate.getIntroduction());
//        assertEquals("updated work ethic", updatedCandidate.getWorkethic());
//        assertEquals("updated we chat id", updatedCandidate.getWechatid());
//        assertEquals("updated cell phone", updatedCandidate.getCellphone());
//        assertEquals(3, updatedCandidate.getSkills().size());
//    }

    @Test
    public void testUserApplyJob() throws Exception {
        createCompany();
        byte[] content = "this is the Preload51job resume content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("fileData", "resume.txt", "text/plain", content);
        List<MultipartFile> resumes = new ArrayList<>();
        resumes.add(mockMultipartFile);

        User user = new User(TestUtil.TEST_EMAIL, TestUtil.TEST_PASSWORD);
        user.setUserId(1L);
        Job job = this.companyRepository.getJob(1L);
        Application application = TestUtil.createApplication();
        application.setJob(job);
        this.candidateService.applyJob(jobForApply(), user, application, resumes);

        Candidate appliedCandidate = this.candidateService.getCandidateByEmail(TestUtil.TEST_EMAIL);
        assertTrue(new File(TestUtil.TEST_RESUME_DIR + TestUtil.TEST_RESUME_FILE).exists());
        assertEquals("jobqueue1", appliedCandidate.getJobqueue());
        List<Application> appliedApplication = this.candidateRepository.getApplications(1L, 1L);
        assertTrue(0.5 == appliedApplication.get(0).getScore());

        //assertTrue((float) 0.5 == appliedCandidate.getScores().get(1L));
        //TODO make sure the user is added to the queue
        //assertTrue(1 == SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue72").getJobsWaiting().size());
    }

    @Test
    public void testUserRemoveApplyJob() throws Exception {
        this.candidateService.removeUserFromQueue(jobForApply(), 1L, "testuuid", User.Type.CANDIDATE);

        Candidate candidate = this.candidateService.getCandidateByEmail(TestUtil.TEST_EMAIL);
        assertEquals("", candidate.getJobqueue());
        //assertTrue((float) 0.5 == candidate.getScores().get(1L));
        //TODO make sure the user is added to the queue
        //assertTrue(1 == SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue72").getJobsWaiting().size());
    }

    @Test
    public void testUserFavoriteJobGet() throws Exception {
        Collection<Job> jobs = this.candidateService.getFavoriteJobs(1L);
        assertEquals(0, jobs.size());
    }

    @Test
    public void testUserFavoriteJobPut() throws Exception {
        Job job1 = this.companyService.getJob("job5uuid");
        Job job2 = this.companyService.getJob("job1uuid");
        Collection<Job> jobs = this.candidateService.putFavoriteJob(1L, job1);
        assertEquals(1, jobs.size());
        jobs = this.candidateService.putFavoriteJob(1L, job2);
        assertEquals(2, jobs.size());
    }

    @Test
    public void testUserFavoriteJobPutGet() throws Exception {
        Collection<Job> jobs = this.candidateService.getFavoriteJobs(1L);
        assertEquals(2, jobs.size());
    }

    @Test
    public void testUserFavoriteJob_Remove() throws Exception {
        Collection<Job> jobs = this.candidateService.deleteFavoriteJob(1L, "job5uuid");
        assertEquals(1, jobs.size());
    }


    private HttpServletRequest mockedRequest() {
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        HttpSession mockedSession = mock(HttpSession.class);
        when(mockedRequest.getSession(true)).thenReturn(mockedSession);
        when(mockedSession.isNew()).thenReturn(true);
        when(mockedSession.getId()).thenReturn("testSessionId");
        return mockedRequest;
    }

//    private Candidate expectedAppliedCandidate() {
//        Candidate expectedCandidate = createAppliedCandidate();
//        expectedCandidate.setResume(TEST_RESUME_DIR + TEST_RESUME_FILE);
//        return expectedCandidate;
//    }


    private Job jobForApply() {
        return this.companyService.getJob(1);
    }

    private void createCompany() {
        Company company = TestUtil.createTestCompany();
        company.setName("testcompany");
        company.setLink("testlink");
        companyService.addCompany(company);
    }
}


