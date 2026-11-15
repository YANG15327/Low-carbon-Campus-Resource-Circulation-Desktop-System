package com.campus.recycle;

import com.campus.recycle.model.Category;
import com.campus.recycle.model.Item;
import com.campus.recycle.model.ItemSearchCriteria;
import com.campus.recycle.model.ItemStatus;
import com.campus.recycle.model.User;
import com.campus.recycle.ui.AppServices;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppServicesIntegrationTest extends TestDatabaseSupport {

    @Test
    void registersLogsInPublishesSearchesAndRemovesItemAcrossServices() {
        AppServices services = AppServices.initialize();
        User seller = services.getUserService().register(
                "seller01", "secret123", "校园卖家", "seller@example.com");
        User loggedIn = services.getUserService().login("seller01", "secret123");
        Category category = services.getCategoryService().findAll().get(0);

        Item published = services.getItemService().publish(seller.getId(), category.getId(),
                "二手教材", "九成新", 12.5, List.of("教材", "低碳"));
        List<Item> found = services.getItemService().search(ItemSearchCriteria.builder()
                .keyword("低碳")
                .status(ItemStatus.AVAILABLE)
                .sellerId(loggedIn.getId())
                .build());

        assertEquals(1, found.size());
        assertEquals(published.getId(), found.get(0).getId());

        services.getItemService().remove(published.getId(), loggedIn.getId());
        assertTrue(services.getItemService().search(ItemSearchCriteria.builder()
                .status(ItemStatus.AVAILABLE)
                .sellerId(loggedIn.getId())
                .build()).isEmpty());
        assertEquals(ItemStatus.REMOVED,
                services.getItemService().findById(published.getId()).orElseThrow().getStatus());
    }
}
