package com.fp.finpointscheduler.feign;

import com.fp.finpointscheduler.dto.TransactionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "feign", url = "${external.url}")
public interface BankingFeign {
    @GetMapping("${external.transaction.list}")
    TransactionResponseDto getTransaction(@RequestHeader("Authorization") String accessToken,
                                          @RequestParam("bank_tran_id") String bank_tran_id,
                                          @RequestParam("fintech_use_num") String fintech_use_num,
                                          @RequestParam("inquiry_type") String inquiry_type,
                                          @RequestParam("inquiry_base") String inquiry_base,
                                          @RequestParam("from_date") String from_date,
                                          @RequestParam("from_time") String from_time,
                                          @RequestParam("to_date") String to_date,
                                          @RequestParam("to_time") String to_time,
                                          @RequestParam("sort_order") String sort_order,
                                          @RequestParam("tran_dtime") String tran_dtime);
}
