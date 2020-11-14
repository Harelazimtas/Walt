package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class WaltServiceImpl implements WaltService {

    @Resource
    private DriverRepository driverRepository;

    @Resource
    private DeliveryRepository deliveryRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant,Date deliveryTime)  {
        Delivery newDelivery;

        if(!validateCustomerAndRestaurantAndDate(customer,restaurant,deliveryTime)) {
            return null;
        }

        List<Driver> drivers=driverRepository.findAllDriversByCity(customer.getCity());

        if(drivers.size()==1){
            if(checkOneDriverIsOk(drivers.get(0),deliveryTime)){
                newDelivery=new Delivery(drivers.get(0),restaurant,customer,deliveryTime);
                deliveryRepository.save(newDelivery);
                WaltApplication.getLog().info("The driver get delivery");
                return newDelivery;
            }
            else{
                WaltApplication.getLog().error("The driver not available");
                return null;
            }
        }
        // there is 0 driver in this city
        else if(drivers.size()==0){
            WaltApplication.getLog().error("There is 0 driver in this city");
            return null;
        }
        //chose the less busy driver
        else{
            return this.checkMoreOneDriver(drivers,customer,restaurant,deliveryTime);
        }

    }

    private boolean checkOneDriverIsOk(Driver driver, Date deliveryTime) {
        Driver driverExist;
        driverExist=deliveryRepository.findDriverByDriver(driver);
        if(driverExist!=null){
            driverExist=deliveryRepository.findDriverByDriverAndDeliveryTime(driver,deliveryTime);
        }
        else{
            return true;
        }
        if(driverExist==null){
            return false;
        }
        return true;
    }

    private boolean validateCustomerAndRestaurantAndDate(Customer customer,Restaurant restaurant,Date date) {
        //check if the customer exist
        if(customer== null){
            WaltApplication.getLog().info("The customer don't exist");
            return false;
        }
        // check the city of customer equal to city of restaurant
        if(customer.getCity().getId()!= restaurant.getCity().getId()){
            WaltApplication.getLog().info("The city of customer is different from restaurant");
            return false;
        }
        if(date ==null){
            WaltApplication.getLog().info("Error in date");
            return false;
        }
        return true;
    }

    private Delivery checkMoreOneDriver(List<Driver> drivers, Customer customer, Restaurant restaurant, Date deliveryTime){
        Driver availableDriver=null;
        List<Driver> availableDrivers=new ArrayList<>();
        for (Driver driver: drivers) {
            availableDriver=deliveryRepository.findDriverByDriver(driver);
            if(availableDriver ==null){
                availableDrivers.add(driver);
            }
            else {
                availableDriver = deliveryRepository.findDriverByDriverAndDeliveryTime(driver, deliveryTime);
                if(availableDriver !=null)
                    availableDrivers.add(availableDriver);
            }
        }
        Driver driverLessBusy= getLessDriverBusy(availableDrivers);
        if (driverLessBusy==null){
            WaltApplication.getLog().error("No available driver all busy");
            return null;
        }
        else{
            Delivery newDelivery=new Delivery(driverLessBusy,restaurant,customer,deliveryTime);
            deliveryRepository.save(newDelivery);
            WaltApplication.getLog().info("The driver get delivery");
            return newDelivery;
        }
    }

    private Driver getLessDriverBusy(List<Driver> availableDrivers) {
        Collections.sort(availableDrivers,Comparator.comparingDouble(this::getTotalDistanceOfDriver));
        if(availableDrivers.size()>0)
            return availableDrivers.get(0);
        else
            return null;
    }

    private double getTotalDistanceOfDriver(Driver driver){
        return deliveryRepository.findAllDeliveryByDriver(driver).stream().mapToDouble(Delivery::getDistance).sum();
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findAllDistancesByDriver();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return deliveryRepository.findAllCityDistancesByDriver(city);
    }
}
