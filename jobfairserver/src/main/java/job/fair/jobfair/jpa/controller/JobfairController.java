package job.fair.jobfair.jpa.controller;

import job.fair.data.Page;
import job.fair.jobfair.Exception.UnknownMatchException;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import job.fair.jobfair.jpa.entity.Status;
import job.fair.jobfair.jpa.service.JobfairService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static job.fair.commons.ArgumentCheckUtils.requireNonEmptyValue;

/**
 * Created by annawang on 2/5/16.
 */
@Controller
@RequestMapping("/jobfairs")
public class JobfairController {
    static final Logger logger = Logger.getLogger(JobfairController.class);

    @Autowired
    private JobfairService services;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private MessageSource messageSource;

    /**
     * Create a new Jobfair when name, start/end date.
     *
     * @param jobfair jobfair to be created
     * @return status
     * successful message if the jobfair got created
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    Status createJobfair(@RequestBody Jobfair jobfair) throws Exception {
        requireNonEmptyValue("Jobfair's name", jobfair.getName());
        logger.info("Creating jobfair with name: " + jobfair.getName());

        long id = services.addJobfair(jobfair);
        return new Status("/jobfairs", "New Jobfair created with id " + id);
    }

    /**
     * Add the company to the jobfair
     *
     * @param jobfairID UUID of the jobfair
     * @param company   UUID of the company
     * @return Successful message if everything goes fine
     * @throws Exception any possible exception
     */
    //TODO return this API
    @RequestMapping(value = "/{jobfairID}/companies", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    Status addCompanyToJobfair(@PathVariable String jobfairID, @RequestBody Company company) throws Exception {
        long jobfairlongid = Long.parseLong(jobfairID);
        String url = "/jobfairs/" + jobfairID + "/companies";
        if (services.getJobfair(jobfairlongid) == null) {
            throw new UnknownMatchException("Jobfair " + jobfairID + " does not exist");
        }

        services.addCompanyToJobFair(jobfairlongid, company);
        return new Status(url, "Company added to jobfair " + jobfairID);
    }

    /**
     * Get companies inside the specific Jobfair
     *
     * @param jobfairUUID jobfair id
     * @return company list
     */
    @RequestMapping(value = "/{jobfairID}/companies", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Page<Company> getJobfairCompanies(@PathVariable String jobfairUUID,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "0") int size) {
        requireNonEmptyValue("Jobfair's ID", jobfairUUID);
        logger.info("Getting companies from jobfair with ID: " + jobfairUUID);


        long jobfairlongid = this.services.getJobFairId(jobfairUUID);
        if (jobfairlongid == 0) {
            throw new UnknownMatchException("Jobfair " + jobfairUUID + " does not exist");
        }

        Pageable pageable;
        if (size == 0) {
            pageable = null;
        } else {
            pageable = new PageRequest(page, size);
        }
        return services.getJobfairCompanies(jobfairlongid, pageable);
    }

    /**
     * Get all companies in any jobfair
     *
     * @return company list
     */
    @RequestMapping(value = "/companies", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Page<Company> getAllCompanies() {

        return services.getJobfairCompanies(0, null);
    }

    /**
     * Get the list of Jobfairs
     *
     * @return jobfairs sorted by their start date
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Collection<Jobfair> getJobfairs() {
        logger.info("Getting the list of jobfairs sorted by startDate");
        return services.getAllJobfairs();
    }
}
