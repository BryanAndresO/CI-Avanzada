package ec.edu.espe.buildtestcl;

import ec.edu.espe.buildtestcl.dto.WalletResponse;
import ec.edu.espe.buildtestcl.model.Wallet;
import ec.edu.espe.buildtestcl.repository.WalletRepository;
import ec.edu.espe.buildtestcl.service.RiskClient;
import ec.edu.espe.buildtestcl.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletServiceTest {
    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    public void setUp() {
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    @Test
    void createWallet_validData_ShouldSaveReturnResponse(){
        //Arrange
        String email = "baortiz7@espe.edu.ec";
        double initial = 100.0;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(Boolean.FALSE);
        when(riskClient.isBlocked(email)).thenReturn(Boolean.FALSE);
        
        // Simulamos que al guardar, se devuelve una wallet con ID generado
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet argument = invocation.getArgument(0);
            // Retornamos una nueva instancia simulando que la DB asignó un ID
            return new Wallet("generated-id-123", argument.getOwnerEmail(), argument.getBalance());
        });

        //Act
        WalletResponse response = walletService.createWallet(email, initial);

        //Assert
        assertNotNull(response.getWalletId());
        assertEquals("generated-id-123", response.getWalletId());
        assertEquals(100.0, response.getBalance());

        verify(riskClient).isBlocked(email);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).existsByOwnerEmail(email);
    }

    @Test
    void createWallet_invalidEmail_shouldThrow_andNotCallDependencies(){
        //Arrange
        String invalidEmail = "baortiz-espe.edu.ec";

        //Act
        assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(invalidEmail, 50.0));

        //No debe llamar a ninguna dependencia porque falla la validacion
        verifyNoInteractions(walletRepository, riskClient);
    }

    @Test
    void deposit_walletNotFound_ShouldThrow(){
        //Arrange
        String walletId = "no-exist-wallet";

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        //Act + Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> walletService.deposit(walletId, 60));

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void deposit_shouldUpdateBalance_andSave_UsingCaptor(){
        //Arrange
        // Corregido: Se agrega el ID como primer argumento
        Wallet wallet = new Wallet("123", "baortiz7@espe.edu.ec", 300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        //Act
        double newBalance = walletService.deposit(walletId, 300.0);

        //Assert
        assertEquals(600.0, newBalance);
        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();
        assertEquals(600.0, saved.getBalance());
    }

    @Test
    void withdraw_insufficientFounds_shouldThrow_andNotSave(){
        //Arrange
        Wallet wallet = new Wallet("baortiz7@espe.edu.ec",300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        //Act + Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> walletService.withdraw(walletId, 500.0));

        assertEquals("Fondos insuficientes", exception.getMessage());
        verify(walletRepository, never()).save(any());
    }

    
}
