package ec.edu.espe.buildtestcl.model;

import java.util.UUID;

public class Wallet {
    private final String id;
    private final String ownerEmail;
    private double balance;

    // Constructor con ID proporcionado (útil cuando se recupera de la base de datos)
    public Wallet(String id, String ownerEmail, double balance) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.balance = balance;
    }

    // Constructor sin ID - genera UUID automáticamente
    public Wallet(String ownerEmail, double balance) {
        this.id = UUID.randomUUID().toString();
        this.ownerEmail = ownerEmail;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public double getBalance() {
        return balance;
    }

    //Depositar dinero en la cuenta
    public void deposit(double amount){
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser positivo");
        }
        this.balance += amount;
    }

    //Retirar dinero de la cuenta
    public void withdraw(double amount){
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser positivo");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Fondos insuficientes");
        }
        this.balance -= amount;
    }
}
