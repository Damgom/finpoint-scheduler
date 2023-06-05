package com.fp.finpointscheduler.token;

import com.fp.finpointscheduler.dto.TransactionResponseDto;
import com.fp.finpointscheduler.feign.BankingFeign;
import com.fp.finpointscheduler.member.Member;
import com.fp.finpointscheduler.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
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

    public void getAllToken() {
        List<Token> tokenList = tokenRepository.findAll();
        List<Member> memberList = memberRepository.findAll();
        for (int i = 0; i < tokenList.size(); i++) {
            Token token = tokenList.get(i);
            Member member = memberList.get(i);
            String accessToken = token.getToken_type() + " " + token.getAccess_token();
            // 거래내역조회를 위한 필수 parameter fin_use_num
            String fin_use_num = member.getFintech_use_num();
            // todo: from date, to date, trandtime 현재시간 기준으로 구해서 형식에 맞추는 method 작성
            TransactionResponseDto transactionResponseDto =
                    bankingFeign.getTransaction(
                            accessToken,
                            generateBankTranId(),
                            fin_use_num,
                            "A",
                            "T",
                            "20230604",
                            "235000",
                            "20230605",
                            "235000",
                            "D",
                            "20230606235000");
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

    public void calculation(TransactionResponseDto transactionResponseDto, Member member) {
        List<TransactionResponseDto.Detail> details = transactionResponseDto.getRes_list();
        Long sum = 0L;
        for (TransactionResponseDto.Detail detail : details) {
            String inoutType = detail.getInout_type();
            String tranAmt = detail.getTran_amt();
            switch (inoutType) {
                case "입금" : {
                    sum += Integer.parseInt(tranAmt);
                    return;
                }
                default: {
                    sum -= Integer.parseInt(tranAmt);
                    return;
                }
            }
        }
        //todo: member field에 finpoint, 일일목표소비금액 추가
    }
}
