package in.co.rays.proj4.controller;

import in.co.rays.proj4.controller.ORSView;
import in.co.rays.proj4.util.ServletUtility;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * FrontController is a servlet filter that performs session checking before any
 * application controller is invoked. It prevents access to protected resources
 * under {@code /doc/*} and {@code /ctl/*} when the user is not authenticated.
 * <p>
 * If the session does not contain a {@code "user"} attribute, the filter
 * forwards the request to the login view with an error message indicating that
 * the session has expired.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 */
@WebFilter(urlPatterns = { "/doc/*", "/ctl/*" })
public class FrontController implements Filter {

    private static final Logger log = Logger.getLogger(FrontController.class);

    /** 
     * Initializes the filter. No special initialization is required for this
     * implementation, but the method is provided for completeness and future use.
     *
     * @param conf the {@link FilterConfig} provided by the servlet container
     * @throws ServletException if an error occurs during initialization
     */
    public void init(FilterConfig conf) throws ServletException {
        log.info("FrontController filter initialized");
        // No initialization required currently
    }

    /**
     * Performs session validation for incoming requests to protected URL
     * patterns. If the session does not contain a "user" attribute, the request
     * is forwarded to the login view with an appropriate error message; otherwise
     * the request proceeds through the filter chain.
     *
     * @param req   the {@link ServletRequest}
     * @param resp  the {@link ServletResponse}
     * @param chain the {@link FilterChain} to pass control to the next filter or servlet
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if a servlet-specific error occurs during filtering
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession();

        String uri = request.getRequestURI();
        request.setAttribute("uri", uri);

        log.debug("Intercepted request URI: " + uri);

        if (session.getAttribute("user") == null) {
            log.warn("Unauthorized access attempt detected. Session expired or user not logged in.");
            request.setAttribute("error", "Your session has been expired. Please Login again!");
            ServletUtility.forward(ORSView.LOGIN_VIEW, request, response);
            return;
        } else {
            log.debug("User session valid. Proceeding with filter chain.");
            chain.doFilter(req, resp);
        }
    }

    /**
     * Cleans up any resources held by the filter. No cleanup is required for
     * this implementation, but the method is present for completeness.
     */
    public void destroy() {
        log.info("FrontController filter destroyed");
        // No cleanup required currently
    }
}
