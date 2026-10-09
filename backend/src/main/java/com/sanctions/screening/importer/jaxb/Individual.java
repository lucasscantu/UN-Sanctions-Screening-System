package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "INDIVIDUAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Individual {

    @XmlElement(name = "PERSON_ID")
    private PersonId personId;

    @XmlElement(name = "INDIVIDUAL_ALIAS")
    private List<IndividualAlias> individualAlias;

    @XmlElement(name = "INDIVIDUAL_ADDRESS")
    private List<IndividualAddress> individualAddress;

    @XmlElement(name = "INDIVIDUAL_DATE_OF_BIRTH")
    private List<IndividualDateOfBirth> individualDateOfBirth;

    @XmlElement(name = "INDIVIDUAL_PLACE_OF_BIRTH")
    private List<IndividualPlaceOfBirth> individualPlaceOfBirth;

    @XmlElement(name = "INDIVIDUAL_NATIONALITY")
    private List<IndividualNationality> individualNationality;

    @XmlElement(name = "SORT_KEY")
    private String sortKey;

    @XmlElement(name = "SUBMITTED_NAME")
    private String submittedName;

    @XmlElement(name = "REFERENCE_NUMBER")
    private String referenceNumber;

    @XmlElement(name = "LISTED_ON")
    private String listedOn;

    @XmlElement(name = "COMMITTEE")
    private String committee;

    @XmlElement(name = "DESIGNATION")
    private String designation;

    @XmlElement(name = "OTHER_INFORMATION")
    private String otherInformation;
}
