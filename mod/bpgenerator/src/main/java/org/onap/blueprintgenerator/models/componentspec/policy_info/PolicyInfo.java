package org.onap.blueprintgenerator.models.componentspec.policy_info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class PolicyInfo {

    @JsonProperty("policy")
    private List<TypePolicy> typePolicyList;
}
