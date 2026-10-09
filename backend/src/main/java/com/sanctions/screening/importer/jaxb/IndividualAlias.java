package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "INDIVIDUAL_ALIAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualAlias {

    @XmlElement(name = "QUALITY")
    private String quality;

    @XmlElement(name = "ALIAS_NAME")
    private String aliasName;

    @XmlElement(name = "NAME_ORIGIN_DESC")
    private String nameOriginDesc;
}
