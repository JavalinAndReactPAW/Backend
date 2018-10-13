package domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Table")
@Table
public class DomainTable {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;
    
    @OneToMany
    private List<DomainList> lists;

}
