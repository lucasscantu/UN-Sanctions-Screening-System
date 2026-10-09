package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "INDIVIDUAL_PLACE_OF_BIRTH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualPlaceOfBirth {

    @XmlElement(name = "CITY")
    private String city;

    @XmlElement(name = "COUNTRY")
    private String country;

    @XmlElement(name = "PLACE_OF_BIRTH")
    private String placeOfBirth;
}
