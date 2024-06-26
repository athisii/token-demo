package com.cdac.tokendemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author root
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRReadDataResDto {
    @JsonProperty("retval")
    int retVal;
    @JsonProperty("response")
    String response;

    @JsonProperty("responseLen")
    int responseLen;

}
