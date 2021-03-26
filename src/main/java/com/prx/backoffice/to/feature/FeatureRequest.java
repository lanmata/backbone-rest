/*
 * @(#)FeatureRequest.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.to.feature;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.prx.commons.pojo.Feature;
import com.prx.commons.to.Request;
import com.prx.commons.util.JsonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FeatureRequest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 24-03-2021
 */
@Getter
@Setter
@JsonNaming
@NoArgsConstructor
public class FeatureRequest extends Request {
    private Feature feature;

    @Override
    public String toString(){
       return JsonUtil.toJson(this);
    }
}
