package org.onap.blueprintgenerator.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetAttribute {

    @JsonProperty("get_attribute")
    private Object attribute;
}
