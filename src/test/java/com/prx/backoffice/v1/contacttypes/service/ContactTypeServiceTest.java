package com.prx.backoffice.v1.contacttypes.service;

import com.prx.backoffice.v1.contacttypes.to.ContactTypeRequest;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ContactTypeServiceTest {

    private final ContactTypeService contactTypeService = new ContactTypeService() {
    };

    /**
     * Method under test: {@link ContactTypeService#listById(List)}
     */
    @Test
    void testListById() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.listById(new ArrayList<>()));
    }

    /**
     * Method under test: {@link ContactTypeService#listById(List)}
     */
    @Test
    void testListById2() {
        ArrayList<String> contactTypeIds = new ArrayList<>();
        contactTypeIds.add("aa7ca292-eede-4319-b20b-5db61a56efa8");
        assertThrows(NotImplementedException.class, () -> contactTypeService.listById(contactTypeIds));
    }

    /**
     * Method under test: {@link ContactTypeService#listById(List)}
     */
    @Test
    void testListById3() {
        ArrayList<String> contactTypeIds = new ArrayList<>();
        contactTypeIds.add("d65f84d0-4845-46fb-a541-e46ef3dd30e0");
        contactTypeIds.add("c26f681f-f25c-4dde-b57c-22bd0b296877");
        assertThrows(NotImplementedException.class, () -> contactTypeService.listById(contactTypeIds));
    }

    /**
     * Method under test: {@link ContactTypeService#update(String)}
     */
    @Test
    void testUpdate() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.update("d425105c-9539-4790-b281-32ff22407888"));
    }

    /**
     * Method under test: {@link ContactTypeService#create(ContactTypeRequest)}
     */
    @Test
    void testCreate() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.create(new ContactTypeRequest()));
    }

    /**
     * Method under test: {@link ContactTypeService#list()}
     */
    @Test
    void testList() {
        assertThrows(NotImplementedException.class, contactTypeService::list);
    }

    /**
     * Method under test: {@link ContactTypeService#list()}
     */
    @Test
    void testDelete() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.delete("c241fa1f-1770-4e6a-ab50-99e86562fd4a"));
    }

    /**
     * Method under test: {@link ContactTypeService#list()}
     */
    @Test
    void testFindById() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.findById("c241fa1f-1770-4e6a-ab50-99e86562fd4a"));
    }
}

