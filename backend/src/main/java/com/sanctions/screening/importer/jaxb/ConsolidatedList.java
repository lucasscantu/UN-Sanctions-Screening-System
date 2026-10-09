package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@XmlRootElement(name = "CONSOLIDATED_LIST")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedList {

    @XmlElement(name = "INDIVIDUALS")
    private Individuals individuals;

    @XmlElement(name = "ENTITIES")
    private Entities entities;
}
