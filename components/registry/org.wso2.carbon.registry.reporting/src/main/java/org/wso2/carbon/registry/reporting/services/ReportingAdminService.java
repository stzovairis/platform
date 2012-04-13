/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.reporting.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.registry.common.beans.ReportConfigurationBean;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.reporting.annotation.Property;
import org.wso2.carbon.registry.reporting.internal.ReportingServiceComponent;
import org.wso2.carbon.registry.reporting.utils.ReportingTask;
import org.wso2.carbon.registry.reporting.utils.Utils;

import java.lang.reflect.Method;
import java.util.*;

public class ReportingAdminService extends RegistryAbstractAdmin {

    private static final Log log = LogFactory.getLog(ReportingAdminService.class);
    public static final String REPORTING_CONFIG_PATH = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
            "/repository/components/org.wso2.carbon.registry.reporting/configurations/";

    public byte[] getReportBytes(ReportConfigurationBean configuration)
            throws Exception {
        return Utils.getReportContentStream(
                configuration.getReportClass(), configuration.getTemplate(),
                configuration.getType(), CommonUtil.attributeArrayToMap(
                configuration.getAttributes()), getRootRegistry()).toByteArray();
    }

    public void scheduleReport(ReportConfigurationBean configuration)
            throws Exception {
        HashMap<String, String> propertyMap = new HashMap<String, String>(
                CommonUtil.attributeArrayToMap(configuration.getAttributes()));
        propertyMap.put("reporting.registry.url", configuration.getRegistryURL());
        propertyMap.put("reporting.registry.username", configuration.getUsername());
        propertyMap.put("reporting.registry.password", configuration.getPassword());
        propertyMap.put("reporting.type", configuration.getType());
        propertyMap.put("reporting.class", configuration.getReportClass());
        propertyMap.put("reporting.template", configuration.getTemplate());
        propertyMap.put("reporting.resource.path", configuration.getResourcePath());
        String clazz = ReportingTask.class.getName();
        TaskManager taskManager = ReportingServiceComponent.getTaskManager();
        taskManager.registerTask(new TaskInfo(configuration.getName(), clazz, propertyMap,
                new TaskInfo.TriggerInfo(configuration.getCronExpression())));
        taskManager.rescheduleTask(configuration.getName());
    }

    public void stopScheduledReport(String name) throws Exception {
        TaskManager taskManager = ReportingServiceComponent.getTaskManager();
        taskManager.pauseTask(name);
        taskManager.deleteTask(name);
    }

