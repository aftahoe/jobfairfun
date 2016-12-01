package job.fair.jobfair.jpa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.testutil.TestUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by annawang on 1/26/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@WebAppConfiguration
@ActiveProfiles("jpa")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TransactionConfiguration(defaultRollback = true)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CompanyControllerTest {


    @Autowired
    private CompanyController compController;

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
    public void createNewCompany() throws Exception {
        //create company
        Company testCompany = TestUtil.createTestCompany();
        testCompany.setEmailDomain("tutormeet.com");
        JSONObject companyJSON = new JSONObject(new ObjectMapper().writeValueAsString(testCompany));
        companyJSON.put("uuid", "companyuuid");
        JSONArray jobsArray = companyJSON.getJSONArray("jobs");
        for (int i = 0; i < jobsArray.length(); i++) {
            JSONObject jobObject = (JSONObject) jobsArray.get(i);
            jobObject.put("uuid", jobObject.getString("id"));
            jobObject.remove("id");

            if (jobObject.get("uuid").equals("job1uuid")) {
                JSONArray questions = jobObject.getJSONArray("questionSet");
                for (int j = 0; j < questions.length(); j++) {
                    JSONObject questionObject = (JSONObject) questions.get(j);
                    questionObject.put("uuid", questionObject.getString("id"));
                    questionObject.remove("id");
                }
            }
        }
        String companyJSONString = companyJSON.toString();


        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", companyJSONString))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("create company with id 1"));

    }

    @Ignore
    @Test
    public void createNewCompanyWithIcon() throws Exception {
        Company testCompany = TestUtil.createTestCompany();
        testCompany.setName("tutor2");
        testCompany.setLink("tutor2.com");
        byte[] content = "this is the Preload51job icon content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("icon", "icon.png", "jpg/png", content);
//        List<MockMultipartFile> icons = new ArrayList<>();
//        icons.add(mockMultipartFile);
        String companyJSONString = new ObjectMapper().writeValueAsString(testCompany);

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .file(mockMultipartFile)
                .param("company", companyJSONString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("create company with id 2"));

    }

    @Ignore
    @Test
    public void createNewCompanyFailedWithDuplicateLink() throws Exception {
        Company testCompany = TestUtil.createTestCompany();

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", new ObjectMapper().writeValueAsString(testCompany)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.url").value(containsString("/companies")))
                .andExpect(jsonPath("$.message").value(containsString("could not execute statement; SQL")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithoutName() throws Exception {
        Company testCompany = TestUtil.createTestCompany();
        testCompany.setName("");

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", new ObjectMapper().writeValueAsString(testCompany)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Company's name is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithNullName() throws Exception {
        Company testCompany = new Company();

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", new ObjectMapper().writeValueAsString(testCompany)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Company's name is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithoutLink() throws Exception {
        Company testCompany = TestUtil.createTestCompany();
        testCompany.setLink("");

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", new ObjectMapper().writeValueAsString(testCompany)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Company's link is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithNullLink() throws Exception {
        Company testCompany = new Company();
        testCompany.setName("newCompany");

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", new ObjectMapper().writeValueAsString(testCompany)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Company's link is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithNullJobPosNum() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", "{\"name\":\"cname\",\"link\":\"clink\",\"jobs\":[{\"dump\":\"dump\"}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Job's position number is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithoutJobPosName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", "{\"name\":\"cname\",\"link\":\"clink\"," +
                        "\"jobs\":[{\"positionNumber\":1,\"positionName\":\"\"}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Job's position name is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithNullJobPosName() throws Exception {
        Company testCompany = new Company();
        testCompany.setName("newCompany");

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", "{\"name\":\"cname\",\"link\":\"clink\"," +
                        "\"jobs\":[{\"positionNumber\":1}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Job's position name is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithoutJobLocation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", "{\"name\":\"cname\",\"link\":\"clink\"," +
                        "\"jobs\":[{\"positionNumber\":1,\"positionName\":\"pname\",\"location\":\"\"}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Job's location is required")));
    }

    @Ignore
    @Test
    public void createNewCompanyWithNullJobLocation() throws Exception {
        Company testCompany = new Company();
        testCompany.setName("newCompany");

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/companies")
                .param("company", "{\"name\":\"cname\",\"link\":\"clink\"," +
                        "\"jobs\":[{\"positionNumber\":1,\"positionName\":\"pname\"}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Job's location is required")));
    }

    @Ignore //this uri will become getAllCompanies
    @Test
    public void getCompanyByEmptyName() throws Exception {
        String uri = "/companies/";
        mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

    }

    @Test
    public void getCompanyByName() throws Exception {
        String uri = "/companies/testCompany";
        mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].id").exists());

    }

    @Test
    public void getCompanyJobs() throws Exception {
        String uri = "/companies/companyuuid/jobs";
        mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasSize(6)));

    }

    @Test
    public void getCompanyJobsFailed() throws Exception {
        String uri = "/companies/badcompanyuuid/jobs";
        mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());

    }

    @Test
    public void searchForJobs() throws Exception {
        String uri = "/companies/jobs";
        MvcResult result = mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .param("keyword", "java"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(1, new JSONArray(result.getResponse().getContentAsString()).length());
    }

    @Test
    public void searchForJobs_withLocationSalaryLimitation() throws Exception {
        String uri = "/companies/jobs";
        MvcResult result = mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .param("keyword", "santa")
                .param("location", "ca")
                .param("salary", "80000"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(1, new JSONArray(result.getResponse().getContentAsString()).length());
    }

}
