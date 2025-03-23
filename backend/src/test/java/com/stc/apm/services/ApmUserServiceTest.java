package com.stc.apm.services;

import com.stc.apm.constants.MainConstants;
import com.stc.apm.models.ApiCall;
import com.stc.apm.models.ApmUser;
import com.stc.apm.repositories.ApmUserRepository;
import com.stc.apm.utilities.ApiSystemTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ApmUserServiceTest {

    @Mock
    private ApmUserRepository apmUserRepository;

    @Mock
    private ApiLogService apiLogService;

    @InjectMocks
    private ApmUserService apmUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private List<ApmUser> getApmUserList() {

        List<ApmUser> listApmUsers = new ArrayList<ApmUser>();

        // Create a user instance
        ApmUser apmUser1 = new ApmUser();
        apmUser1.setUsername("username");
        apmUser1.setNameFirst("First");
        apmUser1.setNameLast("Last");
        apmUser1.setEmailId("first.last@email.com");
        apmUser1.setPassword("passwordHash");
        apmUser1.setTimestampRegistration(ApiSystemTime.getInstantTimeAsString());
        apmUser1.setLoginAttemptsFailed(0);
        listApmUsers.add(apmUser1);

        ApmUser apmUser2 = new ApmUser();
        apmUser2.setUsername("username2");
        apmUser2.setNameFirst("First2");
        apmUser2.setNameLast("Last2");
        apmUser2.setEmailId("first.last2@email.com");
        apmUser2.setPassword("passwordHash2");
        apmUser2.setTimestampRegistration(ApiSystemTime.getInstantTimeAsString());
        apmUser2.setLoginAttemptsFailed(1);
        listApmUsers.add(apmUser2);

        return listApmUsers;
    }


    @Test
    void testGetApmUserWithUserName1() {

        ApmUser apmUser = getApmUserList().get(0);
        String username = "testUsernameForMockito";
        username = username.toLowerCase();
        apmUser.setUsername(username);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findApmUserByUsername(username)).thenReturn(apmUser);

//        Calling service this, this will be invoking the Mockito method.
        ApmUser apmUserWithUsernameFromDB = apmUserService.getApmUserByUsername(username);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findApmUserByUsername(username);

        assertNotNull(apmUserWithUsernameFromDB);
        assertEquals(apmUserWithUsernameFromDB, apmUser);

    }

    @Test
    void testGetApmUserWithUserName2() {

        ApmUser apmUser1 = getApmUserList().get(0);
        String username = "testUsernameForMockito";
        username = username.toLowerCase();
        apmUser1.setUsername(username);

        ApmUser apmUser2 = getApmUserList().get(1);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findApmUserByUsername(username)).thenReturn(apmUser2);

//        Calling service this, this will be invoking the Mockito method.
        ApmUser apmUserWithUsernameFromDB = apmUserService.getApmUserByUsername(username);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findApmUserByUsername(username);

        assertNotNull(apmUserWithUsernameFromDB);
        assertNotEquals(apmUserWithUsernameFromDB, apmUser1);

    }

    @Test
    void testGetApmUserWithEmailID1() {

        ApmUser apmUser = getApmUserList().get(0);
        String emailID = "testEmail@email.com";
        apmUser.setEmailId(emailID);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findApmUserByEmailId(emailID)).thenReturn(apmUser);

//        Calling service this, this will be invoking the Mockito method.
        ApmUser apmUserWithEmailIDFromDB = apmUserService.getApmUserByEmailID(emailID);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findApmUserByEmailId(emailID);

        assertNotNull(apmUserWithEmailIDFromDB);
        assertEquals(apmUserWithEmailIDFromDB, apmUser);

    }

    @Test
    void testGetApmUserWithEmailID2() {

        ApmUser apmUser = getApmUserList().get(0);
        String emailID = "testEmail@email.com";
        apmUser.setEmailId(emailID);

        ApmUser apmUser2 = getApmUserList().get(1);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findApmUserByEmailId(emailID)).thenReturn(apmUser2);

//        Calling service this, this will be invoking the Mockito method.
        ApmUser apmUserWithEmailIDFromDB = apmUserService.getApmUserByEmailID(emailID);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findApmUserByEmailId(emailID);

        assertNotNull(apmUserWithEmailIDFromDB);
        assertNotEquals(apmUserWithEmailIDFromDB, apmUser);

    }

    @Test
    void testUnlockApmUserAfterDurationInHours1() {

        long durationMaxForAccountLockInHours = 2;
        int loginAttemptsFailed = 2;
        Instant instantTimestampNow = Instant.now();

        ApmUser apmUser = getApmUserList().get(0);
        apmUser.setLoginAttemptsFailed(loginAttemptsFailed);
        apmUser.setTimestampAccountLocked(instantTimestampNow.toString());
        String accountStatus = apmUserService.unlockApmUserAfterDurationInHours(apmUser, durationMaxForAccountLockInHours);

        assertEquals(accountStatus, MainConstants.MSG_ACCOUNT_LOCK_STATUS_LOCKED);
    }

    @Test
    void testUnlockApmUserAfterDurationInHours2() {

        long durationMaxForAccountLockInHours = 2;
        int loginAttemptsFailed = 2;
        Instant instantTimestampNow = Instant.now().minus(Duration.ofHours(durationMaxForAccountLockInHours * 2));

        ApmUser apmUser = getApmUserList().get(0);
        apmUser.setLoginAttemptsFailed(loginAttemptsFailed);
        apmUser.setTimestampAccountLocked(instantTimestampNow.toString());
        String accountStatus = apmUserService.unlockApmUserAfterDurationInHours(apmUser, durationMaxForAccountLockInHours);

        assertEquals(accountStatus, MainConstants.MSG_ACCOUNT_LOCK_STATUS_UNLOCKED);
    }
}