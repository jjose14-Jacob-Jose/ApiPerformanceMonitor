package com.stc.apm.controllers;

import com.stc.apm.constants.MainConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class.getName());

    /**
     * Method to check server availability.
     * @return MainConstants.MSG_SUCCESS (String).
     */
    @GetMapping("/healthCheck")
    public String healthCheck() {
        logger.info("Request received at /healthCheck.");
        return MainConstants.MSG_SUCCESS;
    }

    @GetMapping("/home")
    public ModelAndView home() {
        logger.info("Request received at /home.");
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("home");
            return modelAndView;
        } catch (Exception exception) {
            logger.error("Failed to get /home. exception: {}", exception.getMessage());
            return handleException(exception);
        }
    }

    @GetMapping({"/login", "/"})
    public ModelAndView login() {
        logger.info("Request received at /login.");
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("login"); // Set the view name to your error page (e.g., "error.html")
            return modelAndView;

        } catch (Exception exception) {
            logger.error("Failed to get login page. exception: {}", exception.getMessage());
            return null;
        }
    }

    @GetMapping({"/signup"})
    public ModelAndView signUp() {
        logger.info("Request received at /signup.");
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("signup"); // Set the view name to your error page (e.g., "error.html")
            return modelAndView;

        } catch (Exception exception) {
            logger.error("Failed to retrieve /signup. exception: {}", exception.getMessage());
            return null;
        }
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {
        logger.error("Exception occurred while calling HTML endpoints. exception: {}", exception.getMessage());
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("error"); // Set the view name to your error page (e.g., "error.html")
            modelAndView.addObject("exceptionMessage", exception.toString()); // Specify attributes you want to pass to the error page.
            return modelAndView;

        } catch (Exception exceptionLocal) {
            logger.error("Exception while handling HTML-API exception. exceptionLocal: {}", exceptionLocal.getMessage());
            return null;
        }
    }


}
