package app.rescue.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table()
public class AdoptionPost extends AnimalPost {
    @Column(name = "age")
    private String age;

    @Column(name = "neutered")
    private Boolean neutered;

    @Column(name = "microchip_number")
    private String adoptionMicrochipNumber;

    @Column(name = "good_with_children")
    private Boolean goodWithChildren;

    @Column(name = "good_with_animals")
    private Boolean goodWithAnimals;

    public Boolean getGoodWithAnimals() {
        return goodWithAnimals;
    }

    public void setGoodWithAnimals(Boolean goodWithAnimals) {
        this.goodWithAnimals = goodWithAnimals;
    }

    public Boolean getGoodWithChildren() {
        return goodWithChildren;
    }

    public void setGoodWithChildren(Boolean goodWithChildren) {
        this.goodWithChildren = goodWithChildren;
    }

    public String getAdoptionMicrochipNumber() {
        return adoptionMicrochipNumber;
    }

    public void setAdoptionMicrochipNumber(String microchipNumber) {
        this.adoptionMicrochipNumber = microchipNumber;
    }

    public Boolean getNeutered() {
        return neutered;
    }

    public void setNeutered(Boolean neutered) {
        this.neutered = neutered;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}