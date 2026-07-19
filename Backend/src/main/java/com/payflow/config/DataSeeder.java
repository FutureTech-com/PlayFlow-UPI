package com.payflow.config;

import com.payflow.dto.bank.BankAccountResponse;
import com.payflow.dto.bank.LinkBankAccountRequest;
import com.payflow.dto.upi.CreateUpiRequest;
import com.payflow.dto.user.SetTransactionPinRequest;
import com.payflow.entity.Biller;
import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.repository.BillerRepository;
import com.payflow.repository.UserRepository;
import com.payflow.service.BankAccountService;
import com.payflow.service.UpiService;
import com.payflow.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seed(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            BankAccountService bankAccountService,
            UpiService upiService,
            UserService userService,
            BillerRepository billerRepository
    ) {
        return args -> {

            seedBillers(billerRepository);

            if (userRepository.count() > 0) {
                return;
            }

            User admin = new User();
            admin.setFullName("PayFlow Admin");
            admin.setEmail("admin@payflow.demo");
            admin.setPhone("9000000000");
            admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
            userRepository.save(admin);

            createDemoUser(
                    userRepository,
                    passwordEncoder,
                    bankAccountService,
                    upiService,
                    userService,
                    "Amit Verma",
                    "verma@payflow.demo",
                    "9111111111",
                    "amit"
            );

            createDemoUser(
                    userRepository,
                    passwordEncoder,
                    bankAccountService,
                    upiService,
                    userService,
                    "Hemu",
                    "hemu@payflow.demo",
                    "9222222222",
                    "hemu"
            );

            log.info("==============================================================");
            log.info("PayFlow demo data seeded successfully.");
            log.info("");
            log.info("Admin:");
            log.info("  Email    : admin@payflow.demo");
            log.info("  Password : Admin@1234");
            log.info("");
            log.info("Demo User 1:");
            log.info("  Email    : verma@payflow.demo");
            log.info("  Password : Passw0rd!");
            log.info("  UPI ID   : amit@payflow");
            log.info("  PIN      : 1234");
            log.info("");
            log.info("Demo User 2:");
            log.info("  Email    : hemu@payflow.demo");
            log.info("  Password : Passw0rd!");
            log.info("  UPI ID   : hemu@payflow");
            log.info("  PIN      : 1234");
            log.info("");
            log.info("Swagger: http://localhost:8082/swagger-ui.html");
            log.info("==============================================================");
        };
    }

    @SuppressWarnings("null")
    private void seedBillers(BillerRepository billerRepository) {

        if (billerRepository.count() > 0) {
            return;
        }

        record DemoBiller(String name, Biller.BillerCategory category, String code) {}

        List<DemoBiller> billers = List.of(
                new DemoBiller("Airtel Prepaid", Biller.BillerCategory.MOBILE_PREPAID, "AIRTEL_PREPAID"),
                new DemoBiller("Airtel Postpaid", Biller.BillerCategory.MOBILE_POSTPAID, "AIRTEL_POSTPAID"),

                new DemoBiller("Jio Prepaid", Biller.BillerCategory.MOBILE_PREPAID, "JIO_PREPAID"),
                new DemoBiller("Jio Postpaid", Biller.BillerCategory.MOBILE_POSTPAID, "JIO_POSTPAID"),

                new DemoBiller("Vi Prepaid", Biller.BillerCategory.MOBILE_PREPAID, "VI_PREPAID"),
                new DemoBiller("Vi Postpaid", Biller.BillerCategory.MOBILE_POSTPAID, "VI_POSTPAID"),

                new DemoBiller("Tata Play DTH", Biller.BillerCategory.DTH, "TATA_PLAY_DTH"),
                new DemoBiller("Dish TV", Biller.BillerCategory.DTH, "DISH_TV"),

                new DemoBiller("State Electricity Board", Biller.BillerCategory.ELECTRICITY, "STATE_ELECTRICITY_BOARD"),
                new DemoBiller("Municipal Water Supply", Biller.BillerCategory.WATER, "MUNICIPAL_WATER_SUPPLY"),

                new DemoBiller("Indane Gas", Biller.BillerCategory.GAS, "INDANE_GAS"),

                new DemoBiller("ACT Fibernet", Biller.BillerCategory.BROADBAND, "ACT_FIBERNET"),
                new DemoBiller("Airtel Xstream Fiber", Biller.BillerCategory.BROADBAND, "AIRTEL_XSTREAM_FIBER"),

                new DemoBiller("National Highways FASTag", Biller.BillerCategory.FASTAG, "NHAI_FASTAG"),

                new DemoBiller("HDFC Credit Card", Biller.BillerCategory.CREDIT_CARD, "HDFC_CREDIT_CARD"),
                new DemoBiller("ICICI Credit Card", Biller.BillerCategory.CREDIT_CARD, "ICICI_CREDIT_CARD")
        );

        int seeded = 0;

        for (DemoBiller b : billers) {

            if (billerRepository.existsByCode(b.code())) {
                continue;
            }

            Biller biller = Biller.builder()
                    .name(b.name())
                    .category(b.category())      // <-- REQUIRED
                    .code(b.code())
                    .active(true)
                    .build();

            billerRepository.save(biller);
            seeded++;
        }

        log.info("Seeded {} demo billers.", seeded);
    }
    private void createDemoUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            BankAccountService bankAccountService,
            UpiService upiService,
            UserService userService,
            String fullName,
            String email,
            String phone,
            String preferredHandle
    ) {

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode("Passw0rd!"));
        user.setRoles(Set.of(Role.ROLE_USER));

        User savedUser = userRepository.save(user);

        LinkBankAccountRequest bankRequest = new LinkBankAccountRequest();
        bankRequest.setBankName("Demo Bank");
        bankRequest.setAccountHolderName(fullName);
        bankRequest.setAccountNumber("1234567890" + phone.substring(6));
        bankRequest.setIfscCode("DEMO0001234");
        bankRequest.setAccountType("SAVINGS");

        BankAccountResponse account =
                bankAccountService.linkAccount(savedUser, bankRequest);

        CreateUpiRequest upiRequest = new CreateUpiRequest();
        upiRequest.setBankAccountId(account.getId());
        upiRequest.setPreferredHandle(preferredHandle);

        upiService.createUpiId(savedUser, upiRequest);

        SetTransactionPinRequest pinRequest = new SetTransactionPinRequest();
        pinRequest.setPin("1234");

        userService.setTransactionPin(savedUser, pinRequest);
    }

}