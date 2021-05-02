package org.hit.android.haim.calc.server.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.android.haim.calc.action.ActionType;

/**
 * @author Haim Adrian
 * @since 13-Apr-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    /**
     * What action to execute
     */
    private ActionType actionType;

    /**
     * The value to execute an action on
     */
    @JsonProperty(defaultValue = "0")
    private Double value;

    /**
     * For operators that take two arguments, this is the right argument
     */
    @JsonProperty(defaultValue = "0")
    private Double lastValue;

    @JsonProperty
    private String dynamicValue;

    @JsonIgnore
    private boolean isHttpRequest;
}