    public void saveReport(ReportConfigurationBean configuration)
            throws RegistryException, CryptoException {
        Registry registry = getRootRegistry();
        Resource resource = registry.newResource();
        resource.setMediaType("application/vnd.wso2.registry-report");
        if (configuration.getCronExpression() != null) {
            resource.setProperty("cronExpression", configuration.getCronExpression());
        } else {
            resource.setProperty("cronExpression", "");
        }
        if (configuration.getReportClass() != null) {
            resource.setProperty("class", configuration.getReportClass());
        } else {
            resource.setProperty("class", "");
        }
        if (configuration.getReportClass() != null) {
            resource.setProperty("resourcePath", configuration.getReportClass());
        } else {
            resource.setProperty("resourcePath", "");
        }
        if (configuration.getTemplate() != null) {
            resource.setProperty("template", configuration.getTemplate());
        } else {
            resource.setProperty("template", "");
        }
        if (configuration.getType() != null) {
            resource.setProperty("type", configuration.getType());
        } else {
            resource.setProperty("type", "");
        }
        if (configuration.getRegistryURL() != null) {
            resource.setProperty("registry.url", configuration.getRegistryURL());
        } else {
            resource.setProperty("registry.url", "");
        }
        if (configuration.getUsername() != null) {
            resource.setProperty("registry.username", configuration.getUsername());
        } else {
            resource.setProperty("registry.username", "");
        }
        if (configuration.getPassword() != null) {
            resource.setProperty("registry.password",
                    CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                            configuration.getPassword().getBytes()));
        } else {
            resource.setProperty("registry.password",
                    CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                            "".getBytes()));
        }
        for (Map.Entry<String, String> e :
                CommonUtil.attributeArrayToMap(configuration.getAttributes()).entrySet()) {
            resource.setProperty("attribute." + e.getKey(), e.getValue());
        }
        registry.put(REPORTING_CONFIG_PATH + configuration.getName(), resource);
    }

    public ReportConfigurationBean[] getSavedReports()
            throws RegistryException, CryptoException, TaskException {
        Registry registry = getRootRegistry();
        List<ReportConfigurationBean> output = new LinkedList<ReportConfigurationBean>();
        if (registry.resourceExists(REPORTING_CONFIG_PATH)) {
            Collection collection = (Collection) registry.get(REPORTING_CONFIG_PATH);
            String[] children = collection.getChildren();
            for (String child : children) {
                ReportConfigurationBean bean = getConfigurationBean(child);
                output.add(bean);
            }
        }
        return output.toArray(new ReportConfigurationBean[output.size()]);
    }

    public ReportConfigurationBean getSavedReport(String name)
            throws RegistryException, CryptoException, TaskException {
        return getConfigurationBean(
                REPORTING_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR + name);
    }

    public void deleteSavedReport(String name)
            throws RegistryException {
        getRootRegistry().delete(REPORTING_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR + name);
    }

    public void copySavedReport(String name, String newName)
            throws RegistryException {
        getRootRegistry().copy(REPORTING_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR + name,
                REPORTING_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR + newName);
    }

    private ReportConfigurationBean getConfigurationBean(String path)
            throws RegistryException, CryptoException, TaskException {
        Resource resource = getRootRegistry().get(path);
        ReportConfigurationBean bean = new ReportConfigurationBean();
        String name = RegistryUtils.getResourceName(path);
        bean.setName(name);
        bean.setCronExpression(resource.getProperty("cronExpression"));
        TaskManager taskManager = ReportingServiceComponent.getTaskManager();
        bean.setScheduled(taskManager.isTaskScheduled(name));
        bean.setReportClass(resource.getProperty("class"));
        bean.setResourcePath(resource.getProperty("resourcePath"));
        bean.setTemplate(resource.getProperty("template"));
        bean.setType(resource.getProperty("type"));
        bean.setRegistryURL(resource.getProperty("registry.url"));
        bean.setUsername(resource.getProperty("registry.username"));
        bean.setPassword(new String(
                CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                        resource.getProperty("registry.password"))));
        Map<String, String> attributes = new HashMap<String, String>();
        Properties props = resource.getProperties();
        for (Object key : props.keySet()) {
            String propKey = (String) key;
            if (propKey.startsWith("attribute.")) {
                attributes.put(propKey.substring("attribute.".length()),
                        resource.getProperty(propKey));
            }
        }
        bean.setAttributes(CommonUtil.mapToAttributeArray(attributes));
        return bean;
    }

    public String[] getAttributeNames(String className) throws Exception {
        List<String> output = new LinkedList<String>();
        Method[] declaredMethods = Class.forName(className).getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Property.class)) {
                String name = method.getName();
                if (name.startsWith("set")) {
                    name = name.substring("set".length());
                }
                output.add(name.substring(0, 1).toLowerCase() + name.substring(1));
            }
        }
        return output.toArray(new String[output.size()]);
    }

    public String[] getMandatoryAttributeNames(String className) throws Exception {
        List<String> output = new LinkedList<String>();
        Method[] declaredMethods = Class.forName(className).getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Property.class) &&
                    method.getAnnotation(Property.class).value()) {
                String name = method.getName();
                if (name.startsWith("set")) {
                    name = name.substring("set".length());
                }
                output.add(name.substring(0, 1).toLowerCase() + name.substring(1));
            }
        }
        return output.toArray(new String[output.size()]);
    }

}