package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ENTITY_ID")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityId {

    @XmlElement(name = "UNIQUE_ID")
    private String uniqueId;

    @XmlElement(name = "ENTITY_NAME")
    private String entityName;

    @XmlElement(name = "NAME_ORIGIN_DESC")
    private String nameOriginDesc;
}
