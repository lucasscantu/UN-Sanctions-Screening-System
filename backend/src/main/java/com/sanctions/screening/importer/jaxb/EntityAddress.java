package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ENTITY_ADDRESS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityAddress {

    @XmlElement(name = "STREET")
    private String street;

    @XmlElement(name = "CITY")
    private String city;

    @XmlElement(name = "STATE_OR_PROVINCE")
    private String stateOrProvince;

    @XmlElement(name = "POSTAL_CODE")
    private String postalCode;

    @XmlElement(name = "COUNTRY")
    private String country;

    @XmlElement(name = "ADDRESS_TYPE")
    private String addressType;
}
