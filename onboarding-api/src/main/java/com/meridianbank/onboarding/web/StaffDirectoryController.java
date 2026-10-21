package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.service.AccessControlService;
import com.meridianbank.onboarding.service.StaffDirectoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Branch directory searches used by the operations console to prefill staff onboarding from the
 * corporate directory.
 */
@RestController
@RequestMapping("/api/directory")
public class StaffDirectoryController {

    private final UserContextService userContextService;
    private final AccessControlService accessControlService;
    private final StaffDirectoryService staffDirectoryService;

    public StaffDirectoryController(UserContextService userContextService,
                                    AccessControlService accessControlService,
                                    StaffDirectoryService staffDirectoryService) {
        this.userContextService = userContextService;
        this.accessControlService = accessControlService;
        this.staffDirectoryService = staffDirectoryService;
    }

    /** Lists the directory entries in a department submitted from the branch console form. */
    @PostMapping("/staff/search")
    public Map<String, Object> searchStaffByDepartment(@RequestParam String department) {
        accessControlService.requireAdmin(userContextService.currentUser());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("entries", staffDirectoryService.searchDepartment(department));
        return body;
    }
}
