package com.fp.finpointscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    public String api_tran_id;
    public String api_tran_dtm;
    public String rsp_code;
    public String rsp_message;
    public String bank_tran_id;
    public String bank_tran_date;
    public String bank_code_tran;
    public String bank_rsp_code;
    public String bank_rsp_message;
    public String bank_name;
    public String savings_bank_name;
    public String fintech_use_num;
    public String balance_amt;
    public String page_record_cnt;
    public String next_page_yn;
    public String befor_inquiry_trace_info;
    public List<Detail> res_list;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        public String tran_date;
        public String tran_time;
        public String inout_type;
        public String tran_type;
        public String print_content;
        public String tran_amt;
        public String after_balance_amt;
        public String branch_name;
    }
}
