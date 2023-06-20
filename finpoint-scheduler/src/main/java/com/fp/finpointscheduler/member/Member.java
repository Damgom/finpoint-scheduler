package com.fp.finpointscheduler.member;

import com.fp.finpointscheduler.token.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true, nullable = false)
    @Email
    @NotBlank
    private String email;

    private String password;

    private String salt;

    private String code;

    private Long goal;

    private Long finPoint;

    @Enumerated(EnumType.STRING)
    private OauthClient oauthClient;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "token_id")
    private Token token;

    private String fintech_use_num;
    public void assignCode(String code) {
        this.code = code;
    }

    public void setFintech_use_num(String fintech_use_num) {
        this.fintech_use_num = fintech_use_num;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void updateFinPoint(Long finPoint) {
        this.finPoint = finPoint;
    }
}
