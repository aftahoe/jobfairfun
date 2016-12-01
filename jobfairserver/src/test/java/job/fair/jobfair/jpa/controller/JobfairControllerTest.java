package job.fair.jobfair.jpa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import job.fair.jobfair.testutil.TestUtil;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.sql.Timestamp;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by annawang on 2/5/16.
 */
@Ignore // TODO not maintain right now
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@WebAppConfiguration
@ActiveProfiles("jpa")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JobfairControllerTest {

    @Autowired
    private JobfairController jobfairController;

    @Autowired
    private CompanyController compController;

    @Autowired
    private CandidateController candidateController;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();
    }

    @Test
    public void createNewJobfairWithCompany() throws Exception {
        Jobfair jobfair = new Jobfair();
        jobfair.setName("new job fair from controller with company");
        jobfair.setUuid("testjobfairuuid");
        Timestamp startTimestamp = Timestamp.valueOf("2016-02-23 10:10:10.0");
        jobfair.setStartDate(startTimestamp);
        Timestamp endTimestamp = Timestamp.valueOf("2016-02-24 10:10:10.0");
        jobfair.setEndDate(endTimestamp);
        jobfair.setDescription("Test Family");
        jobfair.setLocation("test.com");
        Company company = TestUtil.createTestCompany();
        company.setName("test company jobfair");
        company.setLink("testcompany fair link");
        company.setUuid("testcompanyuuid");
        jobfair.addCompany(company);

        ResultActions actions = mockMvc.perform(post("/jobfairs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(jobfair)))
                .andExpect(status().isCreated());

    }

    @Ignore
    @Test
    public void createNewJobfairWithoutCompany() throws Exception {
        Jobfair jobfair = new Jobfair();
        jobfair.setName("new job fair from controller");
        Timestamp startTimestamp = Timestamp.valueOf("2016-03-23 10:10:10.0");
        jobfair.setStartDate(startTimestamp);
        Timestamp endTimestamp = Timestamp.valueOf("2016-03-24 10:10:10.0");
        jobfair.setEndDate(endTimestamp);
        jobfair.setDescription("a wonderful jobfair");
        jobfair.setLocation("51jobs.com");

        ResultActions actions = mockMvc.perform(post("/jobfairs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(jobfair)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message.").value("New Jobfair created with id 2"));

    }

    @Ignore
    @Test
    public void testAddCompanyToJobfair() throws Exception {
        Company company = TestUtil.createTestCompany();
        company.setName("new Tutor");
        company.setLink("new tutor link");
        ResultActions actions = mockMvc.perform(post("/2/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(company)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message.").value("Company added to jobfair 2"));

    }

    @Ignore
    @Test
    public void testAddExistingCompanyToJobfair() throws Exception {
        Company company = TestUtil.createTestCompany();
        company.setId(6);
        ResultActions actions = mockMvc.perform(post("/2/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(company)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message.").value("Company added to jobfair 2"));
    }

    @Ignore
    @Test
    public void testAddExistingCompanyToJobfairAnother() throws Exception {
        Company company = TestUtil.createTestCompany();
        company.setId(6);
        mockMvc.perform(post("/1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(company)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message.").value("Company 6 already belong to jobfair 2"));
    }

    @Ignore
    @Test
    public void testAddCompanyToNonJobfair() throws Exception {
        mockMvc.perform(post("/5/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(TestUtil.createTestCompany())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message.").value("Unknown match: Jobfair 5 does not exist"));

    }

    @Test
    public void testGetCompanyFromJobfair() throws Exception {
        mockMvc.perform(get("/testjobfairuuid/companies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

    }

    @Test
    public void testGetAllCompanies() throws Exception {
        mockMvc.perform(get("/companies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}