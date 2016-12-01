package job.fair.jobfair.Exception;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by annawang on 1/28/16.
 */
public class MultipartExceptionHandler extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (MaxUploadSizeExceededException e) {
            handle(request, response, e);
        } catch (ServletException e) {
            if (e.getRootCause() instanceof MaxUploadSizeExceededException) {
                handle(request, response, (MaxUploadSizeExceededException) e.getRootCause());
            } else {
                throw e;
            }
        }
    }

    private void handle(HttpServletRequest request,
                        HttpServletResponse response, MaxUploadSizeExceededException e) throws ServletException, IOException {

        //String redirect = UrlUtils.buildFullRequestUrl(request) + "?error";
        response.sendRedirect("error");
    }

}