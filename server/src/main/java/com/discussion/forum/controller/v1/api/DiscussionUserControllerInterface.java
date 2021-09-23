package com.discussion.forum.controller.v1.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/1.0")
public interface DiscussionUserControllerInterface {
    @GetMapping({"/discussions/{id:[0-9]+}", "/users/{username}/discussions/{id:[0-9]+}"})
    ResponseEntity<?> getDiscussionsRelative(@PathVariable long id,
                                             @PathVariable(required= false) String username,
                                             Pageable pageable,
                                             @RequestParam(name = "direction", defaultValue = "after") String direction,
                                             @RequestParam(name = "count", defaultValue = "false", required = false) boolean count
    );
}
