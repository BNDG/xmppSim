package com.hjq.demo.chat.entity.area;

import java.util.List;

/**
 * 省
 *
 * @author zhou
 */
public class Province {
    private String name;
    private List<City> city;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCity() {
        return city;
    }

    public void setCity(List<City> city) {
        this.city = city;
    }
}
