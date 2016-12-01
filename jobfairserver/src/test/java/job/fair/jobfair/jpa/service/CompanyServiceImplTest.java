package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.controller.CompanyController;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.InterviewerRepository;
import job.fair.jobfair.testutil.TestUtil;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by annawang on 1/26/16.
 */
//@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@WebAppConfiguration
@ActiveProfiles("jpa")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CompanyServiceImplTest {

    private static Candidate c;
    private static Interviewer in;
    private static String companyuuid;
    @Autowired
    protected CandidateRepository candidateRepository;
    @Autowired
    protected InterviewerRepository interviewerRepository;
    @Autowired
    protected CompanyRepository companyRepository;
    @Autowired
    protected CompanyService companyService;
    @Autowired
    protected CandidateService candidateService;
    @Autowired
    private CompanyController compController;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private Company company;
    private Job job;

    @Before
    public void setup() throws Exception {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();

        company = TestUtil.createTestCompany();
        company.setName("testcompany");
        company.setLink("testlink");
        String[] uuids = {TestUtil.EVALUATION_UUID_11, TestUtil.EVALUATION_UUID_22, TestUtil.EVALUATION_UUID_33, TestUtil.EVALUATION_UUID_44};
        TestUtil.setEvaludationQuestions(company, uuids);
    }

    @Test
    public void getNonCompany() throws Exception {
        Collection<Company> companies = this.companyService.getCompanyByName("non");

        assertThat(companies.size()).isEqualTo(0);
    }

    @Test
    public void createCompany() throws Exception {
        this.companyService.addCompany(company);

        Collection<Company> companies = companyService.getCompanyByName("testcompany");
        Company company = companies.iterator().next();
        companyuuid = company.getUuid();
        assertThat(companies.size()).isEqualTo(1);
        // make sure the foreign keys work
        assertEquals(6, company.getJobs().size());
        // TODO the foreign key
        Iterator<Job> cIterator = company.getJobs().iterator();
        while (cIterator.hasNext()) {
            Job job = cIterator.next();

        }
    }

    @Test
    public void createCompanyWithIcon() throws Exception {
        byte[] content = "this is the Preload51job icon content".getBytes();
        MockMultipartFile mockIcon = new MockMultipartFile("icon", "icon.png", "jpg/png", content);
        Company company = new Company();
        company.setName("companywithicon");
        company.setLink("companywithiconlink");

        long companyid = this.companyService.addCompany(company, mockIcon, "http://host:8080/jobfair/company/icon/");

        Collection<Company> companies = companyService.getCompanyByName("companywithicon");
        assertThat(companies.size()).isEqualTo(1);
        // make sure the foreign keys work
        assertTrue(new File("/tmp/icons/" + companyid + ".png").exists());
    }


    @Test
    public void getComapnyByName() throws Exception {
        Collection<Company> companies = this.companyService.getCompanyByName("testcompany");

        assertThat(companies.size()).isEqualTo(1);
    }

    @Test
    public void getCompanyJobs() throws Exception {
        Pageable pageable = new PageRequest(0, 4);
        Page<Job> jobs = this.companyService.getCompanyJobs(companyuuid, pageable);
        assertThat(jobs.getTotalElements()).isEqualTo(4);

        pageable = new PageRequest(1, 4);
        jobs = this.companyService.getCompanyJobs(companyuuid, pageable);
        assertThat(jobs.getTotalElements()).isEqualTo(2);

        jobs = this.companyService.getCompanyJobs(companyuuid, null);
        assertThat(jobs.getTotalElements()).isEqualTo(6);
    }

    @Test
    public void getEvaluationQuestions() throws Exception {
        ArrayList<EvaluationQuestion> questions = this.companyService.getEvaluationQuestions(1);

        System.out.println(questions.get(0).getQuestionText());
        System.out.println(questions.get(1).getQuestionText());
        System.out.println(questions.get(2).getQuestionText());
        System.out.println(questions.get(3).getQuestionText());
    }

    @Test
    public void putEvaluationResult() throws Exception {
        c = candidateRepository.addCandidate(TestUtil.createCandidate());
        in = interviewerRepository.addInterviewer(TestUtil.createInterviewer());
        Job j = companyService.getJob("job1uuid");
        Application application = new Application();
        application.setCandidate(c);
        application.setJob(j);
        this.candidateRepository.addApplication(application);

        EvaluationReport report = new EvaluationReport();
        report.setApplication(application);
        report.setInterviewer(in);
        report.setNote("test note");

        Map<String, Integer> results = new LinkedHashMap<>();
        results.put(TestUtil.EVALUATION_UUID_1, 1);
        results.put(TestUtil.EVALUATION_UUID_2, 5);
        results.put(TestUtil.EVALUATION_UUID_3, 8);
        results.put(TestUtil.EVALUATION_UUID_4, 10);
        report.setScores(results);
        report.setRecommendationType(EvaluationReport.Type.RECOMMENDED);

        this.companyService.putEvaluationResult(report);

        Pageable pageable = new PageRequest(0, 4);
        Page<EvaluationReport> returnReport = this.companyRepository.getEvaluationReport(in.getId(), pageable);
        assertEquals(1, returnReport.getTotalElements());
        assertEquals("job1uuid", returnReport.iterator().next().getApplication().getJob().getUuid());
    }

    @Test
    public void searchJobs() throws Exception {
        Collection<Job> jobs = this.companyService.searchJobs("testcompany", null, 0, null);
        assertEquals(6, jobs.size());

        jobs = this.companyService.searchJobs("Santa Clara, CA", null, 0, null);
        assertEquals(2, jobs.size());

        jobs = this.companyService.searchJobs("Project manager", null, 0, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("java", null, 0, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("cvs", null, 0, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("aaaaaaaaaaa", null, 0, null);
        assertEquals(0, jobs.size());

        jobs = this.companyService.searchJobs("Front-End Developer", "santa clara", 0, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("Front-End Developer", "CA", 0, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("Front-End Developer", "NV", 0, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("Front-End Developer", "santa clara", 80000, null);
        assertEquals(1, jobs.size());

        jobs = this.companyService.searchJobs("Front-End Developer", "CA", 100000000, null);
        assertEquals(0, jobs.size());

        jobs = this.companyService.searchJobs("testcompany", null, 0, "positionName");
        List<String> realJobs = jobs.stream().map(job -> job.getPositionName()).collect(Collectors.toList());
        String[] expectedArray = {"Front-End Developer", "Front-End Developer",
                "Mobile Developer", "Product Designer", "Project Manager", "QA Engineer"};
        assertArrayEquals(expectedArray, realJobs.toArray());

        jobs = this.companyService.searchJobs("testcompany", null, 0, "-positionName");
        realJobs = jobs.stream().map(job -> job.getPositionName()).collect(Collectors.toList());
        expectedArray = new String[]{"QA Engineer", "Project Manager", "Product Designer",
                "Mobile Developer", "Front-End Developer", "Front-End Developer"};
        assertArrayEquals(expectedArray, realJobs.toArray());

        jobs = this.companyService.searchJobs("testcompany", null, 0, "location");
        realJobs = jobs.stream().map(job -> job.getLocation()).collect(Collectors.toList());
        expectedArray = new String[]{"Baker, NV", "Baker, NV", "Reno, NV", "Santa Clara, CA",
                "Santa Clara, CA", "Sunnyvale, CA"};
        assertArrayEquals(expectedArray, realJobs.toArray());

        jobs = this.companyService.searchJobs("testcompany", null, 0, "-location");
        realJobs = jobs.stream().map(job -> job.getLocation()).collect(Collectors.toList());
        expectedArray = new String[]{"Sunnyvale, CA", "Santa Clara, CA", "Santa Clara, CA",
                "Reno, NV", "Baker, NV", "Baker, NV"};
        assertArrayEquals(expectedArray, realJobs.toArray());
    }

    @Test
    public void searchRecommendedJobs() {
        Collection<Job> jobs = this.companyService.searchRecommendedJobs(c.getId());

        assertEquals(1, jobs.size());
    }

}
