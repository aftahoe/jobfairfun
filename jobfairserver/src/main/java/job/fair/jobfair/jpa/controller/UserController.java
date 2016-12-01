package job.fair.jobfair.jpa.controller;

import job.fair.jobfair.jpa.entity.Status;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.service.CandidateService;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.jpa.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by annawang on 2/19/16.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    static final Logger logger = Logger.getLogger(UserController.class);
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private MessageSource messageSource;

    /**
     * Get user login information including interviewer or candidate
     *
     * @return logined user information
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public
    @ResponseBody
    String getUser() throws Exception {
        User user = this.userService.validateUserSession(request, User.Type.UNKNOWN);

        return this.userService.generateLoginUserObject(user.getUserSessonObject(), user.getUserType()).toString();
    }

    //TODO duplicae
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    private Status handleTypeMismatchException(HttpServletRequest req, Exception e) {
        return new Status(req.getRequestURL().toString(), e.getMessage());
    }
}
