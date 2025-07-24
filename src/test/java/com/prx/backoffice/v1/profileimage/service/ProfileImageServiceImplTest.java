package com.prx.backoffice.v1.profileimage.service;

import com.prx.backoffice.util.JwtUtil;
import com.prx.backoffice.v1.profileimage.to.GetProfileImageReferenceResponse;
import com.prx.backoffice.v1.profileimage.to.PostProfileImageResponse;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProfileImageServiceImplTest {

    @Mock
    private ApplicationRoleUserRepository applicationRoleUserRepository;

    @InjectMocks
    private ProfileImageServiceImpl profileImageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave_Success() throws Exception {

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            // Arrange
            String token = "valid-token";
            UUID applicationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            byte[] image = new byte[]{1, 2, 3};

            ApplicationEntity application = new ApplicationEntity();
            application.setCodeName("app-code");
            application.setId(applicationId);
            RoleEntity roleEntity = new RoleEntity();
            UserEntity userEntity = new UserEntity();

            roleEntity.setActive(true);
            roleEntity.setDescription("role-description");
            roleEntity.setName("role-name");
            roleEntity.setId(UUID.randomUUID());

            userEntity.setId(UUID.randomUUID());
            userEntity.setAlias("alias");
            userEntity.setPassword("password");
            userEntity.setEmail("email");
            ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
            ApplicationRoleUserEntityId  applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
            applicationRoleUserEntityId.setApplicationId(applicationId);
            applicationRoleUserEntityId.setRoleId(roleEntity.getId());
            applicationRoleUserEntityId.setUserId(userId);
            applicationRoleUser.setId(applicationRoleUserEntityId);
            applicationRoleUser.setApplication(application);
            applicationRoleUser.setRole(roleEntity);
            applicationRoleUser.setUser(userEntity);
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(applicationRoleUser);

            // Act
            ResponseEntity<PostProfileImageResponse> response = profileImageService.save(token, applicationId, image);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().ref().contains("app-code"));
        }
    }

    @Test
    void testSave_NotFound() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] image = new byte[]{1, 2, 3};

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(null);

            // Act
            ResponseEntity<PostProfileImageResponse> response = profileImageService.save(token, applicationId, image);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("", response.getBody().ref());
        }
    }

    @Test
    void testGetProfileImageReference_Success() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String profileImageRef = "path/to/image.jpg";

        ApplicationEntity application = new ApplicationEntity();
        application.setCodeName("app-code");
        RoleEntity roleEntity = new RoleEntity();
        UserEntity userEntity = new UserEntity();

        roleEntity.setActive(true);
        roleEntity.setDescription("role-description");
        roleEntity.setName("role-name");
        roleEntity.setId(UUID.randomUUID());

        userEntity.setId(UUID.randomUUID());
        userEntity.setAlias("alias");
        userEntity.setPassword("password");
        userEntity.setEmail("email");

        ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
        ApplicationRoleUserEntityId  applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
        applicationRoleUserEntityId.setApplicationId(applicationId);
        applicationRoleUserEntityId.setRoleId(roleEntity.getId());
        applicationRoleUserEntityId.setUserId(userId);
        applicationRoleUser.setId(applicationRoleUserEntityId);
        applicationRoleUser.setApplication(application);
        applicationRoleUser.setRole(roleEntity);
        applicationRoleUser.setUser(userEntity);

        applicationRoleUser.setProfileImageRef(profileImageRef);

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(applicationRoleUser);

            // Act
            ResponseEntity<GetProfileImageReferenceResponse> response = profileImageService.getProfileImageReference(token, applicationId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(profileImageRef, response.getBody().ref());
        }
    }

    @Test
    void testGetProfileImageReference_NotFound() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);

            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(null);
            // Act
            ResponseEntity<GetProfileImageReferenceResponse> response = profileImageService.getProfileImageReference(token, applicationId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }

    }
}
