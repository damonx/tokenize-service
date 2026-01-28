
package nz.co.anz.tokenization.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a token-to-account-number mapping.
 */
@Entity
@Table(name = "tokens")
public class TokenEntity {

    @Id
    private String token;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    protected TokenEntity() {}

    public TokenEntity(String token, String accountNumber) {
        this.token = token;
        this.accountNumber = accountNumber;
    }

    public String getToken() {
        return token;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
