package cloud.controller;

import cloud.component.EntityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class EntityConfigController {

    @Autowired
    private EntityConfig entityConfig;

    @GetMapping("/show")
    public String show(){
        return entityConfig.toString();
    }
}
