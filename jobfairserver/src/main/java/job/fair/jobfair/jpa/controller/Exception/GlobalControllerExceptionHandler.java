package job.fair.jobfair.jpa.controller.Exception;

import job.fair.jobfair.Exception.JobfairException;
import job.fair.jobfair.Exception.UnknownMatchException;
import job.fair.jobfair.jpa.entity.Status;
import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by annawang on 1/29/16.
 */
@ControllerAdvice
class GlobalControllerExceptionHandler {

    /**
     * Handle the Exception of exceeded size of uploading files
     *
     * @param e       The exception thrown - always {@link MaxUploadSizeExceededException}.
     * @param request Current HTTP request.
     * @return The Status with error message.
     */
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    @ResponseBody
    public Status handleMaxUploadException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        String errorURL = request.getRequestURL().toString();
        return new Status(errorURL, e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class, JSONException.class, HttpMessageNotReadableException.class})
    @ResponseBody
    public Status handleArgumentCheckFailedException(Exception e, HttpServletRequest request) {
        e.printStackTrace();
        String errorMessage = "";
        if (e instanceof JSONException) {
            errorMessage = e.getMessage() + " Please make sure the JSON is valid and contains " +
                    "expected property inside Candidate JSON";
        } else {
            errorMessage = e.getMessage();
        }
        String errorURL = request.getRequestURL().toString();
        return new Status(errorURL, errorMessage);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class})
    @ResponseBody
    public Status handleDBConstraintViolation(Exception e, HttpServletRequest request) {

        String errorURL = request.getRequestURL().toString();
        return new Status(errorURL, e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler({UnknownMatchException.class})
    @ResponseBody
    public Status handleUnknownMatch(Exception e, HttpServletRequest request) {

        String errorURL = request.getRequestURL().toString();
        return new Status(errorURL, e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler({JobfairException.class})
    @ResponseBody
    public Status handleJobFairException(Exception e, HttpServletRequest request) {

        String errorURL = request.getRequestURL().toString();
        return new Status(errorURL, e.getMessage());
    }
}