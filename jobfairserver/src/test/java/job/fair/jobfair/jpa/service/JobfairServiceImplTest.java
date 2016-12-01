package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.controller.JobfairController;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import job.fair.jobfair.testutil.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.sql.Timestamp;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by annawang on 2/5/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@WebAppConfiguration
@ActiveProfiles("jpa")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JobfairServiceImplTest {
    @Autowired
    protected JobfairService jobfairService;
    @Autowired
    protected CompanyService companyService;
    @Autowired
    private JobfairController jobfairController;
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private Jobfair jobfair;

    @Before
    public void setup() throws Exception {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();

        jobfair = new Jobfair();
        jobfair.setName("the Preload51job jobfair");
        Timestamp startTimestamp = Timestamp.valueOf("2016-01-23 10:10:10.0");
        jobfair.setStartDate(startTimestamp);
        Timestamp endTimestamp = Timestamp.valueOf("2016-01-24 10:10:10.0");
        jobfair.setEndDate(endTimestamp);
        jobfair.setDescription("linkedin is holding a jobfair");
        jobfair.setLocation("linkedin.com");
    }

    @Test
    public void testAddJobfair() throws Exception {
        long id = jobfairService.addJobfair(jobfair);

        Jobfair jobfair = jobfairService.getJobfair(id);
        assertThat(jobfair.getName()).isEqualTo("the Preload51job jobfair");
        assertEquals("linkedin.com", jobfair.getLocation());
    }

    @Test
    public void testAddJobfairCompany() throws Exception {
        Company c = addTestingComapny("Company1", "CompanyLink1");

        jobfairService.addCompanyToJobFair(1, c);

        Collection<Company> companies = companyService.getCompanyByName("Company1");
        Assert.assertEquals(1, companies.iterator().next().getJobfair().getId());
    }
//
//    @Test
//    public void testAddJobfairExsitingCompany() throws Exception {
//        Company company = TestUtil.createTutorGroupCompany();
//        company.setId(1);
//        jobfairService.addCompanyToJobFair(1, company);
//
//        Company c = companyService.getCompany(1);
//        Assert.assertEquals(1, c.getJobfair().getId());
//    }

    @Test
    public void testGetJobfairCompanies() throws Exception {
        Company c = addTestingComapny("Comapny2", "CompanyLink2");
        c.setEvaluationQuestions(null);
        c.setJobs(null);
        jobfairService.addCompanyToJobFair(1, c);

        Page<Company> companies = jobfairService.getJobfairCompanies(1, null);
        assertEquals(2, companies.getTotalElements());
        assertEquals(6, companies.iterator().next().getCount());
    }


    @Test
    public void testGetJobfairCompaniesWithPagination() throws Exception {
        Company c = addTestingComapny("Comapny3", "CompanyLink3");
        c.setEvaluationQuestions(null);
        c.setJobs(null);
        jobfairService.addCompanyToJobFair(1, c);

        Pageable pageable = new PageRequest(0, 2);
        Page<Company> companies = jobfairService.getJobfairCompanies(1, pageable);
        assertEquals(2, companies.getTotalElements());
        assertEquals(6, companies.iterator().next().getCount());

        pageable = new PageRequest(1, 2);
        companies = jobfairService.getJobfairCompanies(1, pageable);
        assertEquals(1, companies.getTotalElements());
        assertEquals(0, companies.iterator().next().getCount());

        pageable = new PageRequest(2, 2);
        companies = jobfairService.getJobfairCompanies(1, pageable);
        assertEquals(0, companies.getTotalElements());

    }

    private Company addTestingComapny(String companyName, String companyLink) {
        Company c = TestUtil.createTestCompany();
        c.setName(companyName);
        c.setLink(companyLink);

        return c;
    }

}