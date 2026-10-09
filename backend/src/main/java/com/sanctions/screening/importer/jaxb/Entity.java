package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ENTITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entity {

    @XmlElement(name = "ENTITY_ID")
    private EntityId entityId;

    @XmlElement(name = "ENTITY_ALIAS")
    private List<EntityAlias> entityAlias;

    @XmlElement(name = "ENTITY_ADDRESS")
    private List<EntityAddress> entityAddress;

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

    @XmlElement(name = "OTHER_INFORMATION")
    private String otherInformation;
}
