package com.stc.apm.services;

import com.stc.apm.constants.MainConstants;
import com.stc.apm.models.ApiCall;
import com.stc.apm.models.ApmDashboardApiCall;
import com.stc.apm.repositories.ApiLogRepository;
import com.stc.apm.utilities.ApiSystemTime;
import com.stc.apm.utilities.RecaptchaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiLogService {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogService.class.getName());

    @Autowired
    private ApiLogRepository incomingRequestsRepository;

    /**
     * Save an API from the APM Dashboard.
     * @param apmDashboardApiCall Object containing username, Google reCaptcha token, and API Log.
     * @return MainConstants.MSG_FAILURE or MainConstants.MSG_SUCCESS
     */
    public String saveToDbApiCallFromApmDashboard(ApmDashboardApiCall apmDashboardApiCall) {
        logger.info("Calling saveToDbApiCallFromApmDashboard(). apmDashboardApiCall: {}", apmDashboardApiCall.toString());
        if (apmDashboardApiCall == null || apmDashboardApiCall.getApiCall() == null)
            return MainConstants.MSG_FAILURE;

//        Checking Google reCaptcha.
        if(! (RecaptchaUtil.validateRecaptcha(apmDashboardApiCall.getGoogleReCaptchaToken())))
            return MainConstants.MSG_FAILURE;

//        Adding username to the API caller name.
        ApiCall apiCallFromRequest = apmDashboardApiCall.getApiCall();
        String apiLogCallerNameWithUsername = apmDashboardApiCall.getUsername() + MainConstants.MSG_DELIMITER_USERNAME_TO_CALLER_NAME + apiCallFromRequest.getCallerName();
        apiCallFromRequest.setCallerName(apiLogCallerNameWithUsername);

        return saveToDatabaseApiCall(apiCallFromRequest);

    }

    /**
     *  Saves API to database.
     * @param apiCall: contains parameters to be saved to database.
     * @return boolean based on success of operation.
     */
    public String saveToDatabaseApiCall(ApiCall apiCall) {
        logger.info("Calling saveToDatabaseAPICall(). apiCall: {}", apiCall.toString());
        if(apiCall.getCallerTimestampUTC() == null || apiCall.getCallerTimestampUTC().equalsIgnoreCase(MainConstants.STRING_EMPTY))
            apiCall.setCallerTimestampUTC(ApiSystemTime.getInstantTimeAsString());
        apiCall.setCallId(null);

        try {
    //      Saving to database.
            incomingRequestsRepository.save(apiCall);
            return MainConstants.MSG_SUCCESS;

        } catch (Exception exception) {
            logger.error("Failed to save to DB. exception: {}", exception.getMessage());
            return exception.toString();
        }
    }

    /**
     * Get all rows.
     * @return : ArrayList containing all API logs.
     */
    public List<ApiCall> getApiCallsList() {
        logger.info("Calling getAPICallsList().");

        int maxRows = 0;
//        Identify the type of the logged-in user.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            maxRows = MainConstants.COUNT_SEARCH_RESULTS_ROLE_USER;
            logger.info("Authentication successful.");
        }

        List<ApiCall> listApiCallsFromDB = null;

        try {
            if (maxRows > 0) {
//                For role ROLE_USER return only 'maxRows' number of rows.
                Pageable pageable = PageRequest.of(0, maxRows, Sort.by(Sort.Order.desc("callerTimestampUTC")));
                Page<ApiCall> paginatedResults = incomingRequestsRepository.findAll(pageable);
                listApiCallsFromDB = paginatedResults.getContent();
                logger.info("User is a general member. Returning {} APICalls.", listApiCallsFromDB.size());
            } else {
//                For role ROLE_ADMIN return all rows.
                listApiCallsFromDB = incomingRequestsRepository.findAll(Sort.by(Sort.Order.desc("callerTimestampUTC")));
                logger.info("User is an elevated member. Returning {} APICalls.", listApiCallsFromDB.size());
            }
        } catch (Exception exception) {
            logger.error("Failed to getAPICallsList(). exception: {}", exception.getMessage());
        }
        return listApiCallsFromDB;
    }

    /**
     * Return rows that are within the timeframe.
     * @param dateTimeRangeStartString : Timeframe start date (date, month, year, hour, and minutes).
     * @param dateTimeRangeEndString : Timeframe end date (date, month, year, hour, and minutes).
     * @return : ArrayList containing all API calls within the range.
     */
    public List<ApiCall> getApiCallsWithinRange(String dateTimeRangeStartString, String dateTimeRangeEndString) {
        logger.info("Calling getApicallsWithinRange(). dateTimeRangeStartString: {}", dateTimeRangeStartString);

        List<ApiCall> listApiCallsFromDB = null;

        try {
            listApiCallsFromDB = incomingRequestsRepository.findByCallerTimestampUTCBetween(dateTimeRangeStartString, dateTimeRangeEndString);
        } catch (Exception exception) {
            logger.error("Failed to getAPICallsWithinRange(). exception: {}", exception.getMessage());
        }
        return listApiCallsFromDB;
    }

    /**
     * Saves API call by a user in APM interface.
     * @param apmDashboardApiCall: Object containing username and also API call.
     * @return MainConstants.MSG_FAILURE or MainConstants.MSG_SUCCESS
     */
    public String handleApiLogFromApmUser(ApmDashboardApiCall apmDashboardApiCall) {
        logger.info("Calling handleApiLogFromApmUser(). apmDashboardApiCall: {}", apmDashboardApiCall.toLogString());

        if (apmDashboardApiCall == null || apmDashboardApiCall.getApiCall() == null) {
            logger.error("request object is null.");
            return MainConstants.MSG_FAILURE;
        }

        ApiCall apiCall = apmDashboardApiCall.getApiCall();
        apiCall.setCallId(null);
        String apiCallIdWithCallerUsername = apmDashboardApiCall.getUsername() + MainConstants.MSG_DELIMITER_USERNAME_TO_CALLER_NAME + apiCall.getCallerName();
        apiCall.setCallerName(apiCallIdWithCallerUsername);

        saveToDatabaseApiCall(apiCall);

        return MainConstants.MSG_SUCCESS;

    }
}
