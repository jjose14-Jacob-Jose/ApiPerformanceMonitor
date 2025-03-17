package com.jacob.apm.controllers;

import com.jacob.apm.constants.MainConstants;
import com.jacob.apm.models.APICall;
import com.jacob.apm.models.ApmDashboardApiCall;
import com.jacob.apm.models.RequestForDateRange;
import com.jacob.apm.services.APILogService;
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
public class APILogController {

    private static final Logger logger = LoggerFactory.getLogger(APILogController.class.getName());

    @Autowired
    APILogService apiLogService;

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
     * @param apiCall : Object of APICall containing details of the log.
     * @return HttpStatus.BAD_REQUEST or HttpStatus.ACCEPTED
     */
    @PostMapping(value = "/save", produces = "application/json")
    public ResponseEntity<?> saveAPICall(@RequestBody APICall apiCall) {
        logger.info("Received request at /save. apiCall: {}", apiCall.toLogString());

        String operationStatus = apiLogService.saveToDatabaseAPICall(apiCall);
        if (operationStatus.equalsIgnoreCase(MainConstants.MSG_SUCCESS)) {
            logger.info("Successfully saved API call log to database.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(operationStatus);
        }
        logger.error("Couldn't save API call. Exception: {}", operationStatus);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(operationStatus);
    }

    /**
     * Returns list of all logs.
     * @return List of type APICall.
     */
    @PostMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<List<APICall>> getAll() {
        logger.info("Received request at /getAll.");
        List<APICall> listAPICalls = apiLogService.getAPICallsList();
        logger.info("Successfully retrieved {} logs from database.", listAPICalls.size());
        return ResponseEntity.ok(listAPICalls);
    }

    @PostMapping(value = "/getAll/range", produces = "application/json")
    public ResponseEntity<List<APICall>> getAllInDateRange(@RequestBody RequestForDateRange requestForDateRange) {
        logger.info("Received request at /getAllInDateRange. requestForDateRange: {}", requestForDateRange.toString());
        List<APICall> listAPICalls = apiLogService.getAPICallsWithinRange(requestForDateRange.getDateTimeRangeStartString(), requestForDateRange.getDateTimeRangeEndString());
        logger.info("Successfully retrieved {} logs from database.", listAPICalls.size());
        return ResponseEntity.ok(listAPICalls);
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
