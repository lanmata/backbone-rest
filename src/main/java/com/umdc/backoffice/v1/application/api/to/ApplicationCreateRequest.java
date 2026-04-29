/*
 *  @(#)ApplicationCreateRequest.java
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

package com.umdc.backoffice.v1.application.api.to;


import com.prx.commons.general.pojo.Application;
import com.prx.commons.general.to.Request;

/// Data transfer object for creating an application.
/// Extends the base Request class.
public class ApplicationCreateRequest extends Request {
    private Application application;

    /// Default constructor.
    /// Initializes a new instance of the ApplicationCreateRequest class.
    public ApplicationCreateRequest() {
        // Default constructor
        super();
    }

    /// Gets the application.
    ///
    /// @return the application
    public Application getApplication() {
        return application;
    }

    /// Sets the application.
    ///
    /// @param application the application to set
    public void setApplication(Application application) {
        this.application = application;
    }
}
