package app.rescue.backend.model;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "animal_characteristics")
public class AnimalCharacteristics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "animal_type")
    private String animalType;

    @Column(name = "breed")
    private String breed;

    //@ElementCollection
    //@Column(name = "color")
    //@CollectionTable(name = "animal_characteristics_color", joinColumns = @JoinColumn(name = "owner_id"))
    //private Set<String> color = new LinkedHashSet<>();


    @Column(name = "gender")
    private String gender;

    @Column(name = "size")
    private String size;

    @Column(name = "color")
    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    //public Set<String> getColor() {
    //    return color;
    //}

    //public void setColor(Set<String> color) {
    //    this.color = color;
    //}

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}