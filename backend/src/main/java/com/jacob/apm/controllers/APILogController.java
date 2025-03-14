package com.jacob.apm.controllers;

import com.jacob.apm.constants.MainConstants;
import com.jacob.apm.models.APICall;
import com.jacob.apm.models.ApmDashboardApiCall;
import com.jacob.apm.models.RequestForDateRange;
import com.jacob.apm.services.APILogService;
import com.jacob.apm.utilities.APMLogger;
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

    @Autowired
    APILogService apiLogService;

    /**
     * Storing API calls from APM GUI dashboard.
     * @param apmDashboardApiCall : Object containing information about API Call, username, and Google reCaptcha token.
     * @return Response entity specifying operation status.
     */
    @PostMapping(value = "/saveFromApmDashBoard", produces = "application/json")
    public ResponseEntity<?> saveApiCallFromApmDashBoard(@RequestBody ApmDashboardApiCall apmDashboardApiCall) {
        APMLogger.logMethodEntry("saveApiCallFromApmDashBoard()");

        String operationStatus = apiLogService.saveToDbApiCallFromApmDashboard(apmDashboardApiCall);

        if(operationStatus.equalsIgnoreCase(MainConstants.MSG_FAILURE)) {
            APMLogger.logError("saveApiCallFromApmDashBoard(): "+operationStatus);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(operationStatus);
        } else {
            APMLogger.logMethodExit("saveApiCallFromApmDashBoard()");
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
        APMLogger.logMethodEntry("Saving API call log to database. ");

        String operationStatus = apiLogService.saveToDatabaseAPICall(apiCall);
        if (operationStatus.equalsIgnoreCase(MainConstants.MSG_SUCCESS)) {
            APMLogger.logMethodExit("Successfully saved API call. ");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(operationStatus);
        }

        APMLogger.logError("Couldn't save API call. Exception: "+operationStatus);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(operationStatus);
    }

    /**
     * Returns list of all logs.
     * @return List of type APICall.
     */
    @PostMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<List<APICall>> getAll() {
        String messageLog = "/getAll from APM DashBoard";
        APMLogger.logMethodEntry(messageLog);

        List<APICall> listAPICalls = apiLogService.getAPICallsList();

        APMLogger.logMethodExit(messageLog);
        return ResponseEntity.ok(listAPICalls);
    }

    @PostMapping(value = "/getAll/range", produces = "application/json")
    public ResponseEntity<List<APICall>> getAllInDateRange(@RequestBody RequestForDateRange requestForDateRange) {
        String methodNameForLogger = "getAllInDateRange() from APM Dashboard";
        APMLogger.logMethodEntry(methodNameForLogger);

        List<APICall> listAPICalls = apiLogService.getAPICallsWithinRange(requestForDateRange.getDateTimeRangeStartString(), requestForDateRange.getDateTimeRangeEndString());

        APMLogger.logMethodExit(methodNameForLogger);
        return ResponseEntity.ok(listAPICalls);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {
        try {
            APMLogger.logMethodEntry("handleException() " + exception.toString());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("error"); // Set the view name to your error page (e.g., "error.html")
            modelAndView.addObject("exceptionMessage", exception.toString()); // Specify attributes you want to pass to the error page.
            return modelAndView;

        } catch (Exception exceptionLocal) {
            APMLogger.logError("ERROR - EditionController - @ExceptionHandler(Exception.class) - handleException( " + exception.toString() + ")", exception);
            return null;
        }
    }

}
