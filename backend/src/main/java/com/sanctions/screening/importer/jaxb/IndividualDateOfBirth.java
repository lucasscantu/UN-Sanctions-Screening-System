package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "INDIVIDUAL_DATE_OF_BIRTH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualDateOfBirth {

    @XmlElement(name = "DATE")
    private String date;

    @XmlElement(name = "PRECISION")
    private String precision;
}
