package de.billory.backend.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String street;
    
    @Column(nullable = false)
    private String zip;

    @Column(nullable = false)
    private String city;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String notes;

    @Column(name = "created_at", nullable = false)
    private String created_at;

    @Column(name = "updated_at", nullable = false)
    private String updated_at;

    public Customer(){

    }

    public Integer getId(){
        return this.id;
    }
    
    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getStreet(){
        return this.street;
    }

    public void setStreet(String street){
        this.street = street;
    }

    public String getZip(){
        return this.zip;
    }

    public void setZip(String zip){
        this.zip = zip;
    }

    public String getCity(){
        return this.city;
    }

    public void setCity(String city){
        this.city = city;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPhone(){
        return this.phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getNotes(){
        return this.notes;
    }

    public void setNotes(String notes){
        this.notes = notes;
    }

    public String getCreatedAt(){
        return this.created_at;
    }

    public void setCreatedAt(String created_at){
        this.created_at = created_at;
    }

    public String getUpdatedAt(){
        return this.updated_at;
    }

    public void setUpdatedAt(String updated_at){
        this.updated_at = updated_at;
    }
}
