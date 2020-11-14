package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

    List<Delivery> findAllDeliveryByDriver(Driver driver);

    @Query("select d.driver as driver from Delivery d where "+"d.driver=:chosenDriver And not(d.deliveryTime  =:chosenDate) group by d.driver")
    Driver findDriverByDriverAndDeliveryTime(@Param("chosenDriver") Driver driver,@Param("chosenDate") Date deliveryTime);


    @Query("select delivery.driver as driver, sum(delivery.distance) as totalDistance from Delivery delivery group by delivery.driver order by totalDistance Desc")
    List<DriverDistance> findAllDistancesByDriver();


    @Query("SELECT d.driver AS driver, SUM(d.distance) AS totalDistance FROM Delivery d WHERE " +
            "d.driver.city =:chosenCity GROUP BY d.driver ORDER BY totalDistance DESC")
    List<DriverDistance> findAllCityDistancesByDriver(@Param("chosenCity") City city);

   @Query("select distinct(d.driver) as driver from Delivery d where "+"d.driver=:chosenDriver")
    Driver findDriverByDriver(@Param("chosenDriver") Driver driver);
}


