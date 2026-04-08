package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.v1.application.service.ApplicationService;
import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.backoffice.v1.users.api.to.UserCreateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateResponse;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.general.pojo.Application;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationRoleUserRepository applicationRoleUserRepository;

    @Mock
    ApplicationService applicationService;

    @Mock
    UserApplicationRoleService userApplicationRoleService;

    @Mock
    UserMapper userMapper;

    @Mock
    PersonMapper personMapper;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByAliasWithoutApplicationId() {
        String alias = "testAlias";
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);

        when(userRepository.findByAlias(alias)).thenReturn(userEntity);
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, null);

        assertNotNull(result);
        verify(userRepository, times(1)).findByAlias(alias);
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindByAliasWithApplicationId() {
        String alias = "testAlias";
        UUID applicationId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);

        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, applicationId);

        assertNotNull(result);
        verify(userRepository, times(1)).findByAliasAndApplication(alias, applicationId);
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindByAliasNotFound() {
        String alias = "testAlias";
        UUID applicationId = UUID.randomUUID();

        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.empty());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, applicationId);

        assertNotNull(result);
        assertNull(result.getBody());
        verify(userRepository, times(1)).findByAliasAndApplication(alias, applicationId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void testFindAllWithApplicationId_UsersFound() {
        UUID applicationId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(userEntity);
        UserTO userTO = new UserTO();
        when(userRepository.findByApplication(applicationId)).thenReturn(userEntities);
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);

        ResponseEntity<List<UserTO>> result = userService.findAll(applicationId);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isEmpty());
        verify(userRepository, times(1)).findByApplication(applicationId);
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindAllWithApplicationId_NoUsers() {
        UUID applicationId = UUID.randomUUID();
        when(userRepository.findByApplication(applicationId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<UserTO>> result = userService.findAll(applicationId);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(userRepository, times(1)).findByApplication(applicationId);
    }

    @Test
    void testFindAllWithoutApplicationId_UsersFound() {
        UserEntity userEntity = new UserEntity();
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(userEntity);
        UserTO userTO = new UserTO();
        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);

        ResponseEntity<List<UserTO>> result = userService.findAll(null);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isEmpty());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindAllWithoutApplicationId_NoUsers() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        ResponseEntity<List<UserTO>> result = userService.findAll(null);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindUserByAlias_StatusCodes() {
        String alias = "testAlias";
        UUID applicationId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);
        UserTO userTO = new UserTO();
        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);
        ResponseEntity<UserTO> found = userService.findUserByAlias(alias, applicationId);
        assertEquals(HttpStatus.OK, found.getStatusCode());
        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.empty());
        ResponseEntity<UserTO> notFound = userService.findUserByAlias(alias, applicationId);
        assertEquals(HttpStatus.CONFLICT, notFound.getStatusCode());
    }

    @Test
    void testValidateEmail_Exists() {
        String email = "test@example.com";
        UUID appId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        when(userRepository.findByEmailAndApplication(email, appId)).thenReturn(Optional.of(userEntity));
        ResponseEntity<Void> response = userService.validateEmail(email, appId);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testValidateEmail_NotExists() {
        String email = "test@example.com";
        UUID appId = UUID.randomUUID();
        when(userRepository.findByEmailAndApplication(email, appId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userService.validateEmail(email, appId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testValidateAlias_Exists() {
        String alias = "alias";
        UUID appId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        when(userRepository.findByAliasAndApplication(alias, appId)).thenReturn(Optional.of(userEntity));
        ResponseEntity<Void> response = userService.validateAlias(alias, appId);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testValidateAlias_NotExists() {
        String alias = "alias";
        UUID appId = UUID.randomUUID();
        when(userRepository.findByAliasAndApplication(alias, appId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userService.validateAlias(alias, appId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testFindUserByUserId_Found() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        UserTO userTO = new UserTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);
        ResponseEntity<UserTO> response = userService.findUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userTO, response.getBody());
    }

    @Test
    void testFindUserByUserId_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ResponseEntity<UserTO> response = userService.findUserById(userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreate_BadRequest_NullRequest() {
        ResponseEntity<UserCreateResponse> response = userService.create((UserCreateRequest) null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_BadRequest_BlankAlias() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("");
        ResponseEntity<UserCreateResponse> response = userService.create(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_BadRequest_BlankPassword() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("alias");
        when(req.password()).thenReturn("");
        ResponseEntity<UserCreateResponse> response = userService.create(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_BadRequest_NullRole() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("alias");
        when(req.password()).thenReturn("password");
        when(req.roleId()).thenReturn(null);
        ResponseEntity<UserCreateResponse> response = userService.create(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdate_BadRequest_NullUserId() {
        UserTO userTO = new UserTO();
        ResponseEntity<UserTO> response = userService.update(null, userTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_ApplicationNotFound() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(applicationService.find(appId)).thenReturn(ResponseEntity.notFound().build());
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_UserNotFound() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application application = new Application();
        application.setId(appId);
        when(applicationService.find(appId)).thenReturn(ResponseEntity.ok(application));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_UserNotInApplication() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application application = new Application();
        application.setId(appId);
        var userEntity = mock(UserEntity.class);
        when(applicationService.find(appId)).thenReturn(ResponseEntity.ok(application));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userEntity.getApplicationRoleUser()).thenReturn(java.util.Set.of());
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_InvalidIds() {
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
