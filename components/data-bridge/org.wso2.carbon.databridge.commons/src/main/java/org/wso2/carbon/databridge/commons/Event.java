/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.commons;

import java.util.Arrays;

public class Event {

    private String streamId;

    private long timeStamp;

    private Object[] metaData;
    private Object[] correlationData;
    private Object[] payloadData;

    public Event() {

    }

    public Event(String streamId, long timeStamp, Object[] metaDataArray,
                 Object[] correlationDataArray,
                 Object[] payloadDataArray) {
        this.streamId=streamId;
        this.timeStamp=timeStamp;
        this.metaData=metaDataArray;
        this.correlationData=correlationDataArray;
        this.payloadData=payloadDataArray;
    }


    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Object[] getMetaData() {
        return metaData;
    }

    public void setMetaData(Object[] metaData) {
        this.metaData = metaData;
    }

    public Object[] getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(Object[] correlationData) {
        this.correlationData = correlationData;
    }

    public Object[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(Object[] payloadData) {
        this.payloadData = payloadData;
    }

    @Override
    public String toString() {
        return "Event{" +
               EBCommonsConstants.STREAM_ID + "='" + streamId + '\'' +
                ", " + EBCommonsConstants.TIME_STAMP + "=" + timeStamp +
               ", " + EBCommonsConstants.META_DATA + "=" + (metaData == null ? null : Arrays.asList(metaData)) +
               ", " + EBCommonsConstants.META_DATA + "=" + (correlationData == null ? null : Arrays.asList(correlationData)) +
               ", " +  EBCommonsConstants.PAYLOAD_DATA  + "=" + (payloadData == null ? null : Arrays.asList(payloadData)) +
               '}';
    }
}
