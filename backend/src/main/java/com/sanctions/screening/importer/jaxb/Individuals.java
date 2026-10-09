package com.sanctions.screening.importer.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Individuals {

    @XmlElement(name = "INDIVIDUAL")
    private List<Individual> individuals;
}
