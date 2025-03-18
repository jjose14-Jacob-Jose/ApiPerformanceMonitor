package com.stc.apm.utilities;

import com.stc.apm.config.AppConfig;
import com.stc.apm.constants.MainConstants;
import com.stc.apm.models.ReCaptchaResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


public class RecaptchaUtil {

    /**
     * Method to validate Google reCaptcha.
     * @param userResponse: Google reCaptcha token from Google.
     * @return: MainConstants.FLAG_SUCCESS: If human users; MainConstants.FLAG_FAILURE: if a bot. 
     */
    public static boolean validateRecaptcha(String userResponse) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", AppConfig.getGoogleRecaptchaServerKey());
        map.add("response", userResponse);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<ReCaptchaResponse> reCaptchaApiResponse = restTemplate.postForEntity(AppConfig.getGoogleRecaptchaServerUrl(), request, ReCaptchaResponse.class);

        if (reCaptchaApiResponse.getStatusCode().is2xxSuccessful() && reCaptchaApiResponse.getBody().isSuccess()) {
            return MainConstants.FLAG_SUCCESS;
        }
        return MainConstants.FLAG_FAILURE;
    }
}
