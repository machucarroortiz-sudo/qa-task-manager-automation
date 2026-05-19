package com.qataskmanager.automation_sut.config;

import com.qataskmanager.automation_sut.repository.UserRepository;
import com.qataskmanager.automation_sut.service.TestDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final TestDataService testDataService;

    public DataSeeder(UserRepository userRepository, TestDataService testDataService) {
        this.userRepository = userRepository;
        this.testDataService = testDataService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            testDataService.reset();
        }
    }
}
