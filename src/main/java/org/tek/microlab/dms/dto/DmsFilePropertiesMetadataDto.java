package org.tek.microlab.dms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DmsFilePropertiesMetadataDto {
	@JsonProperty("__metadata")
	private DmsFilePropertiesMetadataTypeDto metadataType;
	@JsonProperty("FieldName")
	private String fieldName;
	@JsonProperty("FieldValue")
	private String fieldValue;
}
