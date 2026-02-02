package ec.edu.espe.buildtestcl.service;

import ec.edu.espe.buildtestcl.dto.WalletResponse;
import ec.edu.espe.buildtestcl.model.Wallet;
import ec.edu.espe.buildtestcl.repository.WalletRepository;

import java.util.Optional;

public class WalletService {

    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    //Crear una cuenta si cumple con las reglas del negocio
    public WalletResponse createWallet(String ownerEmail, double initialBalance) {
        //Validaciones de casos negativos

        if (ownerEmail == null || ownerEmail.isEmpty() || !ownerEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        // Regla de negocio: usuario bloqueado
        if (riskClient.isBlocked(ownerEmail)) {
            throw new IllegalStateException("User blocked");
        }
        //Regla de negocio: No duplicar cuenta por email
        if (walletRepository.existsByOwnerEmail(ownerEmail)) {
            throw new IllegalStateException("Wallet already exists");
        }

        Wallet wallet = new Wallet(null, ownerEmail, initialBalance);
        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(savedWallet.getId(), savedWallet.getBalance());
    }

    //Depositar dinero
    public double deposit(String walletId, double amount){
        //Validaciones
        if (amount <= 0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        Optional<Wallet> found = walletRepository.findById(walletId);
        if (found.isEmpty()){
            throw new IllegalArgumentException("Wallet not found");
        }
        Wallet wallet = found.get();
        wallet.deposit(amount);

        //Persistimos el nuevo saldo
        walletRepository.save(wallet);
        return wallet.getBalance();
    }
}
