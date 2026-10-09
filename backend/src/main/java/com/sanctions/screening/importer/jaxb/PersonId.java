package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PERSON_ID")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonId {

    @XmlElement(name = "UNIQUE_ID")
    private String uniqueId;

    @XmlElement(name = "INDIVIDUAL_TITLE")
    private String individualTitle;

    @XmlElement(name = "FIRST_NAME")
    private String firstName;

    @XmlElement(name = "SECOND_NAME")
    private String secondName;

    @XmlElement(name = "THIRD_NAME")
    private String thirdName;

    @XmlElement(name = "FOURTH_NAME")
    private String fourthName;

    @XmlElement(name = "NAME_ORIGIN_DESC")
    private String nameOriginDesc;
}
