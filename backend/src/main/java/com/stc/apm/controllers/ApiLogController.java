package com.stc.apm.controllers;

import com.stc.apm.constants.MainConstants;
import com.stc.apm.models.ApiCall;
import com.stc.apm.models.ApmDashboardApiCall;
import com.stc.apm.models.RequestForDateRange;
import com.stc.apm.services.ApiLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/apiCall")
public class ApiLogController {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogController.class.getName());

    @Autowired
    ApiLogService apiLogService;

    /**
     * Storing API calls from APM GUI dashboard.
     * @param apmDashboardApiCall : Object containing information about API Call, username, and Google reCaptcha token.
     * @return Response entity specifying operation status.
     */
    @PostMapping(value = "/saveFromApmDashBoard", produces = "application/json")
    public ResponseEntity<?> saveApiCallFromApmDashBoard(@RequestBody ApmDashboardApiCall apmDashboardApiCall) {
        logger.info("Received request at /saveFromApmDashBoard. apmDashboardApiCall: {}", apmDashboardApiCall.toString());
        String operationStatus = apiLogService.saveToDbApiCallFromApmDashboard(apmDashboardApiCall);

        if(operationStatus.equalsIgnoreCase(MainConstants.MSG_FAILURE)) {
            logger.error("Error logging API call. operationStatus: {}", operationStatus);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(operationStatus);
        } else {
            logger.info("API call logged.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(operationStatus);
        }
    }

    /**
     * Receiving and stores incoming API call logs.
     * @param apiCall : Object of ApiCall containing details of the log.
     * @return HttpStatus.BAD_REQUEST or HttpStatus.ACCEPTED
     */
    @PostMapping(value = "/save", produces = "application/json")
    public ResponseEntity<?> saveAPICall(@RequestBody ApiCall apiCall) {
        logger.info("Received request at /save. apiCall: {}", apiCall.toLogString());

        String operationStatus = apiLogService.saveToDatabaseApiCall(apiCall);
        if (operationStatus.equalsIgnoreCase(MainConstants.MSG_SUCCESS)) {
            logger.info("Successfully saved API call log to database.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(operationStatus);
        }
        logger.error("Couldn't save API call. Exception: {}", operationStatus);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(operationStatus);
    }

    /**
     * Returns list of all logs.
     * @return List of type ApiCall.
     */
    @PostMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<List<ApiCall>> getAll() {
        logger.info("Received request at /getAll.");
        List<ApiCall> listApiCalls = apiLogService.getApiCallsList();
        logger.info("Successfully retrieved {} logs from database.", listApiCalls.size());
        return ResponseEntity.ok(listApiCalls);
    }

    @PostMapping(value = "/getAll/range", produces = "application/json")
    public ResponseEntity<List<ApiCall>> getAllInDateRange(@RequestBody RequestForDateRange requestForDateRange) {
        logger.info("Received request at /getAllInDateRange. requestForDateRange: {}", requestForDateRange.toString());
        List<ApiCall> listApiCalls = apiLogService.getApiCallsWithinRange(requestForDateRange.getDateTimeRangeStartString(), requestForDateRange.getDateTimeRangeEndString());
        logger.info("Successfully retrieved {} logs from database.", listApiCalls.size());
        return ResponseEntity.ok(listApiCalls);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {
        try {
            logger.error("Calling handleException(). exception:{}", exception.toString());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("error"); // Set the view name to your error page (e.g., "error.html")
            modelAndView.addObject("exceptionMessage", exception.toString()); // Specify attributes you want to pass to the error page.
            return modelAndView;

        } catch (Exception exceptionLocal) {
            logger.error("Exception occurred. exceptionLocal:{}", exceptionLocal.toString());
            return null;
        }
    }

}
