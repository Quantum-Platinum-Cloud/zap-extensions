/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.paramdigger;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

public class ParamGuessResult {

    private String paramName;
    private HistoryReference historyReference;
    private List<Reason> reasons;
    private static final Logger LOGGER = LogManager.getLogger(ParamGuessResult.class);

    public ParamGuessResult(String paramName, List<Reason> reasons, HttpMessage httpMessage) {
        this.paramName = paramName;
        this.reasons = reasons;
        try {
            this.historyReference =
                    new HistoryReference(
                            Model.getSingleton().getSession(),
                            HistoryReference.TYPE_PARAM_DIGGER,
                            httpMessage);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            LOGGER.warn(
                    "Error creating history reference. Exception raised: {}", e.getMessage(), e);
        }
    }

    public ParamGuessResult(String paramName, List<Reason> reasons, HistoryReference ref) {
        this.paramName = paramName;
        this.reasons = reasons;
        this.historyReference = ref;
    }

    public String getParamName() {
        return paramName;
    }

    public List<Reason> getReasons() {
        return reasons;
    }

    public HttpMessage getHttpMessage() {
        try {
            return this.historyReference.getHttpMessage();
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            LOGGER.warn("Error getting HTTP message. Exception raised: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (reasons != null) {
            for (Reason reason : reasons) {
                sb.append(
                        Constant.messages.getString(
                                "paramdigger.results.reason." + reason.toString()));
                sb.append(", ");
            }
        }
        sb.setLength(sb.length() - 2);
        return Constant.messages.getString(
                "paramdigger.results.maintext", this.historyReference.getURI(), getParamName(), sb);
    }
}
