package com.example.habbitamobile_nativo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String imageUrl;
    private String title;
    private double price;
    private int bedrooms;
    private int bathrooms;
    private int garages;
    private String address;
    private String description;
    private String type;
    private String transactionType;
    private double area;
    private List<String> photos = new ArrayList<>();
    private String contactEmail;
    private String contactPhone;
    private String owner;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public int getGarages() { return garages; }
    public void setGarages(int garages) { this.garages = garages; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos != null ? photos : new ArrayList<>(); }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}
