package com.bidplaza;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.model.item.Art;
import com.bidplaza.model.item.Electronics;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.item.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho ItemFactory.
 */
class ItemFactoryTest {

    private Item createItem(String type) {
        return ItemFactory.create(
            type, "Test", "Desc", 500.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "seller-001"
        );
    }

    @Test
    void create_electronics_shouldReturnElectronics() {
        Item item = createItem("electronics");
        assertInstanceOf(Electronics.class, item);
        assertEquals("ELECTRONICS", item.getCategory());
    }

    @Test
    void create_art_shouldReturnArt() {
        Item item = createItem("art");
        assertInstanceOf(Art.class, item);
        assertEquals("ART", item.getCategory());
    }

    @Test
    void create_vehicle_shouldReturnVehicle() {
        Item item = createItem("vehicle");
        assertInstanceOf(Vehicle.class, item);
        assertEquals("VEHICLE", item.getCategory());
    }

    @Test
    void create_invalidType_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            createItem("xyz"); // loại không tồn tại
        });
    }

    @Test
    void create_startingPriceShouldEqualCurrentPrice() {
        Item item = createItem("electronics");
        assertEquals(item.getStartingPrice(), item.getCurrentPrice());
    }
}
