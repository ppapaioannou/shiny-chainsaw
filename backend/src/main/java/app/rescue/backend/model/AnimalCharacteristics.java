package app.rescue.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "animal_characteristics")
public class AnimalCharacteristics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "animal_type")
    private String animalType;

    @Column(name = "breed")
    private String breed;

    @Column(name = "gender")
    private String gender;

    @Column(name = "size")
    private String size;

    @Column(name = "color")
    private String color;

    @Column(name = "age")
    private String age;

    @Column(name = "microchip_number")
    private String microchipNumber;

    @Column(name = "neutered")
    private Boolean neutered;

    @Column(name = "good_with_animals")
    private Boolean goodWithAnimals;

    @Column(name = "good_with_children")
    private Boolean goodWithChildren;

    @Column(name = "actions_taken")
    private String actionsTaken;

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public Boolean getGoodWithChildren() {
        return goodWithChildren;
    }

    public void setGoodWithChildren(Boolean goodWithChildren) {
        this.goodWithChildren = goodWithChildren;
    }

    public Boolean getGoodWithAnimals() {
        return goodWithAnimals;
    }

    public void setGoodWithAnimals(Boolean goodWithAnimals) {
        this.goodWithAnimals = goodWithAnimals;
    }

    public Boolean getNeutered() {
        return neutered;
    }

    public void setNeutered(Boolean neutered) {
        this.neutered = neutered;
    }

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

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

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}