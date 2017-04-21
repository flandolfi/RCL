// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.Serializable;

// Product class
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String seller;
    private String manufacturer;
    private String model;
    private long price;

    // Constructor
    public Product(String seller, String manufacturer, String model, long price) {
        this.seller = seller;
        this.manufacturer = manufacturer;
        this.model = model;
        this.price = price;
    }

    // Getters
    public String getSeller() {
        return seller;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public long getPrice() {
        return price;
    }

    public String toString() {
        return "[" + seller + "] " + manufacturer + " " + model + ", " + price + "€";
    }
}
