package org.tek.microlab.dms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DmsFilePropertiesMetadataTypeDto {
	@JsonProperty("type")
	private String type;
	
}
