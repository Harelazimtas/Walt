package com.walt.controller;


import com.walt.WaltService;
import com.walt.dao.CityRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {

    @Autowired
    private WaltService waltService;

    @Resource
    private CityRepository cityRepository;

    @PostMapping(value="/createDelivery")
    public Delivery createOrderAndAssignDriver(@RequestBody Customer customer,@RequestBody Date deliveryTime,@RequestBody Restaurant restaurant){
        Delivery delivery=waltService.createOrderAndAssignDriver(customer, restaurant, new Date());
        if(delivery != null)
            return delivery;

        throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,"No available drivers.");
    }

    @GetMapping(value = "/getDriverRankReport")
    public Map<String,Long>  getDriverRankReport(){
        List<DriverDistance> driver=waltService.getDriverRankReport();
        Map<String,Long> nameDriverToDistance=new HashMap<>();
        for (DriverDistance d:driver) {
            nameDriverToDistance.put(d.getDriver().getName(),d.getTotalDistance());
        }
        return nameDriverToDistance;
    }

    @GetMapping(value = "/getDriverRankReportByCity/{cityName}")
    public Map<String,Long> getDriverRankReportByCity(@PathVariable String cityName){
        City city=cityRepository.findByName(cityName);
        if(city==null){
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST,"The city don't exist");
        }
        List<DriverDistance> driver=waltService.getDriverRankReportByCity(city);
        Map<String,Long> nameDriverToDistance=new HashMap<>();
        if(driver == null){
            return nameDriverToDistance;
        }
        for (DriverDistance d:driver) {
            nameDriverToDistance.put(d.getDriver().getName(),d.getTotalDistance());
        }
        return nameDriverToDistance;

    }
}
