package com.fp.finpointscheduler.token;

import com.fp.finpointscheduler.dto.TransactionResponseDto;
import com.fp.finpointscheduler.feign.BankingFeign;
import com.fp.finpointscheduler.member.Member;
import com.fp.finpointscheduler.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final MemberRepository memberRepository;
    private final BankingFeign bankingFeign;

    private static final int LENGTH_10_INT_RADIX = 10;
    private static final int LENGTH_9_INT_RADIX = 9;
    private static final String STANDARD_TIME = "235000";

    public void getAllToken() {
        List<Token> tokenList = tokenRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<Member> memberList = memberRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
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
                            getFromDate(),
                            STANDARD_TIME,
                            getToDate(),
                            STANDARD_TIME,
                            "D",
                            getCurDate());
            calculation(transactionResponseDto, member);
            //todo 계산돼 나온 finpoint 다시 set
        }
    }

    public String generateBankTranId() {
        // 10자리 + U + 9자리
        UUID uuid = UUID.randomUUID();
        return parseToUUID(uuid.toString()) + "U" + parseToShortUUID(uuid.toString());
    }
    public String parseToUUID(String uuid) {
        int l = ByteBuffer.wrap(uuid.getBytes()).getInt();
        return Integer.toString(l, LENGTH_10_INT_RADIX);
    }
    public String parseToShortUUID(String uuid) {
        int l = ByteBuffer.wrap(uuid.getBytes()).getInt();
        return Integer.toString(l, LENGTH_9_INT_RADIX);
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
        member.updateFinPoint(member.getFinPoint() + diff);
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
