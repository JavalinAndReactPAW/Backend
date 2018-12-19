package domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Card")
public class DomainCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String value;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<DomainComment> comments;
}
