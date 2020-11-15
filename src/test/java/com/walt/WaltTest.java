package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import static org.junit.Assert.*;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;


    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa,bash);

        createRestaurant(bash, tlv,jerusalem);
    }

    private void createRestaurant(City bash, City tlv,City jerusalem) {
        Restaurant meat = new Restaurant("meat", bash, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", jerusalem, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa,City bash) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer bach = new Customer("Bach", bash, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver daniel = new Driver("Daniel", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver james = new Driver("James", bash);


        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, daniel, noa, ofri));
    }

    @Test
    public void testThreeDriverInOneCity(){
        //check if there is driver in this city
        assertNotNull(driverRepository.findAllDriversByCity(cityRepository.findByName("Tel-Aviv")));
        //test with more delivery from driver in same city(tlv-Tel Aviv)
        Date date1=new Date(2020, 11, 10, 18, 30);
        Date date2=new Date(2020, 11, 10, 19, 20);
        //Three driver in one city with four delivery in same time so the last must be null
        assertNotNull(waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"),restaurantRepository.findByName("vegan") ,date1));
        assertNotNull(waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"),restaurantRepository.findByName("vegan") ,date1));
        assertNotNull(waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"),restaurantRepository.findByName("vegan") ,date1));
        assertEquals(null,waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"),restaurantRepository.findByName("vegan") ,date1));
        //after one hour the driver can be available to get the delivery
        assertNotNull(waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"),restaurantRepository.findByName("vegan") ,date2));

    }

    @Test
    public void testOneDriverInCity() {
        //create two delivery in same city("bach"-Beer-Sheva) also in same time, and there is one driver in this city
        Date date=new Date(2020, 11, 10, 18, 20);
        //get delivery
        assertNotNull(waltService.createOrderAndAssignDriver(customerRepository.findByName("Bach"),restaurantRepository.findByName("meat") ,date));
        // not available to delivery(same time)
        assertEquals(null,waltService.createOrderAndAssignDriver(customerRepository.findByName("Bach"),restaurantRepository.findByName("meat") ,date));

    }
    @Test
    public void testNullCustomer(){
        Date date=new Date(2020, 11, 10, 18, 20);
        assertEquals(null,waltService.createOrderAndAssignDriver(null,restaurantRepository.findByName("meat") ,date));

    }

    @Test
    public void testDeliveryWithoutDriverInCity(){
        Date date=new Date(2020, 11, 10, 18, 20);
        assertEquals(null,waltService.createOrderAndAssignDriver(customerRepository.findByName("Mozart"),restaurantRepository.findByName("restaurant") ,date));
    }


    @Test
    public void testDescendingOrder(){
        List<DriverDistance> driverList=waltService.getDriverRankReport();
        double maxTotal=-1;
        for (DriverDistance d:driverList) {
            //init
            if(maxTotal ==-1){
                maxTotal=d.getTotalDistance();
            }
            assertTrue(d.getTotalDistance() <= maxTotal);
        }
    }



}
