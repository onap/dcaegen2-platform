package org.onap.blueprintgenerator.model.componentspec.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class configVolume {

    private String name;

}
