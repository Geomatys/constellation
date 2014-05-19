/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.services.web.controller.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;
import org.opengis.metadata.Identifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/process")
public class AdminProcessController {

	public static class RegistryDTO {
		private String name;

		private List<ProcessDTO> processes;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<ProcessDTO> getProcesses() {
			return processes;
		}

		public void setProcesses(List<ProcessDTO> processes) {
			this.processes = processes;
		}
	}

	public static class ProcessDTO {

		private String id;

		private String description;

		public String getId() {
			return id;
		}

		public void setId(String name) {
			this.id = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<RegistryDTO> all() {

		List<RegistryDTO> registries = new ArrayList<AdminProcessController.RegistryDTO>();

		for (Iterator<ProcessingRegistry> it = ProcessFinder
				.getProcessFactories(); it.hasNext();) {
			ProcessingRegistry processingRegistry = it.next();
			RegistryDTO registry = new RegistryDTO();
			Iterator<? extends Identifier> iterator = processingRegistry
					.getIdentification().getCitation().getIdentifiers()
					.iterator();

			registry.setName(iterator.next().getCode());
			

			List<ProcessDTO> processes = new ArrayList<AdminProcessController.ProcessDTO>();
			registry.setProcesses(processes);

			for (ProcessDescriptor processDescriptor : processingRegistry
					.getDescriptors()) {
				ProcessDTO dto = new ProcessDTO();
				dto.setId(processDescriptor.getIdentifier().getCode()
						.toString());
				if (processDescriptor.getProcedureDescription() != null)
					dto.setDescription(processDescriptor
							.getProcedureDescription().toString());
				processes.add(dto);
			}
			registries.add(registry);

		}

		return registries;

	}

}
