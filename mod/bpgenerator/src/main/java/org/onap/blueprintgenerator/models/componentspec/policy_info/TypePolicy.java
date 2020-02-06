package org.onap.blueprintgenerator.models.componentspec.policy_info;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class TypePolicy {

    private String node_label;
    private String policy_id;
    private String policy_model_id;

}
