package job.fair.jobfair.jpa.controller;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Question;
import job.fair.jobfair.jpa.entity.Status;
import job.fair.jobfair.jpa.service.CompanyService;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static job.fair.commons.ArgumentCheckUtils.require;
import static job.fair.commons.ArgumentCheckUtils.requireNonEmptyValue;

/**
 * Created by annawang on 1/22/16.
 */
@Controller
@RequestMapping("/companies")
public class CompanyController {

    static final Logger logger = Logger.getLogger(CompanyController.class);
    @Autowired
    private CompanyService services;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private MessageSource messageSource;

    /**
     * Create a new company with company json and its icon file
     *
     * @param companyJsonString company json
     * @param icons             icon png file which is not required
     * @return Successful message if company is added
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    Status addCompany(@RequestParam(value = "company", required = true) String companyJsonString,
                      @RequestParam(value = "icon", required = true) List<MultipartFile> icons) throws Exception {
        logger.debug("Create the company by JSON string: " + companyJsonString);
        Company company = validateCompanyJSON(companyJsonString);
        // Add the company with icon
        if (icons.size() != 0) {
            URL iconURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "/jobfair/company/icon/");
            long id = services.addCompany(company, icons.get(0), iconURL.toString());
            return new Status("/companies", "create company with id " + id);
        }
        // Add the company without icon
        long companyId = services.addCompany(company).getId();
        return new Status("/companies", "create company with id " + companyId);
    }


    /**
     * Get company by its name.
     *
     * @param name company name
     * @return list of companies
     * @throws UnsupportedEncodingException exception
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Collection<Company> getCompanyByName(@PathVariable String name) throws UnsupportedEncodingException {
        requireNonEmptyValue("Company's name", name);

        return services.getCompanyByName(name);
    }

    /**
     * Get job listings from a company
     *
     * @param companyID company ID
     * @return All job listings with job details
     * @throws Exception Any possible exception
     */
    @RequestMapping(value = "/{companyID}/jobs", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Page getCompanyJobs(@PathVariable String companyID,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "0") int size) throws Exception {
        requireNonEmptyValue("Company's id", companyID);

        logger.debug("company with id " + companyID);
        Pageable pageable;
        if (size == 0) {
            pageable = null;
        } else {
            pageable = new PageRequest(page, size);
        }
        Page jobs = services.getCompanyJobs(companyID, pageable);

        if (jobs == null) {
            throw new IllegalArgumentException("Cannot find a company with id " + companyID);
        }
        return jobs;
    }

    /**
     * Search jobs by keyword
     *
     * @param keyword  keyword to search
     * @param location this is optional search
     * @param salary   this is optional search
     * @param sort     sort by requirement
     * @return list of related jobs
     * @throws Exception Any possible exception
     */
    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Collection<Job> searchCompanyJobs(@RequestParam(value = "keyword", required = true) String keyword,
                                      @RequestParam(value = "location", required = false) String location,
                                      @RequestParam(value = "salary", required = false) Integer salary,
                                      @RequestParam(value = "sort", required = false) String sort) throws Exception {
        requireNonEmptyValue("search keyword ", keyword);
        if (sort != null && (
                !Job.SORTING_ASC_COMPANY_NAME.equals(sort) || !Job.SORTING_DESC_COMPANY_NAME.equals(sort) ||
                        !Job.SORTING_ASC_POSITION_NAME.equals(sort) || !Job.SORTING_DESC_POSITION_NAME.equals(sort) ||
                        !Job.SORTING_ASC_LOCATION.equals(sort) || !Job.SORTING_DESC_LOCATION.equals(sort))) {
            throw new IllegalArgumentException("Invalid sorting type " + sort);
        }

        return this.services.searchJobs(keyword, location, salary == null ? 0 : salary, sort);
    }



    /*
    /**
     * Let front-end get the company icon if there is one
     *
     * @param fileName fileName is {companyId}.png for now
     * @return company icon file

    @RequestMapping(value = "/icon/{file_name:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    FileSystemResource getIconFile(@PathVariable("file_name") String fileName) {
        return new FileSystemResource(new File("/tmp/icons/" + fileName));

    }*/

    private String getStringJSONValue(JSONObject json, String key) {
        return json.isNull(key) ? "" : json.getString(key);
    }

    //TODO simplify
    private Company validateCompanyJSON(String companyJsonString) {
        JSONObject companyJSON = new JSONObject(companyJsonString);
        Company company = new Company();
        company.setName(requireNonEmptyValue("Company's name", getStringJSONValue(companyJSON, "name")));
        company.setLink(requireNonEmptyValue("Company's link", getStringJSONValue(companyJSON, "link")));
        company.setEmailDomain(getStringJSONValue(companyJSON, "emailDomain"));
        company.setUuid(getStringJSONValue(companyJSON, "uuid"));
        JSONArray jobs = companyJSON.isNull("jobs") ? new JSONArray() : companyJSON.getJSONArray("jobs");

        for (int i = 0; i < jobs.length(); i++) {
            JSONObject jobJSON = jobs.getJSONObject(i);
            Job job = new Job();
            job.setUuid(getStringJSONValue(jobJSON, "uuid"));
            job.setPositionCount(jobJSON.isNull("positionCount") ? 0 : jobJSON.getInt("positionCount"));
            job.setPositionNumber(require("Job's position number", jobJSON.isNull("positionNumber") ? null : jobJSON.getInt("positionNumber")));
            job.setPositionName(requireNonEmptyValue("Job's position name", getStringJSONValue(jobJSON, "positionName")));
            job.setLocation(requireNonEmptyValue("Job's location", getStringJSONValue(jobJSON, "location")));
            job.setCompensation(jobJSON.getInt("compensation"));
            job.setIssueDate(getStringJSONValue(jobJSON, "issueDate"));
            job.setContractType(getStringJSONValue(jobJSON, "contractType"));
            job.setDescription(getStringJSONValue(jobJSON, "description"));
            if (!jobJSON.isNull("questionSet")) {
                JSONArray questions = jobJSON.getJSONArray("questionSet");
                for (int j = 0; j < questions.length(); j++) {
                    JSONObject questionJSON = questions.getJSONObject(j);
                    Question question = new Question();
                    question.setUuid(getStringJSONValue(questionJSON, "uuid"));
                    question.setQuestionText(requireNonEmptyValue("Question's Text", getStringJSONValue(questionJSON, "questionText")));
                    question.setAnswer(require("Question's Answer", questionJSON.isNull("answer") ? null : (short) questionJSON.getInt("answer")));
                    JSONArray options = questionJSON.getJSONArray("questionOptions");
                    if (options.length() == 0) {
                        throw new IllegalArgumentException("Question options are required for setting a question");
                    }
                    for (int k = 0; k < options.length(); k++) {
                        question.addQuestionOption(options.getString(k));
                    }
                    job.addQuestion(question);
                }
            }

            if (!jobJSON.isNull("requiredSkills")) {
                JSONArray requiredSkills = jobJSON.getJSONArray("requiredSkills");
                for (int j = 0; j < requiredSkills.length(); j++) {
                    job.addRequiredSkills(requiredSkills.getString(j));
                }
            }
            company.addJob(job);
        }
        return company;
    }
}
