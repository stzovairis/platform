/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.rest.api;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.PropertyModel;

/**
 * This class retrieves the properties of the requested resource according to
 * the parameters passed with the request.
 */
@Path("/properties")
public class Properties extends PaginationCalculation<PropertyModel> {

	private Log log = LogFactory.getLog(Properties.class);
	private String path = null;

	/**
	 * This method get the properties on the requested resource.
	 * 
	 * @param resourcePath
	 *            - path of the resource in the registry.
	 * @param start
	 *            - starting page number
	 * @param size
	 *            - number of records to be retrieved
	 * @param username
	 *            - username of the enduser
	 * @param tenantID
	 *            - tenant ID of the enduser belongs to username
	 * @return array of properties
	 */
	@GET
	@Produces("application/json")
	public Response getPropertiesOnAResource(@QueryParam("path") String resourcePath,
	                                         @QueryParam("start") int start,
	                                         @QueryParam("size") int size,
	                                         @QueryParam("user") String username) {
		path = resourcePath;
		if (username == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			String tenantID = super.getTenantID();
			super.createUserRegistry(username, tenantID);
		}
		if (RestPathPaginationValidation.validate(resourcePath, start, size) == -1) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if (super.getUserRegistry() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {
			exist = super.getUserRegistry().resourceExists(resourcePath);
			if (exist) {
				// response = displayPaginatedResult(start, size);
				return displayPaginatedResult(start, size);
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("User doesn't allow to access the resource", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@Override
	protected PropertyModel[] getResult() {
		// Resource resource;
		List<PropertyModel> list = new ArrayList<PropertyModel>();
		try {
			// resource = super.getUserRegistry().get(path);
			java.util.Properties prop = super.getUserRegistry().get(path).getProperties();
			// PropertyModel propModel = new PropertyModel();
			if (prop != null) {
				Enumeration<Object> propName = prop.keys();
				while (propName.hasMoreElements()) {
					PropertyModel propModel = new PropertyModel();
					String property = propName.nextElement().toString();
					String propValue = prop.get(property).toString();
					propValue =
					            propValue.substring(propValue.indexOf('[') + 1,
					                                propValue.indexOf(']'));
					String[] propString = propValue.split(",");
					propModel.setName(property);
					propModel.setValue(propString);
					list.add(propModel);
				}
			}
		} catch (RegistryException e) {
			log.error(e.getMessage(), e);
		}
		return list.toArray(new PropertyModel[list.size()]);
	}

	@Override
	protected Response display(PropertyModel[] e, int begin, int end) {
		PropertyModel[] model = new PropertyModel[end - begin + 1];
		for (int i = begin, j = 0; i <= end && j < model.length; i++, j++) {
			model[j] = e[i];
		}
		return Response.ok(model).build();
	}
}
