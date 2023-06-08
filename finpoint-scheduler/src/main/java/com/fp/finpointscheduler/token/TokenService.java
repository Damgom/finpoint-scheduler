package com.fp.finpointscheduler.token;

import com.fp.finpointscheduler.dto.TransactionResponseDto;
import com.fp.finpointscheduler.feign.BankingFeign;
import com.fp.finpointscheduler.member.Member;
import com.fp.finpointscheduler.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;
    private final MemberRepository memberRepository;
    private final BankingFeign bankingFeign;
    private static final String STANDARD_TIME = "235000";
    @Value("${bank.tran}")
    private String TRAN_ID;
    private static String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

//    @Scheduled(cron = "00 50 23 * * *")
    public void getAllToken() {
        List<Token> tokenList = tokenRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<Member> memberList = memberRepository.findAll(Sort.by(Sort.Direction.ASC, "memberId"));
        for (int i = 0; i < tokenList.size(); i++) {
            Token token = tokenList.get(i);
            Member member = memberList.get(i);
            String accessToken = token.getToken_type() + " " + token.getAccess_token();
            // 거래내역조회를 위한 필수 parameter fin_use_num
            String fin_use_num = member.getFintech_use_num();
            TransactionResponseDto transactionResponseDto =
                    bankingFeign.getTransaction(
                            accessToken,
                            generateBankTranId(),
                            fin_use_num,
                            "O",
                            "T",
                            "20230501",
                            "000000",
                            "20230530",
                            "000000",
                            "D",
                            "20230606000000");
            log.info("transaction = {}", transactionResponseDto.getBank_name());
            calculation(transactionResponseDto, member);
        }
    }

    @Transactional
    public void calculation(TransactionResponseDto transactionResponseDto, Member member) {
        List<TransactionResponseDto.Detail> details = transactionResponseDto.getRes_list();
        Long sum = 0L;
        for (TransactionResponseDto.Detail detail : details) {
            String tranAmt = detail.getTran_amt();
            sum += Long.parseLong(tranAmt);
        }
        Long diff = member.getTargetSpend() - sum;
        member.updateFinpoint(member.getFinpoint() + diff);
        log.info("diff = {}", diff);
        memberRepository.save(member);
    }

    public String generateBankTranId() {
        // 10자리 + U + 9자리
        return TRAN_ID + "U" + generateRandomValue();
    }

    public static String generateRandomValue() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(9);

        for (int i = 0; i < 9; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            char randomChar = ALPHANUMERIC.charAt(index);
            sb.append(randomChar);
        }
        log.info("sb length = {}", sb);
        return sb.toString();
    }

    public String getFromDate() {
        LocalDateTime yesterday = LocalDateTime.now().minus(Period.ofDays(1));
        return yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public String getToDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public String getCurDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
