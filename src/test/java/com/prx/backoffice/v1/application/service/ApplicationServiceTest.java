/*
 *  @(#)ApplicationServiceTest.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */

package com.prx.backoffice.v1.application.service;

import com.prx.commons.pojo.Application;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationServiceTest {

    @Test
    @DisplayName("Create application throws NotImplementedException")
    void createApplicationThrowsNotImplementedException() {
        ApplicationService applicationService = new ApplicationService() {};
        Application application = new Application();
        assertThrows(NotImplementedException.class, () -> applicationService.create(application));
    }

    @Test
    @DisplayName("Find application by ID throws NotImplementedException")
    void findApplicationByIdThrowsNotImplementedException() {
        ApplicationService applicationService = new ApplicationService() {};
        assertThrows(NotImplementedException.class, () -> applicationService.find("1"));
    }

    @Test
    @DisplayName("Update application throws NotImplementedException")
    void updateApplicationThrowsNotImplementedException() {
        ApplicationService applicationService = new ApplicationService() {};
        Application application = new Application();
        assertThrows(NotImplementedException.class, () -> applicationService.update("1", application));
    }

    @Test
    @DisplayName("Delete application throws NotImplementedException")
    void deleteApplicationThrowsNotImplementedException() {
        ApplicationService applicationService = new ApplicationService() {};
        Application application = new Application();
        assertThrows(NotImplementedException.class, () -> applicationService.delete("1", application));
    }

    @Test
    @DisplayName("List applications throws NotImplementedException")
    void listApplicationsThrowsNotImplementedException() {
        ApplicationService applicationService = new ApplicationService() {};
        assertThrows(NotImplementedException.class, () -> applicationService.list("1"));
    }
}
