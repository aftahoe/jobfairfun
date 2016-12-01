package job.fair;

import job.fair.jobfair.jpa.controller.CompanyController;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.testutil.connector.HtmlConnector51job;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
import java.util.Set;

/**
 * Created by annawang on 2/11/16.
 */
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("jpa")
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore
public class Preload51job {

    @Autowired
    private CompanyController compController;

    @Autowired
    private CompanyService companyService;

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
    public void load_51Job() throws Exception {
        HtmlConnector51job connector = new HtmlConnector51job();
        File dir = new ClassPathResource("htmls", getClass()).getFile();

        for (File f : dir.listFiles()) {
            Set<Company> result = connector.parseHtmlFile(f.getAbsolutePath());
            for (Company c : result) {
                companyService.addCompany(c);
            }
        }
    }

}
