package com.cdac.tokendemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CRSelectAppReqDto {
    @JsonProperty("cardtype")
    int cardType;
    @JsonProperty("handle")
    int handle;
}

