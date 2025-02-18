package com.example.Shares.seeder;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.repository.BankCardRepository;
import com.example.Shares.auth.repository.UserRepository;
import com.example.Shares.auth.utils.Roles;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.hub.repository.HubRepository;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.transactions.repository.TransactionsRepository;
import com.example.Shares.wallet.entity.WalletEntity;
import com.example.Shares.wallet.repository.WalletRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseSeeder implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private HubRepository hubRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Running database seeder...");
        seedUsers();
        seedHubTable();
        seedBankCardTable();
        seedWalletTable();
        seedTransactionsTable();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            // First user - fully set up account with hub and wallets
            UserEntity establishedUser = new UserEntity();
            establishedUser.setCivilId("299010100494");
            establishedUser.setPhoneNumber("+96550215090");
            establishedUser.setUsername("yousef");
            establishedUser.setFirstName("Yousef");
            establishedUser.setLastName("Almesaeed");
            establishedUser.setPictureUrl("299010100494.jpg");
            establishedUser.setPassword(new BCryptPasswordEncoder().encode("password"));
            establishedUser.setRole(Roles.User);
            userRepository.save(establishedUser);

            // Second user - same person, different civil ID for registration demo
            // Minimal details since this represents pre-registration state
            UserEntity newUser = new UserEntity();
            newUser.setCivilId("299110201259");
            newUser.setPhoneNumber("+96550215090");
            newUser.setFirstName("Abdulwahab");
            newUser.setLastName("Alawadhi");
            userRepository.save(newUser);

            logger.info("Users table seeded with two accounts for demo.");
        }
    }

    private void seedHubTable() {
        logger.info("Checking hub seeding...");

        if (hubRepository.count() > 0) {
            logger.info("Hub table already seeded. Skipping...");
            return;
        }

        // Only create hub for the established user
        Optional<UserEntity> userOptional = userRepository.findByCivilId("299010100494");
        if (!userOptional.isPresent()) {
            logger.warn("Established user not found. Skipping hub seeding.");
            return;
        }

        UserEntity user = userOptional.get();
        logger.info("User found for hub: " + user.getCivilId());

        HubEntity hub = new HubEntity();
        hub.setHubCardNumber("2221841523654789");
        hub.setUser(user);
        hubRepository.save(hub);

        logger.info("Hub table seeded successfully.");
    }

    private void seedBankCardTable() {
        logger.info("Checking bank card seeding...");

        if (bankCardRepository.count() > 0) {
            logger.info("Bank Cards table already seeded. Skipping...");
            return;
        }

        // Get both users
        Optional<UserEntity> establishedUserOptional = userRepository.findByCivilId("299010100494");
        Optional<UserEntity> newUserOptional = userRepository.findByCivilId("301020500392");

        if (!establishedUserOptional.isPresent() || !newUserOptional.isPresent()) {
            logger.warn("One or both users not found. Skipping bank card seeding.");
            return;
        }

        UserEntity establishedUser = establishedUserOptional.get();
        UserEntity newUser = newUserOptional.get();

        // Get hub for established user only
        Optional<HubEntity> hubOptional = hubRepository.findAll().stream()
                .filter(h -> h.getUser().getId().equals(establishedUser.getId()))
                .findFirst();

        if (!hubOptional.isPresent()) {
            logger.warn("No hub found for established user. Skipping bank card seeding.");
            return;
        }
        HubEntity hub = hubOptional.get();

        // FIRST USER: Established user's cards (linked to hub)
        BankCardEntity establishedBoubyanCard = new BankCardEntity();
        establishedBoubyanCard.setCardBalance(1200.0);
        establishedBoubyanCard.setCardNumber("4565841523654789");
        establishedBoubyanCard.setBankName("Boubyan Bank");
        establishedBoubyanCard.setCardType("Checking Account");
        establishedBoubyanCard.setCvv("123");
        establishedBoubyanCard.setAccountNumber("0044556677");
        establishedBoubyanCard.setExpiryDate("05/27");
        establishedBoubyanCard.setHub(hub);
        establishedBoubyanCard.setUser(establishedUser);
        bankCardRepository.save(establishedBoubyanCard);

        BankCardEntity establishedNomoCard = new BankCardEntity();
        establishedNomoCard.setCardBalance(850.0);
        establishedNomoCard.setCardNumber("4565841523654790");
        establishedNomoCard.setBankName("Nomo Bank");
        establishedNomoCard.setCardType("Digital Account");
        establishedNomoCard.setCvv("456");
        establishedNomoCard.setAccountNumber("0044556678");
        establishedNomoCard.setExpiryDate("08/27");
        establishedNomoCard.setUser(establishedUser);
        establishedNomoCard.setHub(hub);
        bankCardRepository.save(establishedNomoCard);

        // SECOND USER: New user's cards (completely separate, no hub)
        BankCardEntity newKFHCard = new BankCardEntity();
        newKFHCard.setCardBalance(2500.0);
        newKFHCard.setCardNumber("4532789156234567");
        newKFHCard.setBankName("Kuwait Finance House");
        newKFHCard.setCardType("Savings Account");
        newKFHCard.setCvv("789");
        newKFHCard.setAccountNumber("0077889900");
        newKFHCard.setExpiryDate("09/26");
        newKFHCard.setUser(newUser);
        // Explicitly not setting hub for second user
        bankCardRepository.save(newKFHCard);

        BankCardEntity newGulfBankCard = new BankCardEntity();
        newGulfBankCard.setCardBalance(1800.0);
        newGulfBankCard.setCardNumber("4532789156234568");
        newGulfBankCard.setBankName("Gulf Bank");
        newGulfBankCard.setCardType("Current Account");
        newGulfBankCard.setCvv("321");
        newGulfBankCard.setAccountNumber("0077889901");
        newGulfBankCard.setExpiryDate("11/26");
        newGulfBankCard.setUser(newUser);
        // Explicitly not setting hub for second user
        bankCardRepository.save(newGulfBankCard);

        logger.info("Bank Cards table seeded successfully with separate cards for each user.");
    }

    private void seedWalletTable() {
        logger.info("Checking wallet seeding...");

        if (walletRepository.count() > 0) {
            logger.info("Wallet table already seeded. Skipping...");
            return;
        }

        // Get the established user specifically (not just first user)
        Optional<UserEntity> userOptional = userRepository.findByCivilId("299010100494");
        if (!userOptional.isPresent()) {
            logger.warn("Established user not found. Skipping wallet seeding.");
            return;
        }

        UserEntity user = userOptional.get();
        if (user.getHub() == null) {
            logger.warn("User has no hub. Skipping wallet seeding.");
            return;
        }

        List<BankCardEntity> bankCards = bankCardRepository.findByUser(user);
        if (bankCards.isEmpty()) {
            logger.warn("No bank cards found for established user. Skipping wallet seeding.");
            return;
        }

        BankCardEntity salaryCard = bankCards.stream()
                .filter(card -> card.getBankName().equals("Boubyan Bank"))
                .findFirst()
                .orElse(bankCards.get(0));

        BankCardEntity nomoCard = bankCards.stream()
                .filter(card -> card.getBankName().equals("Nomo Bank"))
                .findFirst()
                .orElse(bankCards.get(bankCards.size() > 1 ? 1 : 0));

        // Bills & Essentials (from salary card)
        WalletEntity billsWallet = new WalletEntity();
        billsWallet.setLinkedCards(List.of(salaryCard));
        billsWallet.setAllocation(250.0);   // Monthly bills
        billsWallet.setBalance(85.0);       // Remaining after paying most bills
        billsWallet.setCategory("Bills");
        billsWallet.setName("Bills & Essentials");
        billsWallet.setSelected(true);
        billsWallet.setHub(user.getHub());
        billsWallet.setPatternId(1L);
        billsWallet.setColorId(1L);
        walletRepository.save(billsWallet);

        // Food & Daily (from salary card)
        WalletEntity foodWallet = new WalletEntity();
        foodWallet.setLinkedCards(List.of(salaryCard));
        foodWallet.setAllocation(250.0);   // Monthly food budget
        foodWallet.setBalance(80.0);       // Mid-month remaining
        foodWallet.setCategory("Food");
        foodWallet.setName("Food & Daily");
        foodWallet.setSelected(false);
        foodWallet.setHub(user.getHub());
        foodWallet.setPatternId(2L);
        foodWallet.setColorId(2L);
        walletRepository.save(foodWallet);

        // Travel & Vacation (using Nomo for online bookings)
        WalletEntity travelWallet = new WalletEntity();
        travelWallet.setLinkedCards(List.of(nomoCard));
        travelWallet.setAllocation(300.0);  // Monthly travel savings
        travelWallet.setBalance(180.0);     // Saved for next trip
        travelWallet.setCategory("Travel");
        travelWallet.setName("Travel & Vacation");
        travelWallet.setSelected(false);
        travelWallet.setHub(user.getHub());
        travelWallet.setPatternId(3L);
        travelWallet.setColorId(3L);
        walletRepository.save(travelWallet);

        // Gadgets & Gaming (using Nomo for online purchases)
        WalletEntity gadgetsWallet = new WalletEntity();
        gadgetsWallet.setLinkedCards(List.of(nomoCard));
        gadgetsWallet.setAllocation(200.0);  // Monthly tech budget
        gadgetsWallet.setBalance(75.0);      // Remaining after purchases
        gadgetsWallet.setCategory("Gadgets");
        gadgetsWallet.setName("Gadgets & Gaming");
        gadgetsWallet.setSelected(false);
        gadgetsWallet.setHub(user.getHub());
        gadgetsWallet.setPatternId(4L);
        gadgetsWallet.setColorId(4L);
        walletRepository.save(gadgetsWallet);

        // Entertainment (using both cards)
        WalletEntity entertainmentWallet = new WalletEntity();
        entertainmentWallet.setLinkedCards(List.of(salaryCard, nomoCard));
        entertainmentWallet.setAllocation(200.0);  // Monthly entertainment
        entertainmentWallet.setBalance(60.0);      // Left for month
        entertainmentWallet.setCategory("Entertainment");
        entertainmentWallet.setName("Entertainment");
        entertainmentWallet.setSelected(false);
        entertainmentWallet.setHub(user.getHub());
        entertainmentWallet.setPatternId(5L);
        entertainmentWallet.setColorId(5L);
        walletRepository.save(entertainmentWallet);

        logger.info("Wallet table seeded successfully with realistic spending data.");
    }

    private void seedTransactionsTable() {
        logger.info("Checking transactions seeding...");

        if (transactionsRepository.count() > 0) {
            logger.info("Transactions table already seeded. Skipping...");
            return;
        }

        List<WalletEntity> wallets = walletRepository.findAll();
        if (wallets.isEmpty()) {
            logger.warn("No wallets found. Skipping transactions seeding.");
            return;
        }

        WalletEntity billsWallet = wallets.get(0);
        seedBillsTransactions(billsWallet);

        WalletEntity foodWallet = wallets.get(1);
        seedFoodTransactions(foodWallet);

        WalletEntity travelWallet = wallets.get(2);
        seedTravelTransactions(travelWallet);

        WalletEntity gadgetsWallet = wallets.get(3);
        seedGadgetsTransactions(gadgetsWallet);

        WalletEntity entertainmentWallet = wallets.get(4);
        seedEntertainmentTransactions(entertainmentWallet);

        logger.info("Transactions table seeded successfully with realistic spending data.");
    }

    private void seedBillsTransactions(WalletEntity wallet) {
        createTransaction(wallet, 20.000, "Netflix", 29.3399, 47.9337, "2024-03-15");
        createTransaction(wallet, 65.000, "Ministry of Electricity", 29.3759, 47.9774, "2024-03-10");
        createTransaction(wallet, 35.000, "Zain Kuwait", 29.3015, 47.9282, "2024-03-05");
        createTransaction(wallet, 45.000, "Ooredoo Internet", 29.3399, 47.9337, "2024-02-28");
        createTransaction(wallet, 12.500, "BeIN Sports", 29.3759, 47.9774, "2024-02-25");
        createTransaction(wallet, 25.000, "Kuwait Municipality", 29.3483, 47.9371, "2024-02-20");
    }

    private void seedFoodTransactions(WalletEntity wallet) {
        createTransaction(wallet, 8.500, "Pick - The Avenues", 29.3759, 47.9774, "2024-03-18");
        createTransaction(wallet, 15.500, "Caribou - Arraya", 29.3759, 47.9774, "2024-03-16");
        createTransaction(wallet, 18.750, "Table Otto - JACC", 29.3780, 47.9903, "2024-03-14");
        createTransaction(wallet, 25.000, "Dar Hamad - Gulf Road", 29.3420, 48.2203, "2024-03-12");
        createTransaction(wallet, 12.750, "Shake Shack - Al Kout", 29.0965, 48.1301, "2024-03-08");
        createTransaction(wallet, 7.500, "Starbucks - Marina Mall", 29.3399, 47.9337, "2024-03-06");
        createTransaction(wallet, 32.000, "Salt - Kuwait City", 29.3759, 47.9774, "2024-03-02");
        createTransaction(wallet, 22.500, "Mais Alghanim", 29.3483, 47.9371, "2024-02-28");
    }

    private void seedTravelTransactions(WalletEntity wallet) {
        createTransaction(wallet, 85.000, "Kuwait Airways", 29.2268, 47.9689, "2024-03-20");
        createTransaction(wallet, 120.000, "Booking.com", 29.3759, 47.9774, "2024-03-19");
        createTransaction(wallet, 35.000, "Dubai Visa Center", 29.3759, 47.9774, "2024-03-15");
        createTransaction(wallet, 25.000, "AXA Insurance", 29.3759, 47.9774, "2024-03-10");
        createTransaction(wallet, 15.000, "Uber Dubai", 29.3759, 47.9774, "2024-02-25");
        createTransaction(wallet, 145.000, "Turkish Airlines", 29.2268, 47.9689, "2024-02-20");
        createTransaction(wallet, 65.000, "Hilton Dubai", 29.3759, 47.9774, "2024-02-15");
        createTransaction(wallet, 40.000, "Careem Kuwait", 29.3483, 47.9371, "2024-02-10");
    }

    private void seedGadgetsTransactions(WalletEntity wallet) {
        createTransaction(wallet, 65.000, "Xcite Electronics", 29.3759, 47.9774, "2024-03-17");
        createTransaction(wallet, 45.000, "Virgin - Avenues", 29.3759, 47.9774, "2024-03-15");
        createTransaction(wallet, 25.000, "PlayStation Store", 29.3759, 47.9774, "2024-03-10");
        createTransaction(wallet, 35.000, "Blink Kuwait", 29.3759, 47.9774, "2024-03-05");
        createTransaction(wallet, 15.000, "Apple Store", 29.3759, 47.9774, "2024-02-28");
        createTransaction(wallet, 299.000, "iPhone Case - Xcite", 29.3759, 47.9774, "2024-02-20");
        createTransaction(wallet, 89.000, "Gaming Chair - Blink", 29.3483, 47.9371, "2024-02-15");
        createTransaction(wallet, 49.000, "Apple AirPods - Virgin", 29.3759, 47.9774, "2024-02-10");
    }

    private void seedEntertainmentTransactions(WalletEntity wallet) {
        createTransaction(wallet, 75.000, "VOX Cinema - Avenues", 29.2694, 47.9783, "2024-03-19");
        createTransaction(wallet, 35.000, "Cinescape - The Gate", 29.3483, 47.9371, "2024-03-15");
        createTransaction(wallet, 25.000, "Nintendo Store", 29.3608, 47.9169, "2024-03-12");
        createTransaction(wallet, 35.000, "Grand Cafe - Kuwait", 29.3015, 47.9282, "2024-03-08");
        createTransaction(wallet, 120.000, "Kuwait Opera House", 29.3399, 47.9337, "2024-03-05");
        createTransaction(wallet, 45.000, "Bowling City - Kuwait", 29.3483, 47.9371, "2024-03-01");
        createTransaction(wallet, 85.000, "Kuwait Magic Mall", 29.3759, 47.9774, "2024-02-25");
        createTransaction(wallet, 15.000, "Netflix Subscription", 29.3759, 47.9774, "2024-02-20");
        createTransaction(wallet, 25.000, "OSN Streaming", 29.3759, 47.9774, "2024-02-15");
    }

    private void createTransaction(WalletEntity wallet, double amount, String name, double lat, double lon, String date) {
        TransactionsEntity transaction = new TransactionsEntity();
        transaction.setAmount(amount);
        transaction.setTransactionName(name);
        transaction.setWalletUsed(wallet);
        transaction.setHub(wallet.getHub());
        transaction.setLatitude(lat);
        transaction.setLongitude(lon);
        transaction.setTransactionTime(java.time.LocalDateTime.parse(date + "T00:00:00"));
        transactionsRepository.save(transaction);
    }
}
